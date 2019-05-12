package uk.ac.excites.ucl.sapelliviewer.service;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

/**
 * Created by Julia on 13/02/2018.
 */

public class RefreshAuthenticator implements Authenticator {

    private TokenManager tokenManager;
    private static RefreshAuthenticator INSTANCE;

    private RefreshAuthenticator(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    static synchronized RefreshAuthenticator getInstance(TokenManager tokenManager) {
        if (INSTANCE == null) {
            INSTANCE = new RefreshAuthenticator(tokenManager);
        }

        return INSTANCE;
    }


    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) >= 3) {
            return null;
        }

        AccessToken token = tokenManager.getToken();

        GeoKeyRequests service = RetrofitBuilder.createService(GeoKeyRequests.class, tokenManager.getServerUrl());
        Call<AccessToken> call = service.refreshToken(AccessToken.GRANT_TYPE_REFRESH_TOKEN, token.getRefresh_token());
        retrofit2.Response<AccessToken> res = call.execute();

        if (res.isSuccessful()) {
            AccessToken newToken = res.body();
            tokenManager.saveToken(newToken);

            return response.request().newBuilder().header("Authorization", "Bearer " + res.body().getAccess_token()).build();
        } else {
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
