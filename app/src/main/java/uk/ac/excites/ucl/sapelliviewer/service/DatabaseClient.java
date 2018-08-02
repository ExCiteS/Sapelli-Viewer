package uk.ac.excites.ucl.sapelliviewer.service;

import android.content.Context;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;

public class DatabaseClient {
    private AppDatabase db;

    public DatabaseClient(Context context) {
        db = AppDatabase.getAppDatabase(context);
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

}
