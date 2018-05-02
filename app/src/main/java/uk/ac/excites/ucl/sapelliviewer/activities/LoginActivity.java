package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.UnknownHostException;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
import uk.ac.excites.ucl.sapelliviewer.utils.NoConnectivityException;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;
import uk.ac.excites.ucl.sapelliviewer.utils.Validator;

/**
 * A login screen that offers login via email/password. TODO: convert network calls to RxJava
 */
public class LoginActivity extends AppCompatActivity {

    private static LoginActivity application;

    private EditText server_url;
    private EditText userName;
    private EditText password;
    private TextView errorText;
    private TokenManager tokenManager;
    private Intent intent_settingsActivity;
    private CompositeDisposable disposables;
    private AppDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        intent_settingsActivity = new Intent(this, SettingsActivity.class);
        tokenManager = TokenManager.getInstance();
        disposables = new CompositeDisposable();
        setContentView(R.layout.activity_login);
        db = AppDatabase.getAppDatabase(getApplicationContext());


        server_url = (EditText) findViewById(R.id.textUrl);
        userName = (EditText) findViewById(R.id.editUsername);
        password = (EditText) findViewById(R.id.editPassword);
        errorText = (TextView) findViewById(R.id.text_error);
        final Button loginButton = (Button) findViewById(R.id.buttonLogin);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean inputValid = Validator.isValid(server_url, Validator.URL) && Validator.isValid(userName, Validator.EMAIL);
                if (inputValid)
                    login(server_url.getText().toString().trim(), userName.getText().toString().trim(), password.getText().toString());
                errorText.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager != null && tokenManager.getToken().getAccess_token() != null) {
            startActivity(intent_settingsActivity);
            finish();
        }
    }

    public static LoginActivity get() {
        return application;
    }

    public void login(String url, String username, String password) {
        tokenManager.saveServerUrl(url);
        GeoKeyClient client = RetrofitBuilder.createService(GeoKeyClient.class, url);
        disposables.add(
                client.login(AccessToken.GRANT_TYPE_PASSWORD, username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<AccessToken>() {
                            @Override
                            public void onSuccess(AccessToken accessToken) {
                                tokenManager.saveToken(accessToken);
                                updateUser();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e instanceof UnknownHostException) {
                                    errorText.setText(R.string.geokey_not_found);
                                } else if (e instanceof NoConnectivityException) {
                                    errorText.setText(R.string.no_internet);
                                } else {
                                    int errorCode = ((HttpException) e).code();
                                    if (errorCode == 404) {
                                        errorText.setText(R.string.geokey_not_found);
                                    } else if (errorCode == 401) {
                                        errorText.setText(R.string.invalid_username_pw);
                                    } else {
                                        errorText.setText(((HttpException) e).message());
                                    }
                                }
                            }
                        }));


    }

    public void updateUser() {
        GeoKeyClient clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyClient.class, tokenManager);
        disposables.add(
                clientWithAuth.getUserInfo()
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableSingleObserver<UserInfo>() {
                            @Override
                            public void onSuccess(UserInfo user) {
                                db.userDao().clearPrevUser();
                                db.userDao().insertUserInfo(user);
                                startActivity(intent_settingsActivity);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                //logout
                            }
                        })
        );


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}




