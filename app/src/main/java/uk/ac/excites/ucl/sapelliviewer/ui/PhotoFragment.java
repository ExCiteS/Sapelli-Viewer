package uk.ac.excites.ucl.sapelliviewer.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import uk.ac.excites.ucl.sapelliviewer.R;


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
