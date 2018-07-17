package uk.ac.excites.ucl.sapelliviewer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;

public class BaseMapsActivity extends AppCompatActivity {
    protected AppDatabase db;
    protected int projectId;
    protected CompositeDisposable disposables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getAppDatabase(getApplicationContext());
        projectId = getIntent().getIntExtra(SettingsActivity.PROJECT_ID, 0);
        disposables = new CompositeDisposable();
    }

    public Single<List<Contribution>> getMarkers(int projectId) {
        return db.contributionDao().getContributions(projectId).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
