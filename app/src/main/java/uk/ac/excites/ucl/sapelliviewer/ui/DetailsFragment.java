package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;
import uk.ac.excites.ucl.sapelliviewer.utils.DateTimeHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


public class DetailsFragment extends Fragment implements DocumentFragmentListener {
    private static final String CONTRIBUTION_ID = "contributionID";

    private int contributionId;
    private ContributionAudioAdapter audioAdapter;
    private ContributionPhotoAdapter photoAdapter;
    private RecyclerView valueRecyclerView;
    private RecyclerView photoRecyclerView;
    private RecyclerView audioRecyclerView;
    private TextView dateTextView;
    private DatabaseClient dbClient;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(int contributionId) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(CONTRIBUTION_ID, contributionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contributionId = getArguments().getInt(CONTRIBUTION_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        dateTextView = view.findViewById(R.id.txt_date);
        valueRecyclerView = view.findViewById(R.id.value_recycler_view);
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        audioRecyclerView = view.findViewById(R.id.audio_recycler_view);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        AppDatabase db = AppDatabase.getAppDatabase(getActivity());
        dbClient = ((OfflineMapsActivity) Objects.requireNonNull(getActivity())).getDbClient();
        CompositeDisposable disposables = ((OfflineMapsActivity) Objects.requireNonNull(getActivity())).getDisposables();
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        disposables.add(db.contributionDao().getPropertiesByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(contributionProperties -> valueRecyclerView.setAdapter(new ContributionValueAdapter(getActivity(), contributionProperties))));
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        disposables.add(db.contributionDao().getPhotosByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(photos -> {
                    photoAdapter = new ContributionPhotoAdapter(getActivity(), photos, new ContributionPhotoAdapter.PhotoAdapterClickListener() {
                        @Override
                        public void onClick(Document photo) {
                            openPhotoView(photo);
                        }
                    });
                    photoRecyclerView.setAdapter(photoAdapter);
                }));
        audioRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        disposables.add(db.contributionDao().getAudiosByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(audios -> {
                    audioAdapter = new ContributionAudioAdapter(getActivity(), audios, new ContributionAudioAdapter.AudioAdapterClickListener() {
                        @Override
                        public void onClick(Document audio) {
                            openAudioView(audio);
                        }
                    });
                    audioRecyclerView.setAdapter(audioAdapter);
                }));
        disposables.add(db.contributionDao().getDateByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .map(contributionProperty -> DateTimeHelpers.parseIso8601DateTime(contributionProperty.getValue())).subscribe(date -> dateTextView.setText(DateTimeHelpers.dateToString(date))));

    }


    public void openPhotoView(Document photo) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        if (photo.isActive()) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(String.valueOf(photo.getId()))).commit();
            dbClient.insertLog(Logger.PHOTO_CLOSED , photo.getId());
        } else {
            PhotoFragment photoFragment = PhotoFragment.newInstance(photo.getId(), MediaHelpers.dataPath + File.separator + photo.getUrl());
            fragmentManager.beginTransaction().replace(R.id.fragment_media_container, photoFragment, String.valueOf(photo.getId())).commit();
            photoFragment.setFragmentListener(this);
            dbClient.insertLog(Logger.PHOTO_OPENED , photo.getId());
        }
    }

    public void openAudioView(Document audio) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        if (audio.isActive()) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(String.valueOf(audio.getId()))).commit();
            dbClient.insertLog(Logger.AUDIO_CLOSED , audio.getId());
        } else {
            AudioFragment audioFragment = AudioFragment.newInstance(audio.getId(), MediaHelpers.dataPath + File.separator + audio.getUrl());
            fragmentManager.beginTransaction().replace(R.id.fragment_media_container, audioFragment, String.valueOf(audio.getId())).commit();
            audioFragment.setFragmentListener(this);
            dbClient.insertLog(Logger.AUDIO_OPENED , audio.getId());
        }
    }


    public int getContributionId() {
        return contributionId;
    }

    @Override
    public void OnFragmentAttached(String type, int documentId) {
        if (type.equals("AudioFragment")) {
            audioAdapter.getAudioByid(documentId).setActive(true);
            audioAdapter.notifyDataSetChanged();
        } else if (type.equals("PhotoFragment")) {
            photoAdapter.getPhotoByid(documentId).setActive(true);
            photoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnFragmentDetached(String type, int documentId) {
        if (type.equals("AudioFragment")) {
            audioAdapter.getAudioByid(documentId).setActive(false);
            audioAdapter.notifyDataSetChanged();
        } else if (type.equals("PhotoFragment")) {
            photoAdapter.getPhotoByid(documentId).setActive(false);
            photoAdapter.notifyDataSetChanged();
        }
    }

}
