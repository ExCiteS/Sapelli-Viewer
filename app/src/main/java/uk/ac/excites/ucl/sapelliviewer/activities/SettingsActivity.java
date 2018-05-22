package uk.ac.excites.ucl.sapelliviewer.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageButton;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
import uk.ac.excites.ucl.sapelliviewer.ui.GeoKeyProjectAdapter;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

public class SettingsActivity extends AppCompatActivity {

    public static String PROJECT_ID = "project_id";

    private RecyclerView recyclerView;
    private TokenManager tokenManager;
    private GeoKeyClient clientWithAuth;
    private CompositeDisposable disposables;
    private AppDatabase db;
    private GeoKeyProjectAdapter projectAdapter;
    private ImageButton clickedButton;
    private ObjectAnimator rotator;
    private MenuItem nameItem;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance();
        disposables = new CompositeDisposable();
        clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyClient.class, tokenManager);
        db = AppDatabase.getAppDatabase(getApplicationContext());
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
        projectAdapter = new GeoKeyProjectAdapter(getApplicationContext(), disposables, new GeoKeyProjectAdapter.DetailsAdapterListener() {
            @Override
            public void syncContributionOnClick(View v, int position) {
                loadContributions(projectAdapter.getProject(position));
            }

            @Override
            public void syncProjectOnClick(View v, int position) {
                clickedButton = (ImageButton) v;
                getProject(projectAdapter.getProject(position).getId());
                rotator = ObjectAnimator.ofFloat(clickedButton, View.ROTATION, 0f, -360f);
                rotator.setDuration(1000);
                rotator.setRepeatCount(Animation.INFINITE);
                rotator.start();
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
    }

    public void updateUser() {
        GeoKeyClient clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyClient.class, tokenManager);
        disposables.add(
                clientWithAuth.getUserInfo()
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess(user -> db.userDao().clearPrevUser())
                        .doOnSuccess(user -> db.userDao().insertUserInfo(user))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<UserInfo>() {
                            @Override
                            public void onSuccess(UserInfo user) {
                                if (nameItem != null)
                                    nameItem.setTitle(user.getDisplay_name());
                                updateProjects();
                                Log.d(getLocalClassName(), user.getDisplay_name());
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e instanceof NoConnectivityException) {
                                    // get user from database
                                    db.userDao().getUserInfo()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeWith(new SingleObserver<UserInfo>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {
                                                    disposables.add(d);
                                                }

                                                @Override
                                                public void onSuccess(UserInfo userInfo) {
                                                    nameItem.setTitle(userInfo.getDisplay_name());
                                                    updateProjects();
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Log.e("USER INFO", e.getMessage());
                                                }
                                            });
                                } else
                                    Log.e("USER INFO", e.getMessage());
                            }
                        })
        );
    }

    public void updateProjects() {
        disposables.add(
                clientWithAuth.listProjects()
                        .subscribeOn(Schedulers.io())
                        .flatMap(Observable::fromIterable)
                        .filter(projectInfo -> projectInfo.getUser_info().is_admin())
                        .toList()
                        .doOnSuccess(projectInfos -> Log.d(getLocalClassName(), "projects: " + projectInfos.size()))
                        .doOnSuccess(projectInfos -> db.projectInfoDao().clearProjectInfos())
                        .doOnSuccess(projectInfos -> db.projectInfoDao().insertProjectInfo(projectInfos))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ProjectInfo>>() {

                                           @Override
                                           public void onSuccess(List<ProjectInfo> projectInfos) {
                                               Log.d(getLocalClassName(), "projects: " + projectInfos.size());
                                               projectAdapter.setProjects(projectInfos);
                                           }

                                           @Override
                                           public void onError(Throwable e) {
                                               if (e instanceof NoConnectivityException) {
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

                                                               }
                                                           });
                                               } else
                                                   Log.e("Update Projects", e.getMessage());
                                           }
                                       }
                        )
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        nameItem = menu.getItem(0);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                logOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logOut() {
        tokenManager.deleteToken();
        tokenManager.deleteServerUrl();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void openMap(int projectId) {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra(PROJECT_ID, projectId);
        startActivity(mapIntent);
    }

    public void getProject(int projectId) {
        disposables.add(
                clientWithAuth.getProject(projectId)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(project -> db.projectInfoDao().insertProject(project))
                        .concatMap(project -> Observable.fromIterable(project.categories))
                        .doOnNext(category -> category.setProjectid(projectId))
                        .doOnNext(category -> db.projectInfoDao().insertCategory(category))
                        .doOnNext(category -> setParentId(category))
                        .concatMap(category -> Observable.fromIterable(category.getFields()))
                        .doOnNext(field -> db.projectInfoDao().insertField(field))
                        .doOnNext(field -> setParentId(field))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<Field>() {
                            @Override
                            public void onNext(Field field) {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("ERROR", e.getMessage());
                                rotator.cancel();
                                clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#c70039"), PorterDuff.Mode.SRC_IN);
                            }

                            @Override
                            public void onComplete() {
                                rotator.cancel();
                                clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#37ab52"), PorterDuff.Mode.SRC_IN);
                            }
                        })
        );
    }

    public void setParentId(Object object) {
        if (object instanceof Category) {
            Category category = (Category) object;
            for (Field field : category.getFields()) {
                field.setCategory_id(category.getId());
            }
        } else if (object instanceof Field) {
            Field field = (Field) object;
            if (field.getLookupvalues() != null) {
                for (LookUpValue lookUpValue : field.getLookupvalues()) {
                    lookUpValue.setFieldId(field.getId());
                    db.projectInfoDao().insertLookupValue(lookUpValue);
                    if (lookUpValue.getSymbol() != null)
                        downloadfile(lookUpValue.getSymbol().substring(1));
                }
            }
        }
    }

    private void downloadfile(String url) {
        disposables.add(
                clientWithAuth.downloadFileByUrl(url)
                        .observeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<ResponseBody>() {
                            @Override
                            public void onNext(ResponseBody responseBody) {
                                writeFileToDisk(responseBody, url);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        })
        );
    }

    public void loadContributions(ProjectInfo project) {
        disposables.add(
                clientWithAuth.getContributions(project.getId())
                        .flatMap(contributionCollection -> Observable.fromIterable(contributionCollection.getFeatures()))
                        .doOnNext(contribution -> contribution.setProjectId(project.getId()))
                        .doOnNext(contribution -> db.contributionDao().insertContributionProperty(contribution))
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Contribution>>() {

                            @Override
                            public void onSuccess(List<Contribution> contributions) {
                                projectAdapter.getContributionCount(project);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("LoadContributions", e.getMessage());
                            }
                        })
        );
    }

    private void insertProperties(Contribution contribution) {
        for (Map.Entry<String, String> property : contribution.getProperties().entrySet()) {
            Field field = db.projectInfoDao().getFieldByKey(property.getKey());
            ContributionProperty contributionProperty = new ContributionProperty(contribution.getId(), field.getId(), property.getKey(), property.getValue());
            if (field.getFieldtype().equals("LookupField")) {
                LookUpValue lookUpValue = db.projectInfoDao().getLookupValueById(property.getValue());
                contributionProperty.setValue(lookUpValue.getName());
                contributionProperty.setSymbol(lookUpValue.getSymbol());
            }
            db.contributionDao().insertContributionProperty(contributionProperty);
        }
    }

    private boolean writeFileToDisk(ResponseBody responseBody, String url) {
        try {
            File file = new File("/data/data/" + getPackageName() + File.separator + url);
            file.mkdirs();


            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = responseBody.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = responseBody.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1)
                        break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;

                    Log.d(getLocalClassName(), "file downloaded " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.flush();
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}