package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

/**
 * Created by Julia on 14/02/2018.
 */

@Entity
public class UserInfo {
    @PrimaryKey
    private int user_id;
    private String email;
    private String display_name;

    @Ignore
    private List<SocialAccount> social_accounts;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public List<SocialAccount> getSocial_accounts() {
        return social_accounts;
    }

    public void setSocial_accounts(List<SocialAccount> social_accounts) {
        this.social_accounts = social_accounts;
    }

    private class SocialAccount {
        private int id;
        private String provider;

        public int getId() {
            return id;
        }

        public String getProvider() {
            return provider;
        }
    }
}
