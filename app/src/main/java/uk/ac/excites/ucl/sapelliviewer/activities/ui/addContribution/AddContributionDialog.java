package uk.ac.excites.ucl.sapelliviewer.activities.ui.addContribution;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import uk.ac.excites.ucl.sapelliviewer.R;

public class AddContributionDialog extends DialogFragment {

    private static final String TAG = "AddContributionDialog";
    private final int REQUEST_CAMERA_PERMISSION = 1;
    private AppCompatImageButton buttonOk;
    private AppCompatImageButton buttonCancel;
    private AppCompatImageButton buttonAddText;
    private AppCompatImageButton buttonAddAudio;
    private AppCompatImageButton buttonAddPhoto;


    public static AddContributionDialog newInstance() {
        Bundle args = new Bundle();
        AddContributionDialog fragment = new AddContributionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_contribution, container, false);

        setUp(view);
        return view;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    private void setUp(View view) {

        buttonOk = view.findViewById(R.id.buttonOk);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonAddText = view.findViewById(R.id.buttonAddText);
        buttonAddAudio = view.findViewById(R.id.buttonAddAudio);
        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto);

        buttonOk.setOnClickListener(view1 -> dismiss());
        buttonCancel.setOnClickListener(view1 -> dismiss());

        buttonAddPhoto.setOnClickListener(view1 -> takePhoto());
    }

    private void takePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            } else {
                startCameraIntent();
            }
        } else {
            startCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startCameraIntent();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA_PERMISSION);
    }
}
