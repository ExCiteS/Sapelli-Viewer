package uk.ac.excites.ucl.sapelliviewer.service;

import android.content.Context;
import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

/* Executes GeoKey network calls and returns observables */
public class GeoKeyClient {

    private TokenManager tokenManager;
    private GeoKeyRequests geoKeyRequests;
    private AppDatabase db;


    public GeoKeyClient(Context context) {
        this.tokenManager = TokenManager.getInstance();
        this.geoKeyRequests = RetrofitBuilder.createServiceWithAuth(GeoKeyRequests.class, tokenManager);
        db = AppDatabase.getAppDatabase(context);

    }

    /* Retrieve list of projects from GeoKey and store in db */
    public Single<List<ProjectInfo>> updateProjects() {
        return geoKeyRequests.listProjects()
                .subscribeOn(Schedulers.io())
                .concatMap(Observable::fromIterable)
                .filter(projectInfo -> projectInfo.getUser_info().is_admin())
                .toList()
                .doOnSuccess(projectInfos -> {
                    db.projectInfoDao().clearProjectInfos();
                    db.projectInfoDao().insertProjectInfo(projectInfos);
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /* Retrieve entire project structure from GeoKey server and store in db */
    public Observable<ResponseBody> getProject(int projectID) {
        return geoKeyRequests.getProject(projectID)
                .subscribeOn(Schedulers.io())
                .doOnNext(project -> db.projectInfoDao().insertProject(project))
                .flatMap(project -> Observable.fromIterable(project.categories))
                .doOnNext(category -> {
                    category.setProjectid(projectID);
                    db.projectInfoDao().insertCategory(category);
                })
                .flatMap(category -> Observable.fromIterable(category.getFields())
                        .doOnNext(field -> {
                            field.setCategory_id(category.getId());
                            db.projectInfoDao().insertField(field);
                        })
                        .filter(field -> field.getLookupvalues() != null)
                        .flatMap(field -> Observable.fromIterable(field.getLookupvalues())
                                .doOnNext(lookUpValue -> {
                                    lookUpValue.setFieldId(field.getId());
                                    db.projectInfoDao().insertLookupValue(lookUpValue);
                                })
                                .filter(lookUpValue -> lookUpValue.getSymbol() != null)
                                .flatMap(lookUpValue -> geoKeyRequests.downloadFileByUrl(lookUpValue.getSymbol())
                                        .doOnNext(symbol -> MediaHelpers.writeFileToDisk(symbol, lookUpValue.getSymbol())))
                        )
                )
                .observeOn(AndroidSchedulers.mainThread());

    }


}
