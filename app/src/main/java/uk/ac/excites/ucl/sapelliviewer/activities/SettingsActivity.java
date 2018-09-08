package uk.ac.excites.ucl.sapelliviewer.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import lib.folderpicker.FolderPicker;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectProperties;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyRequests;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
import uk.ac.excites.ucl.sapelliviewer.ui.GeoKeyProjectAdapter;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;
import uk.ac.excites.ucl.sapelliviewer.utils.SdCardStorage;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

public class SettingsActivity extends AppCompatActivity {

    public static final String PROJECT_ID = "project_id";
    public static final String ERROR_CODE = "error_code";
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    private static final int FILE_PICKER_REQUEST_CODE = 434;

    private RecyclerView recyclerView;
    private TokenManager tokenManager;
    private CompositeDisposable disposables;
    private AppDatabase db;
    private GeoKeyProjectAdapter projectAdapter;
    private ImageView clickedButton;
    private ObjectAnimator rotator;
    private MenuItem nameItem;
    private int mapPathPosition;
    private GeoKeyClient geoKeyclient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        tokenManager = TokenManager.getInstance();
        disposables = new CompositeDisposable();
        db = AppDatabase.getAppDatabase(SettingsActivity.this);
        geoKeyclient = new GeoKeyClient(SettingsActivity.this);
        setContentView(R.layout.activity_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_sapelli_viewer);

        recyclerView = (RecyclerView) findViewById(R.id.project_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new GeoKeyProjectAdapter(SettingsActivity.this, disposables, new GeoKeyProjectAdapter.ProjectAdapterClickListener() {
            @Override
            public void openMap(View v, int position) {
                openMapView(projectAdapter.getProject(position).getId());
            }

            @Override
            public void syncProjectOnClick(View v, int position) {
                clickedButton = (ImageView) v;
                clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);
                getProject(projectAdapter.getProject(position));
                rotator = ObjectAnimator.ofFloat(clickedButton, View.ROTATION, 0f, -360f);
                rotator.setDuration(1000);
                rotator.setRepeatCount(Animation.INFINITE);
                rotator.start();
            }

            @Override
            public void activateMapOnClick(View v, int position) {
                int clickedProjectId = projectAdapter.getProject(position).getId();
                projectAdapter.toggleProject(clickedProjectId);


            }

            @Override
            public void setMapPath(View v, int position) {
                mapPathPosition = position;
                checkPermissionsAndOpenFilePicker();
            }
        });
        recyclerView.setAdapter(projectAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.getToken().getAccess_token() != null) {
            updateUser();
        }
        if (tokenManager.getActiveProject() != -1) {
            openMapView(tokenManager.getActiveProject());
        }

    }

    public void updateUser() {
        if (nameItem != null) {
            GeoKeyRequests clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyRequests.class, tokenManager);
            disposables.add(
                    clientWithAuth.getUserInfo()
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess(user -> db.userDao().clearPrevUser())
                            .doOnSuccess(user -> db.userDao().insertUserInfo(user))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<UserInfo>() {
                                @Override
                                public void onSuccess(UserInfo user) {
                                    nameItem.setTitle(user.getDisplay_name());
                                    updateProjects();
                                    Log.d(getLocalClassName(), user.getDisplay_name());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    disposables.add(
                                            db.userDao().getUserInfo()
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeWith(new DisposableSingleObserver<UserInfo>() {
                                                        @Override
                                                        public void onSuccess(UserInfo userInfo) {
                                                            Log.d("getUserFromDB", "SUCCESS");
                                                            nameItem.setTitle(userInfo.getDisplay_name());
                                                            listProjects();
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {
                                                            Log.e("getUserFromDB", e.getMessage());
                                                            logOut(e.getMessage());
                                                        }
                                                    }));

                                }
                            })
            );
        }
    }

    /* Fetch project information list from server and update database and UI */
    public void updateProjects() {
        disposables.add(
                geoKeyclient.updateProjects()
                        .subscribe(
                                projectInfos -> projectAdapter.setProjects(projectInfos),
                                error -> {
                                    if (error instanceof NoConnectivityException)
                                        listProjects(); // if no internet connection -> get projects from db
                                    else Log.e("Update Projects", error.getMessage());
                                })
        );
    }

    public void listProjects() {
        disposables.add(
                db.projectInfoDao().getProjectInfos()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ProjectInfo>>() {
                            @Override
                            public void onSuccess(List<ProjectInfo> projectInfos) {
                                projectAdapter.setProjects(projectInfos);
                                recyclerView.setAdapter(projectAdapter);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProjectInfosDB", e.getMessage());
                            }
                        }));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        nameItem = menu.getItem(0);
        updateUser();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                logOut(null);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logOut(String errorCode) {
        tokenManager.deleteToken();
        tokenManager.deleteServerUrl();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        if (errorCode != null)
            loginIntent.putExtra(ERROR_CODE, errorCode);
        startActivity(loginIntent);
        finish();
    }

    public void openMapView(int projectId) {
        Intent mapIntent = new Intent(this, OfflineMapsActivity.class);
        mapIntent.putExtra(PROJECT_ID, projectId);
        startActivity(mapIntent);
    }

    public void getProject(ProjectInfo projectInfo) {
        int projectID = projectInfo.getId();

        Observable<Object> contributionAndMediaObervable =
                Observable.merge(geoKeyclient.getContributionsWithProperties(projectID), geoKeyclient.getMedia(projectID));

        disposables.add(
                Observable.concat(geoKeyclient.getProject(projectID), contributionAndMediaObervable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<Object>() {
                            @Override
                            public void onNext(Object o) {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProject", e.getMessage());
                                updateUI(false);
                            }

                            @Override
                            public void onComplete() {
                                projectAdapter.getCounts(projectInfo);
                                updateUI(true);
                            }
                        }));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }


    public void updateUI(boolean success) {
        rotator.cancel();
        if (success) {
            clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#37ab52"), PorterDuff.Mode.SRC_IN);
        } else {
            clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#c70039"), PorterDuff.Mode.SRC_IN);
        }
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", getResources().getString(R.string.choose_directory));
        intent.putExtra("location", SdCardStorage.getExternalStoragePath(SettingsActivity.this, true));
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String folderLocation = data.getExtras().getString("data");
            ProjectProperties projectProperties = new ProjectProperties(projectAdapter.getProject(mapPathPosition).getId(), folderLocation);

            disposables.add(
                    Completable.fromAction(() -> db.projectInfoDao().insertProjectProperties(projectProperties)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    projectAdapter.notifyItemChanged(mapPathPosition);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("setMapPath", e.getMessage());
                                }
                            }));
        }
    }
}
