package uk.ac.excites.ucl.sapelliviewer.service;


import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionCollection;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

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
    Single<List<ProjectInfo>> listProjects();

    @GET("/api/projects/{projectId}/")
    Call<Project> getProject(@Path("projectId") int projectID);

    @GET("/api/projects/{projectId}/contributions/")
    Call<ContributionCollection> getContributions(@Path("projectId") int projectID);

}
