package uk.ac.excites.ucl.sapelliviewer.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import uk.ac.excites.ucl.sapelliviewer.utils.GeoKeyException;

/**
 * Created by Julia on 21/02/2018.
 */

public class ErrorInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (!response.isSuccessful()) {
            throw new GeoKeyException(
                    response.code(),
                    response.message()
            );
        }
        return response;
    }
}
