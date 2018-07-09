package uk.ac.excites.ucl.sapelliviewer.utils;

/**
 * Created by Julia on 13/02/2018.
 */

import android.content.SharedPreferences;

import uk.ac.excites.ucl.sapelliviewer.activities.LoginActivity;
import uk.ac.excites.ucl.sapelliviewer.datamodel.AccessToken;

public class TokenManager {
    private final SharedPreferences prefs;

    private static TokenManager INSTANCE = null;

    private TokenManager(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static TokenManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TokenManager();
        }
        return INSTANCE;
    }

    private TokenManager() {
        prefs = LoginActivity.get().getSharedPreferences("prefs", 0);
    }

    public void saveToken(AccessToken token) {
        prefs.edit().putString("ACCESS_TOKEN", token.getAccess_token()).commit();
        prefs.edit().putString("REFRESH_TOKEN", token.getRefresh_token()).commit();
    }

    public void deleteToken() {
        prefs.edit().remove("ACCESS_TOKEN").commit();
        prefs.edit().remove("REFRESH_TOKEN").commit();
    }


    public AccessToken getToken() {
        AccessToken token = new AccessToken();
        token.setAccess_token(prefs.getString("ACCESS_TOKEN", null));
        token.setRefresh_token(prefs.getString("REFRESH_TOKEN", null));
        return token;
    }

    public void saveServerUrl(String url) {
        prefs.edit().putString("SERVER_URL", url).commit();
    }

    public String getServerUrl() {
        return prefs.getString("SERVER_URL", null);
    }

    public void deleteServerUrl() {
        prefs.edit().remove("SERVER_URL").commit();
    }

    public void saveActiveProject(int id) {
        prefs.edit().putInt("ACTIVE_PROJECT", id).commit();
    }

    public int getActiveProject() {
        return prefs.getInt("ACTIVE_PROJECT", -1);
    }

    public void deleteActiveProject() {
        prefs.edit().remove("ACTIVE_PROJECT").commit();
    }
}
