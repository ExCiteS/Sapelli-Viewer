package uk.ac.excites.ucl.sapelliviewer.datamodel;

/**
 * Created by Julia on 13/02/2018.
 */

public class Login {
    private String grant_type;
    private String username;
    private String password;

    public Login() {
        this.grant_type = "refresh_token";
    }

    public Login(String user, String password) {
        this.grant_type = "password";
        this.username = user;
        this.password = password;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
