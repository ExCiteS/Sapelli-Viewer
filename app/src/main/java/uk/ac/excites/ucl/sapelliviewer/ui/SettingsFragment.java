package uk.ac.excites.ucl.sapelliviewer.ui;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectProperties;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;


public class SettingsFragment extends Fragment {
    private static final String PROJECT_ID = "projectID";

    private int projectId;
    private AppDatabase db;
    private GeoKeyClient geoKeyclient;
    private CompositeDisposable disposables;
    private ObjectAnimator rotator;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance(int projectId) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt(PROJECT_ID);
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_settings, container, false);

        disposables = new CompositeDisposable();
        db = AppDatabase.getAppDatabase(getActivity());
        geoKeyclient = new GeoKeyClient(getActivity());

        TextView projectName = view.findViewById(R.id.txt_project_name);
        disposables.add(
                db.projectInfoDao().getProjectInfo(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(project -> projectName.setText(project.getName())));

        /* INSTANTIATE ALL VIEWS */
        ToggleButton toggleLogging = view.findViewById(R.id.tggl_logging);
        toggleLogging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Completable.fromAction(() -> db.projectInfoDao().setLogging(projectId, isChecked)).subscribeOn(Schedulers.io()).subscribe();
            }
        });

        Button exportLogs = view.findViewById(R.id.btn_logs);
        exportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportLogs();
            }
        });

        RadioGroup layerOptions = view.findViewById(R.id.radio_show_fields);
        layerOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checked) {
                switch (checked) {
                    case R.id.show_all_fields:
                        Completable.fromAction(() -> db.projectInfoDao().showFields(projectId, "all")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                    case R.id.show_no_fields:
                        Completable.fromAction(() -> db.projectInfoDao().showFields(projectId, "none")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                    case R.id.show_display_fields:
                        Completable.fromAction(() -> db.projectInfoDao().showFields(projectId, "display")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                }
            }
        });

        RadioGroup upDirection = view.findViewById(R.id.radio_whatsup);
        upDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checked) {
                switch (checked) {
                    case R.id.radio_north:
                        Completable.fromAction(() -> db.projectInfoDao().setUpDirection(projectId, "north")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                    case R.id.radio_east:
                        Completable.fromAction(() -> db.projectInfoDao().setUpDirection(projectId, "east")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                    case R.id.radio_south:
                        Completable.fromAction(() -> db.projectInfoDao().setUpDirection(projectId, "south")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                    case R.id.radio_west:
                        Completable.fromAction(() -> db.projectInfoDao().setUpDirection(projectId, "west")).subscribeOn(Schedulers.io()).subscribe();
                        break;
                }
            }
        });

        ImageView reloadProject = view.findViewById(R.id.img_reload);
        reloadProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getProject((ImageView) v);
            }
        });

        disposables.add(
                db.projectInfoDao().getProjectProperties(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ProjectProperties>() {
                            @Override
                            public void onSuccess(ProjectProperties projectProperties) {
                                toggleLogging.setChecked(projectProperties.isLogging());

                                // Radiobutton initialisation
                                switch (projectProperties.getShowFields()) {
                                    case "all":
                                        layerOptions.check(R.id.show_all_fields);
                                        break;
                                    case "none":
                                        layerOptions.check(R.id.show_no_fields);
                                        break;
                                    case "display":
                                        layerOptions.check(R.id.show_display_fields);
                                        break;

                                }

                                switch (projectProperties.getUpDirection()) {
                                    case "north":
                                        upDirection.check(R.id.radio_north);
                                        break;
                                    case "east":
                                        upDirection.check(R.id.radio_east);
                                        break;
                                    case "south":
                                        upDirection.check(R.id.radio_south);
                                        break;
                                    case "west":
                                        upDirection.check(R.id.radio_west);
                                        break;
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProjectProperties", e.getMessage());
                            }
                        }));

        return view;
    }

    private void exportLogs() {
        File externalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dataDirectory = Environment.getDataDirectory();

        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/" + getActivity().getApplicationInfo().packageName + "/databases/app-database";
        String backupDBPath = "db_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".sqlite";
        File currentDB = new File(dataDirectory, currentDBPath);
        File backupDB = new File(externalStorageDirectory, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());

            Toast.makeText(getActivity(), R.string.db_exported, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (source != null) source.close();
                if (destination != null) destination.close();
            } catch (IOException e) {
                Log.e("Export db", e.getMessage());
            }
        }
    }


    public void getProject(ImageView clicked) {
        clicked.getDrawable().mutate().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
        rotator = ObjectAnimator.ofFloat(clicked, View.ROTATION, 0f, -360f);
        rotator.setDuration(1000);
        rotator.setRepeatCount(Animation.INFINITE);
        rotator.start();


        Observable<Object> contributionAndMediaObervable =
                Observable.merge(geoKeyclient.getContributionsWithProperties(projectId), geoKeyclient.getMedia(projectId));
        disposables.add(
                Observable.concat(geoKeyclient.getProject(projectId), contributionAndMediaObervable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<Object>() {
                            @Override
                            public void onNext(Object o) {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProject", e.getMessage());
                                clicked.getDrawable().mutate().setColorFilter(Color.parseColor("#c70039"), PorterDuff.Mode.SRC_IN);
                                rotator.cancel();
                            }

                            @Override
                            public void onComplete() {
                                clicked.getDrawable().mutate().setColorFilter(Color.parseColor("#37ab52"), PorterDuff.Mode.SRC_IN);
                                rotator.cancel();
                            }
                        }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
