package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
import uk.ac.excites.ucl.sapelliviewer.ui.GeoKeyProjectAdapter;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;


public class SettingsActivity extends AppCompatActivity {

    public static String PROJECT_ID = "project_id";

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TokenManager tokenManager;
    private GeoKeyClient clientWithAuth;
    private CompositeDisposable disposables;
    private AppDatabase db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance();
        disposables = new CompositeDisposable();
        clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyClient.class, tokenManager);
        db = AppDatabase.getAppDatabase(getApplicationContext());
        setContentView(R.layout.activity_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_sapelli_viewer);

        recyclerView = (RecyclerView) findViewById(R.id.project_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                Toast.makeText(SettingsActivity.this, Integer.toString(adminProjects.get(position).getId()), Toast.LENGTH_SHORT).show();
////                getProject(adminProjects.get(position).getId());
////                getContributions(adminProjects.get(position).getId());
//                openMap(adminProjects.get(position).getId());
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        user has access token
        if (tokenManager.getToken().getAccess_token() != null) {
            clientWithAuth.getUserInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new SingleObserver<UserInfo>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposables.add(d);
                        }

                        @Override
                        public void onSuccess(UserInfo user) {
                            logIn(user);
                            clientWithAuth.listProjects()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(projects -> displayProjects(projects));
                        }

                        @Override
                        public void onError(Throwable e) {
                            logOut();
                        }
                    });
        }
    }

    private void displayProjects(List<ProjectInfo> projects) {
        ArrayList<ProjectInfo> adminProjects = new ArrayList<ProjectInfo>();
        for (ProjectInfo project : projects) {
            if (project.getUser_info().is_admin())
                adminProjects.add(project);
        }
        recyclerView.setAdapter(new GeoKeyProjectAdapter(SettingsActivity.this, adminProjects));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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

    public void logIn(UserInfo user) {
        db.userDao().insertUserInfo(user);
//        MenuItem item = toolbar.getMenu().getItem(0);
//        item.setTitle(user.getDisplay_name());
    }

    public void openMap(int projectId) {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra(PROJECT_ID, projectId);
        startActivity(mapIntent);
    }

    public void getProject(int projectId) {
        Call<Project> call = clientWithAuth.getProject(projectId);
        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                Toast.makeText(SettingsActivity.this, response.body().getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "error :(", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}