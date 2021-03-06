package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.DateTimeHelpers;


public class PhotoFragment extends Fragment {
    private static final String PHOTO_ID = "photoId";
    private static final String PHOTO_PATH = "photoPath";

    private int photoId;
    private String photoPath;
    private DocumentFragmentListener listener;


    public PhotoFragment() {
        // Required empty public constructor
    }

    public void setFragmentListener(DocumentFragmentListener listener) {
        this.listener = listener;
    }

    public static PhotoFragment newInstance(int photoId, String photoPath) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(PHOTO_ID, photoId);
        args.putString(PHOTO_PATH, photoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoId = getArguments().getInt(PHOTO_ID);
            photoPath = getArguments().getString(PHOTO_PATH);
        }
        if (listener != null)
            listener.OnFragmentAttached(getClass().getSimpleName(), photoId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ImageView photoView = view.findViewById(R.id.photo_view);

        Glide.with(Objects.requireNonNull(getActivity()))
                .asBitmap()
                .load(photoPath)
                .into(photoView);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null)
            listener.OnFragmentDetached(getClass().getSimpleName(), photoId);
    }


}
