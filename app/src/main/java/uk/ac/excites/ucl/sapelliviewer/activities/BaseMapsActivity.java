package uk.ac.excites.ucl.sapelliviewer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
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

    public Single<List<Contribution>> getContributions(int projectId) {
        return db.contributionDao().getContributions(projectId).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Category>> getCategories(int projectId) {
        return db.projectInfoDao().getCategories(projectId).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Field>> getFields(int projectId) {
        return db.projectInfoDao().getFieldsByProject(projectId)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }


    public Single<List<LookUpValue>> getLookUpValues(int projectId) {
        return db.projectInfoDao().getLookupValueByProject(projectId).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<LookUpValue>> getLookUpValuesByField(int fieldId) {
        return db.projectInfoDao().getLookupValueByField(fieldId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Contribution>> getContributionsByValues(List<Integer> valueIDs) {
        return db.contributionDao().getContributionsByValues(valueIDs)
                .subscribeOn(Schedulers.io());
    }

    public Maybe<List<Contribution>> getContributionsByValue(int valueID) {
        return db.contributionDao().getContributionsByValue(valueID).filter(contributions -> !contributions.isEmpty())
                .subscribeOn(Schedulers.io());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
