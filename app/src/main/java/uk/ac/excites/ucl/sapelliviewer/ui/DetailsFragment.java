package uk.ac.excites.ucl.sapelliviewer.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;
import uk.ac.excites.ucl.sapelliviewer.utils.DateTimeHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


public class DetailsFragment extends DialogFragment implements DocumentFragmentListener {
    private static final String CONTRIBUTION_ID = "contributionId";
    private static final String PROJECT_ID = "projectId";

    private int contributionId;
    private int projectId;
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

    public static DetailsFragment newInstance(int contributionId, int projectId) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(CONTRIBUTION_ID, contributionId);
        args.putInt(PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            contributionId = 30334;
            contributionId = getArguments().getInt(CONTRIBUTION_ID);
            projectId = getArguments().getInt(PROJECT_ID);
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
        dbClient = new DatabaseClient(getContext(), projectId, null);
        CompositeDisposable disposables = new CompositeDisposable();
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        Log.d("contribution", "onStart: " + contributionId);
//        contributionId = 30340;
        disposables.add(db.contributionDao().getPropertiesByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contributionProperties ->
                {
                    valueRecyclerView.setAdapter(new ContributionValueAdapter(getActivity(), contributionProperties));
                }));

        photoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        disposables.add(db.contributionDao().getPhotosByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(photos -> {
                    photoAdapter = new ContributionPhotoAdapter(getActivity(), photos, photo -> openPhotoView(photo));
                    photoRecyclerView.setAdapter(photoAdapter);
                }));
        audioRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        disposables.add(db.contributionDao().getAudiosByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audios -> {
                    audioAdapter = new ContributionAudioAdapter(getActivity(), audios, audio -> openAudioView(audio));
                    audioRecyclerView.setAdapter(audioAdapter);
                }));

        disposables.add(db.contributionDao().getDateByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(contributionProperty -> DateTimeHelpers.parseIso8601DateTime(contributionProperty.getValue())).subscribe(date -> dateTextView.setText(DateTimeHelpers.dateToString(date))));

    }


    public void openPhotoView(Document photo) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        if (photo.isActive()) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(String.valueOf(photo.getId()))).commit();
            dbClient.insertLog(Logger.PHOTO_CLOSED, photo.getId());
        } else {
            PhotoFragment photoFragment = PhotoFragment.newInstance(photo.getId(), MediaHelpers.dataPath + File.separator + photo.getUrl());
//            fragmentManager.beginTransaction().replace(R.id.fragment_media_container, photoFragment, String.valueOf(photo.getId())).commit();
            photoFragment.setFragmentListener(this);
            dbClient.insertLog(Logger.PHOTO_OPENED, photo.getId());
        }
    }

    public void openAudioView(Document audio) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        if (audio.isActive()) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(String.valueOf(audio.getId()))).commit();
            dbClient.insertLog(Logger.AUDIO_CLOSED, audio.getId());
        } else {
            AudioFragment audioFragment = AudioFragment.newInstance(audio.getId(), MediaHelpers.dataPath + File.separator + audio.getUrl());
//            fragmentManager.beginTransaction().replace(R.id.fragment_media_container, audioFragment, String.valueOf(audio.getId())).commit();
            audioFragment.setFragmentListener(this);
            dbClient.insertLog(Logger.AUDIO_OPENED, audio.getId());
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
