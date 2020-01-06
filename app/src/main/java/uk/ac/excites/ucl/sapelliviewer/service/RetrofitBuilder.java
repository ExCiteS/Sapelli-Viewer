package uk.ac.excites.ucl.sapelliviewer.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.excites.ucl.sapelliviewer.BuildConfig;
import uk.ac.excites.ucl.sapelliviewer.activities.LoginActivity;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

/**
 * Created by Julia on 13/02/2018.
 */

public class RetrofitBuilder {

    private final static OkHttpClient client = buildClient();
    private final static Retrofit retrofit = buildRetrofit(client);

    private static OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder.build();

    }

    private static Retrofit buildRetrofit(OkHttpClient client) {
        TokenManager token = TokenManager.getInstance();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Geometry.class, new GeometryDeserializer())
                .registerTypeAdapter(Contribution.class, new ContributionDeserializer())
                .create();

        return new Retrofit.Builder()
                .baseUrl(token.getServerUrl())
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static <T> T createService(Class<T> service, String url) {
        OkHttpClient newClient = client.newBuilder().addInterceptor(new ConnectivityInterceptor(LoginActivity.get().getApplicationContext()))
                .build();
        Retrofit newRetrofit = buildRetrofit(newClient).newBuilder().baseUrl(url).build();
        return newRetrofit.create(service);
    }

    public static <T> T createServiceWithAuth(Class<T> service, final TokenManager tokenManager) {

        OkHttpClient.Builder builder = client.newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder builder = request.newBuilder();
                        if (tokenManager.getToken().getAccess_token() != null) {
                            builder.addHeader("Authorization", "Bearer " + tokenManager.getToken().getAccess_token());
                        }

                        request = builder.build();
                        return chain.proceed(request);
                    }
                }).authenticator(RefreshAuthenticator.getInstance(tokenManager))
                .addInterceptor(new ConnectivityInterceptor(LoginActivity.get().getApplicationContext()));

        if (BuildConfig.DEBUG) {
            Class clazz;
            Interceptor chuckerInterceptor;
            try {
                clazz = Class.forName("com.chuckerteam.chucker.api.ChuckerInterceptor");
                Constructor constructor = clazz.getConstructor(Context.class);
                Object obj = constructor.newInstance(LoginActivity.get().getApplicationContext());
                chuckerInterceptor = (Interceptor) obj;
                builder.addInterceptor(chuckerInterceptor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Retrofit newRetrofit = retrofit.newBuilder().client(builder.build()).baseUrl(TokenManager.getInstance().getServerUrl()).build();
        return newRetrofit.create(service);
    }
}