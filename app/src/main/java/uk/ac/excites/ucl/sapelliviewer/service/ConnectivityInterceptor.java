package uk.ac.excites.ucl.sapelliviewer.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;

public class ConnectivityInterceptor implements Interceptor {

    private Context context;

    public ConnectivityInterceptor(Context context) {
        this.context = context;
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!isOnline(context)) {
            throw new NoConnectivityException();
        }

        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }
}
