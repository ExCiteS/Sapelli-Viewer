package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import uk.ac.excites.ucl.sapelliviewer.utils.DateTimeHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


public class DetailsFragment extends Fragment implements AudioFragment.FragmentListener {
    private static final String CONTRIBUTION_ID = "contributionID";

    private int contributionId;
    private OnFragmentInteractionListener interactionListener;
    private ContributionAudioAdapter audioAdapter;

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
        TextView dateTextView = view.findViewById(R.id.txt_date);
        Button btnClose = view.findViewById(R.id.btn_close_fragment);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeView();
            }
        });

        AppDatabase db = AppDatabase.getAppDatabase(getActivity());
        CompositeDisposable disposables = ((OfflineMapsActivity) Objects.requireNonNull(getActivity())).getDisposables();
        RecyclerView valueRecyclerView = view.findViewById(R.id.value_recycler_view);
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        disposables.add(db.contributionDao().getPropertiesByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(contributionProperties -> valueRecyclerView.setAdapter(new ContributionValueAdapter(getActivity(), contributionProperties))));
        RecyclerView photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        disposables.add(db.contributionDao().getPhotosByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(photos -> photoRecyclerView.setAdapter(new ContributionPhotoAdapter(getActivity(), photos, new ContributionPhotoAdapter.PhotoAdapterClickListener() {
                    @Override
                    public void onClick(Document photo) {
                        openPhotoView(photo);
                    }
                }))));
        RecyclerView audioRecyclerView = view.findViewById(R.id.audio_recycler_view);
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


        return view;
    }


    public void closeView() {
        if (interactionListener != null) {
            interactionListener.onFragmentInteraction();
        }
    }

    public void openPhotoView(Document photo) {
        PhotoFragment photoFragment = PhotoFragment.newInstance(photo.getId(), MediaHelpers.dataPath + File.separator + photo.getUrl());
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_media_container, photoFragment)
                .addToBackStack(null)
                .commit();
    }

    public void openAudioView(Document audio) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        if (audio.isActive()) {
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(String.valueOf(audio.getId()))).commit();
        } else {
            AudioFragment audioFragment = AudioFragment.newInstance(audio.getId(), MediaHelpers.dataPath + File.separator + audio.getUrl());
            fragmentManager.beginTransaction().replace(R.id.fragment_media_container, audioFragment, String.valueOf(audio.getId())).commit();
            audioFragment.setFragmentListener(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            interactionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public int getContributionId() {
        return contributionId;
    }

    @Override
    public void OnFragmentAttached(int audioId) {
        audioAdapter.getAudioByid(audioId).setActive(true);
        audioAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnFragmentDetached(int audioId) {
        audioAdapter.getAudioByid(audioId).setActive(false);
        audioAdapter.notifyDataSetChanged();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
