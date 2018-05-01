package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        intent_settingsActivity = new Intent(this, SettingsActivity.class);
        tokenManager = TokenManager.getInstance();
        setContentView(R.layout.activity_login);

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
        Call<AccessToken> call = client.login(AccessToken.GRANT_TYPE_PASSWORD, username, password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    tokenManager.saveToken(response.body());
                    startActivity(intent_settingsActivity);
                    finish();
                } else {
                    if (response.code() == 404) {
                        errorText.setText(R.string.geokey_not_found);
                    } else if (response.code() == 401) {
                        errorText.setText(R.string.invalid_username_pw);
                    } else {
                        try {
                            errorText.setText(response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                errorText.setText(R.string.geokey_not_found);
            }
        });


    }


}

