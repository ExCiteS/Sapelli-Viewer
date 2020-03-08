package uk.ac.excites.ucl.sapelliviewer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;

import java.io.File;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;


public class AudioFragment extends ProjectManagerFragment {
    private static final String AUDIO_ID = "audioID";
    private static final String AUDIO_PATH = "audioPath";

    private int audioId;
    private String audioPath;
    private RxAudioPlayer rxAudioPlayer;
    private DocumentFragmentListener listener;

    public AudioFragment() {
        // Required empty public constructor
    }

    static AudioFragment ShowDialog(AppCompatActivity owner, int audioID, String audioPath) {
        AudioFragment fragment = AudioFragment.newInstance(audioID, audioPath);
        fragment.show(owner.getSupportFragmentManager(), AudioFragment.class.getSimpleName());
        return fragment;
    }

    private static AudioFragment newInstance(int audioID, String audioPath) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putInt(AUDIO_ID, audioID);
        args.putString(AUDIO_PATH, audioPath);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getOwner());
        AlertDialog dialog = builder.create();

        // Set view:
        setDialogView(dialog);
        return dialog;
    }

    void setFragmentListener(DocumentFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            audioId = getArguments().getInt(AUDIO_ID);
            audioPath = getArguments().getString(AUDIO_PATH);
        }
        if (listener != null)
            listener.OnFragmentAttached(getClass().getSimpleName(), audioId);

        rxAudioPlayer = RxAudioPlayer.getInstance();

        rxAudioPlayer.play(
                PlayConfig.file(new File(audioPath))
                        .build())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        ((OfflineMapsActivity) Objects.requireNonNull(getActivity())).getDisposables().add(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("rxAudioPlayer play", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismiss();
                    }
                });
    }

    @Override
    protected void setupUI(AppCompatActivity owner, View rootLayout) throws Exception {
        super.setupUI(owner, rootLayout);
        ImageView photoView = rootLayout.findViewById(R.id.photo_view);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(metrics.widthPixels, metrics.heightPixels);
        photoView.setLayoutParams(params);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    protected Integer getLayoutID() {
        return R.layout.fragment_audio;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxAudioPlayer.stopPlay();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null)
            listener.OnFragmentDetached(getClass().getSimpleName(), audioId);
    }
}