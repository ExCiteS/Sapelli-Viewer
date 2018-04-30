package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

/**
 * Created by Julia on 14/02/2018.
 */

@Entity
public class UserInfo {
    @PrimaryKey
    private int id;
    private String email;
    private String display_name;

    @Ignore
    private List<SocialAccount> social_accounts;

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public List<SocialAccount> getSocial_accounts() {
        return social_accounts;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
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
