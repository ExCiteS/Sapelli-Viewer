package uk.ac.excites.ucl.sapelliviewer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import uk.ac.excites.ucl.sapelliviewer.R;


public class PhotoFragment extends ProjectManagerFragment {
    private static final String PHOTO_ID = "photoId";
    private static final String PHOTO_PATH = "photoPath";

    private int photoId;
    private String photoPath;
    private DocumentFragmentListener listener;

    public PhotoFragment() {
        // Required empty public constructor
    }

    static PhotoFragment ShowDialog(AppCompatActivity owner, int photoId, String photoPath) {
        PhotoFragment fragment = PhotoFragment.newInstance(photoId, photoPath);
        fragment.show(owner.getSupportFragmentManager(), PhotoFragment.class.getSimpleName());
        return fragment;
    }

    private static PhotoFragment newInstance(int photoId, String photoPath) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(PHOTO_ID, photoId);
        args.putString(PHOTO_PATH, photoPath);
        fragment.setArguments(args);
        return fragment;
    }

    void setFragmentListener(DocumentFragmentListener listener) {
        this.listener = listener;
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
    protected void setupUI(AppCompatActivity owner, View rootLayout) throws Exception {
        super.setupUI(owner, rootLayout);
        ImageView photoView = rootLayout.findViewById(R.id.photo_view);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(metrics.widthPixels, metrics.heightPixels);
        photoView.setLayoutParams(params);

        Glide.with(photoView.getContext())
                .asBitmap()
                .load(photoPath)
                .into(photoView);
    }

    @Override
    protected Integer getLayoutID() {
        return R.layout.fragment_photo;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener != null)
            listener.OnFragmentDetached(getClass().getSimpleName(), photoId);
    }
}
