package uk.ac.excites.ucl.sapelliviewer.service;


import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionCollection;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;

/**
 * Created by Julia on 13/02/2018.
 */

public interface GeoKeyClient {

    @POST("/api/sapelli/login/")
    @FormUrlEncoded
    Single<AccessToken> login(@Field("grant_type") String grantType,
                              @Field("username") String username,
                              @Field("password") String password);


    @POST("/api/sapelli/login/")
    @FormUrlEncoded
    Call<AccessToken> refreshToken(@Field("grant_type") String grantType,
                                   @Field("refresh_token") String refreshToken);

    @GET("/api/user/")
    Single<UserInfo> getUserInfo();

    /* Get a list of all projects the authenticated user is allowed to access */
    @GET("/api/projects/")
    Observable<List<ProjectInfo>> listProjects();

    @GET("/api/projects/{projectId}/")
    Observable<Project> getProject(@Path("projectId") int projectID);

    @GET("/api/projects/{projectId}/contributions/")
    Observable<ContributionCollection> getContributions(@Path("projectId") int projectID);

    @Streaming
    @GET
    Observable<ResponseBody> downloadFileByUrl(@Url String fileUrl);

}
