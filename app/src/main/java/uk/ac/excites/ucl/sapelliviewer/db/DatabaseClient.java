package uk.ac.excites.ucl.sapelliviewer.db;

import android.content.Context;

import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Logs;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;

public class DatabaseClient {
    private final Logger logger;
    private final int projectId;
    private final MapView mapView;
    private AppDatabase db;
    private boolean pendingRotation;
    private double scale;

    public DatabaseClient(Context context, int projectId, MapView mapView) {
        this.db = AppDatabase.getAppDatabase(context);
        this.logger = new Logger();
        this.projectId = projectId;
        this.mapView = mapView;
    }

    public Single<List<Field>> getFields() {
        return db.projectInfoDao().getFieldsByProject(projectId)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<LookUpValue>> getLookUpValues() {
        return db.projectInfoDao().getLookupValueByProject(projectId).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Contribution>> getContributionsByValues(List<Integer> valueIDs) {
        return db.contributionDao().getContributionsByValues(valueIDs)
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Contribution>> loadMarkers(List<LookUpValue> lookUpValuesToDisplay) {
        return Observable.just(lookUpValuesToDisplay)
                .flatMap(Observable::fromIterable)
                .map(LookUpValue::getId)
                .toList()
                .flatMap(this::getContributionsByValues);
    }

    public void insertLog(String event) {
        Logs log = logger.log(projectId, event, null, mapView);
        Completable.fromAction(() -> db.projectInfoDao().insertLog(log)).subscribeOn(Schedulers.io()).subscribe();
    }

    public void insertLog(String event, int interactionId) {
        Logs log = logger.log(projectId, event, interactionId, mapView);
        Completable.fromAction(() -> db.projectInfoDao().insertLog(log)).subscribeOn(Schedulers.io()).subscribe();
    }


    public void setPendingRotation(boolean pending) {
        this.pendingRotation = pending;
    }

    public boolean isPendingotation() {
        return this.pendingRotation;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return this.scale;
    }


}
