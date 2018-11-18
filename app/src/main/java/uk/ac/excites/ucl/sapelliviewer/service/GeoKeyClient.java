package uk.ac.excites.ucl.sapelliviewer.service;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectProperties;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

/* Executes GeoKey network calls and returns observables */
public class GeoKeyClient {

    private GeoKeyRequests geoKeyRequests;
    private AppDatabase db;

    /* keep variables in memory to reduce db write operations */
//    private List<Field> fields = new ArrayList<>();
    //    private List<Contribution> contributionList = new ArrayList<>();
//    private List<LookUpValue> lookUpValues = new ArrayList<>();
    //    private List<ContributionProperty> contributionProperties = new ArrayList<>();
//    private List<Document> mediaFiles = new ArrayList<>();


    public GeoKeyClient(Context context) {
        TokenManager tokenManager = TokenManager.getInstance();
        this.geoKeyRequests = RetrofitBuilder.createServiceWithAuth(GeoKeyRequests.class, tokenManager);
        db = AppDatabase.getAppDatabase(context);

    }

    /* Retrieve list of projects from GeoKey and store in db */
    public Single<List<ProjectInfo>> updateProjects() {
        return geoKeyRequests.listProjects()
                .subscribeOn(Schedulers.io())
                .concatMap(Observable::fromIterable)
                .filter(projectInfo -> projectInfo.getUser_info().is_admin())
                .doOnNext(projectInfo -> db.projectInfoDao().insertProjectProperties(new ProjectProperties(projectInfo.getId())))
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
                                        .doOnNext(symbol -> MediaHelpers.writeFileToDisk(symbol, lookUpValue.getSymbol()))
                                )
                        )
                );
    }

    private Observable<Contribution> getContributions(int projectID) {
        return geoKeyRequests.getContributions(projectID)
                .subscribeOn(Schedulers.io())
                .flatMap(contributionCollection -> Observable.fromIterable(contributionCollection.getFeatures())
                        .doOnNext(contribution -> contribution.setProjectId(projectID))
                );
    }

    @SuppressLint("CheckResult")
    public Observable<ContributionProperty> getContributionsWithProperties(int projectID) {
        return getContributions(projectID)
                .doOnNext(contribution -> {
                    contribution.getContributionProperty().setFieldId(db.projectInfoDao().getFieldByKey(contribution.getContributionProperty().getKey(), contribution.getCategoryId()).getId());
                    db.contributionDao().insertContribution(contribution);
                })
                .flatMap(contribution -> Observable.fromIterable(contribution.getContributionProperties())
                        .doOnNext(contributionProperty -> {
                            contributionProperty.setFieldId(db.projectInfoDao().getFieldByKey(contributionProperty.getKey(), contribution.getCategoryId()).getId());
                            db.contributionDao().insertContributionProperties(contributionProperty);
                        })
                );
    }

    public Observable<ResponseBody> getMedia(int projectID) {
        return getContributions(projectID)
                .flatMap(contribution -> geoKeyRequests.getMedia(projectID, contribution.getId())
                        .flatMap(documents -> Observable.fromIterable(documents)
                                .filter(mediaFile -> !mediaFile.getFile_type().equals("VideoFile")) // Don't handle video files for now
                                .doOnNext(mediaFile -> {
                                    mediaFile.setContribution_id(contribution.getId());
                                    db.contributionDao().insertMediaFile(mediaFile);
                                })
                                .filter(mediaFile -> !(new File(MediaHelpers.dataPath + mediaFile.getUrl()).exists()))
                                .flatMap(mediaFile -> geoKeyRequests.downloadFileByUrl(mediaFile.getUrl())
                                        .doOnNext(symbol -> MediaHelpers.writeFileToDisk(symbol, mediaFile.getUrl())))
                        )
                );
    }
}
