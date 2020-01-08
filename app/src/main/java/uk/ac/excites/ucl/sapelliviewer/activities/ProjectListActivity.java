package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.ui.ProjectIconListAdapter;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;

public class ProjectListActivity extends AppCompatActivity {
    private static final String ARG_PROJECTS = "arg_projects";

    private CompositeDisposable disposables;
    private AppDatabase db;
    private GeoKeyClient geoKeyclient;
    private ProjectIconListAdapter remoteProjectListAdapter;
    private ProjectIconListAdapter localProjectListAdapter;
    private ArrayList<ProjectInfo> localProjects = new ArrayList<>();
    private ArrayList<ProjectInfo> remoteProjects = new ArrayList<>();

    public static Intent newIntent(Context context, List<ProjectInfo> projects) {
        return new Intent(context, ProjectListActivity.class)
                .putExtra(ARG_PROJECTS, new ArrayList<>(projects));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        db = AppDatabase.getAppDatabase(ProjectListActivity.this);
        geoKeyclient = new GeoKeyClient(ProjectListActivity.this);
        setContentView(R.layout.activity_project_list);

        RecyclerView rvProjectLocal = findViewById(R.id.rvProjectLocal);
        rvProjectLocal.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        localProjectListAdapter = new ProjectIconListAdapter(projectInfo -> {

        });
        rvProjectLocal.setAdapter(localProjectListAdapter);

        RecyclerView rvProjectRemote = findViewById(R.id.rvProjectRemote);
        rvProjectRemote.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        remoteProjectListAdapter = new ProjectIconListAdapter(projectInfo -> {

        });
        rvProjectRemote.setAdapter(remoteProjectListAdapter);
        handleIntent();
    }

    private void handleIntent() {
        if (getIntent() != null && getIntent().hasExtra(ARG_PROJECTS)) {
            ArrayList<ProjectInfo> projectInfos = (ArrayList<ProjectInfo>) getIntent().getSerializableExtra(ARG_PROJECTS);
            updateAdapters(projectInfos);
        }
    }

    private void updateAdapters(ArrayList<ProjectInfo> projectInfos) {
        for (ProjectInfo p : projectInfos) {
            if (p.isRemote()) {
                remoteProjects.add(p);
            } else
                localProjects.add(p);
        }
//        localProjects.add(new ProjectInfo());
        remoteProjectListAdapter.setProjects(remoteProjects);
        localProjectListAdapter.setProjects(localProjects);
    }

    /* Fetch project information list from server and update database and UI */
    public void updateProjects() {
        disposables.add(
                geoKeyclient.updateProjects()
                        .subscribe(
                                projectInfos -> updateAdapters(new ArrayList<>(projectInfos)),
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
                                updateAdapters(new ArrayList<>(projectInfos));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getProjectInfosDB", e.getMessage());
                            }
                        }));
    }
}
