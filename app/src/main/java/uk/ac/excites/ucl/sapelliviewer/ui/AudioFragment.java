package uk.ac.excites.ucl.sapelliviewer.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;

import java.io.File;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;


public class AudioFragment extends Fragment {
    private static final String AUDIO_ID = "audioID";
    private static final String AUDIO_PATH = "audioPath";

    private int audioId;
    private String audioPath;
    private RxAudioPlayer rxAudioPlayer;
    private DocumentFragmentListener listener;

    public AudioFragment() {
        // Required empty public constructor
    }

    public void setFragmentListener(DocumentFragmentListener listener) {
        this.listener = listener;
    }

    public static AudioFragment newInstance(int audioID, String audioPath) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putInt(AUDIO_ID, audioID);
        args.putString(AUDIO_PATH, audioPath);
        fragment.setArguments(args);
        return fragment;
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
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().remove(AudioFragment.this).commit();
                    }
                });


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio, container, false);
        return root;
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
