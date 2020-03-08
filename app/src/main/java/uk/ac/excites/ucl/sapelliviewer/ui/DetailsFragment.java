package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

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


public class DetailsFragment extends ProjectManagerFragment implements DocumentFragmentListener {
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
    private DocumentFragmentListener listener;

    private DetailsFragment() {
        // Required empty public constructor
    }

    static public DetailsFragment ShowDialog(FragmentActivity owner, int contributionId, int projectId) {
        DetailsFragment fragment = DetailsFragment.newInstance(contributionId, projectId);
        fragment.show(owner.getSupportFragmentManager(), DetailsFragment.class.getSimpleName());
        return fragment;
    }

    private static DetailsFragment newInstance(int contributionId, int projectId) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(CONTRIBUTION_ID, contributionId);
        args.putInt(PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getOwner());
        AlertDialog dialog = builder.create();

        // Set view:
        setDialogView(dialog);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contributionId = getArguments().getInt(CONTRIBUTION_ID);
            projectId = getArguments().getInt(PROJECT_ID);
        }
        if (listener != null)
            listener.OnFragmentAttached(getClass().getSimpleName(), contributionId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null)
            listener.OnFragmentDetached(getClass().getSimpleName(), contributionId);
    }

    @Override
    protected Integer getLayoutID() {
        return R.layout.fragment_details;
    }

    @Override
    protected void setupUI(AppCompatActivity owner, View rootLayout) throws Exception {
        super.setupUI(owner, rootLayout);
        dateTextView = rootLayout.findViewById(R.id.txt_date);
        valueRecyclerView = rootLayout.findViewById(R.id.value_recycler_view);
        photoRecyclerView = rootLayout.findViewById(R.id.photo_recycler_view);
        audioRecyclerView = rootLayout.findViewById(R.id.audio_recycler_view);
        getDialog().setCanceledOnTouchOutside(true);
    }

    public void setFragmentListener(DocumentFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        AppDatabase db = AppDatabase.getAppDatabase(getActivity());
        dbClient = new DatabaseClient(getContext(), projectId, null);
        CompositeDisposable disposables = new CompositeDisposable();
        LinearLayoutManager lm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        valueRecyclerView.setLayoutManager(lm);
        Log.d("contribution", "onStart: " + contributionId);

//        contributionId = 30340;
//        contributionId = 29081; //with photo
//        contributionId = 29395; //with audio
        disposables.add(db.contributionDao().getPropertiesByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contributionProperties -> {
                    valueRecyclerView.setAdapter(new ContributionValueAdapter(getActivity(), contributionProperties));
                }));

        photoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        disposables.add(db.contributionDao().getPhotosByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(photos -> {
                    Log.d("photos", "count: " + photos.size());
                    if (photos.isEmpty() && audioAdapter != null && audioAdapter.getItemCount() == 0)
                        getView().findViewById(R.id.lnrMedia).setVisibility(View.GONE);

                    photoAdapter = new ContributionPhotoAdapter(getActivity(), photos, photo -> openPhotoView(photo));
                    photoRecyclerView.setAdapter(photoAdapter);
                }));
        audioRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        disposables.add(db.contributionDao().getAudiosByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audios -> {
                    Log.d("audios", "count: " + audios.size());
                    if (audios.isEmpty() && photoAdapter != null && photoAdapter.getItemCount() == 0)
                        getView().findViewById(R.id.lnrMedia).setVisibility(View.GONE);

                    audioAdapter = new ContributionAudioAdapter(getActivity(), audios, audio -> openAudioView(audio));
                    audioRecyclerView.setAdapter(audioAdapter);
                }));

        disposables.add(db.contributionDao().getDateByContribution(contributionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(contributionProperty -> DateTimeHelpers.parseIso8601DateTime(contributionProperty.getValue())).subscribe(date -> dateTextView.setText(DateTimeHelpers.dateToString(date))));

        Dialog d = getDialog();
        if (d != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int height = metrics.heightPixels;

            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(4.5f * height / 5));
        }
    }

    private void openPhotoView(Document photo) {
        PhotoFragment photoFragment = PhotoFragment.ShowDialog(getOwner(), photo.getId(), MediaHelpers.dataPath + File.separator + photo.getUrl());
        photoFragment.setFragmentListener(this);
    }

    private void openAudioView(Document audio) {
        AudioFragment audioFragment = AudioFragment.ShowDialog(getOwner(), audio.getId(), MediaHelpers.dataPath + File.separator + audio.getUrl());
        audioFragment.setFragmentListener(this);
    }

    public int getContributionId() {
        return contributionId;
    }

    @Override
    public void OnFragmentAttached(String type, int documentId) {
        if (type.equals("AudioFragment")) {
            audioAdapter.getAudioByid(documentId).setActive(true);
            audioAdapter.notifyDataSetChanged();
            dbClient.insertLog(Logger.AUDIO_OPENED, documentId);
        } else if (type.equals("PhotoFragment")) {
            photoAdapter.getPhotoByid(documentId).setActive(true);
            photoAdapter.notifyDataSetChanged();
            dbClient.insertLog(Logger.PHOTO_OPENED, documentId);
        }
    }

    @Override
    public void OnFragmentDetached(String type, int documentId) {
        if (type.equals("AudioFragment")) {
            audioAdapter.getAudioByid(documentId).setActive(false);
            audioAdapter.notifyDataSetChanged();
            dbClient.insertLog(Logger.AUDIO_CLOSED, documentId);
        } else if (type.equals("PhotoFragment")) {
            photoAdapter.getPhotoByid(documentId).setActive(false);
            photoAdapter.notifyDataSetChanged();
            dbClient.insertLog(Logger.PHOTO_CLOSED, documentId);
        }
    }

}
