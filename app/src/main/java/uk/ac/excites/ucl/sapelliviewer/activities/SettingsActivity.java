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
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
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
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
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
    private List<ContributionProperty> contributionProperties = new ArrayList<ContributionProperty>();


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
            public void openMap(View v, int position) {
                openMapView(projectAdapter.getProject(position).getId());
            }

            @Override
            public void syncProjectOnClick(View v, int position) {
                clickedButton = (ImageButton) v;
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
        if (nameItem != null) {
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
                                    nameItem.setTitle(user.getDisplay_name());
                                    updateProjects();
                                    Log.d(getLocalClassName(), user.getDisplay_name());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    // get user from database
                                    if (e instanceof NoConnectivityException)
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
                                    else
                                        Log.e("USER INFO", e.getMessage());
                                }
                            })
            );
        }
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
                                                                       }
                                                                   }));
                                               } else {
                                                   StringWriter sw = new StringWriter();
                                                   PrintWriter pw = new PrintWriter(sw);
                                                   e.printStackTrace(pw);
                                                   Log.e("Update Projects", sw.toString());
                                               }
                                           }
                                       }
                        )
        );
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

    public void openMapView(int projectId) {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra(PROJECT_ID, projectId);
        startActivity(mapIntent);
    }

    public void getProject(ProjectInfo projectInfo) {
        disposables.add(
                clientWithAuth.getProject(projectInfo.getId())
                        .subscribeOn(Schedulers.io())
                        .doOnNext(project -> loadContributions(projectInfo)) // load contributions in parallel
                        .doOnNext(project -> db.projectInfoDao().insertProject(project))
                        .flatMap(project -> Observable.fromIterable(project.categories))
                        .doOnNext(category -> category.setProjectid(projectInfo.getId()))
                        .doOnNext(category -> db.projectInfoDao().insertCategory(category))
                        .doOnNext(category -> setParentId(category))
                        .flatMap(category -> Observable.fromIterable(category.getFields()))
                        .doOnNext(field -> db.projectInfoDao().insertField(field))
                        .doOnNext(field -> setParentId(field))
                        .toList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Field>>() {
                            @Override
                            public void onSuccess(List<Field> fields) {
                                Log.d("getProject", "Successful");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProject", e.getMessage());
                                updateUI(false);
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
                        downloadfile(lookUpValue.getSymbol());
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
                        .doOnNext(contribution -> insertProperties(project, contribution))
                        .doOnNext(contribution -> insertMedia(project, contribution))
                        .toList()
                        .doOnSuccess(contributions -> db.contributionDao().insertContributions(contributions))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Contribution>>() {
                            @Override
                            public void onSuccess(List<Contribution> contributions) {
                                Log.d("loadContributions", "Successful");
                                insertContributionProperties(contributionProperties);
                                projectAdapter.getCounts(project);
                                updateUI(true);

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getContributions", e.getMessage());
                                updateUI(false);
                            }
                        })
        );
    }

    private void insertMedia(ProjectInfo project, Contribution contribution) {
        if (contribution.getMeta().getNum_media() != 0) {
            disposables.add(
                    clientWithAuth.getMedia(project.getId(), contribution.getId())
                            .subscribeOn(Schedulers.io())
                            .flatMap(Observable::fromIterable)
                            .doOnNext(mediaFile -> mediaFile.setContribution_id(contribution.getId()))
                            .doOnNext(mediaFile -> downloadfile(mediaFile.getUrl()))
                            .toList()
                            .doOnSuccess(mediaList -> db.contributionDao().insertMediaFiles(mediaList))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableSingleObserver<List<Document>>() {
                                @Override
                                public void onSuccess(List<Document> documents) {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("insertMedia", e.getMessage());
                                }
                            }));
        }
    }

    private void insertProperties(ProjectInfo project, Contribution contribution) {
        for (Map.Entry<String, String> property : contribution.getProperties().entrySet()) {
            Field field = db.projectInfoDao().getFieldByKey(property.getKey());
            ContributionProperty contributionProperty = new ContributionProperty(contribution.getId(), field.getId(), property.getKey(), property.getValue());
            if (field.getFieldtype().equals("LookupField"))
                db.projectInfoDao().getLookupValueById(property.getValue()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new SingleObserver<LookUpValue>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                disposables.add(d);
                            }

                            @Override
                            public void onSuccess(LookUpValue lookUpValue) {
                                Log.d("getLookupValueById", "Successful");
                                contributionProperty.setValue(lookUpValue.getName());
                                contributionProperty.setSymbol(lookUpValue.getSymbol());
                                contributionProperties.add(contributionProperty);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getLookupValueById", e.getMessage());
                            }
                        });
            else {
                contributionProperties.add(contributionProperty);
            }
        }
    }

    private boolean writeFileToDisk(ResponseBody responseBody, String url) {
        try {

            String fileName = url.split("/")[url.split("/").length - 1];
            String subPath = url.replace(fileName, "");

            new File("/data/data/" + getPackageName() + subPath).mkdirs();
            File destinationFile = new File("/data/data/" + getPackageName() + subPath + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = responseBody.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = responseBody.byteStream();
                outputStream = new FileOutputStream(destinationFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1)
                        break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;


                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                Log.e("File download", e.getMessage());
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

    public void insertContributionProperties(List<ContributionProperty> contributionProperties) {
        Completable.fromAction(() -> db.contributionDao().insertContributionProperty(contributionProperties)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onComplete() {
                        contributionProperties.clear();
                        Log.d("insertContribProperty", "Successful");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }


                });
    }


    public void updateUI(boolean success) {
        rotator.cancel();
        if (success) {
            clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#37ab52"), PorterDuff.Mode.SRC_IN);
        } else {
            clickedButton.getDrawable().mutate().setColorFilter(Color.parseColor("#c70039"), PorterDuff.Mode.SRC_IN);
        }
    }
}