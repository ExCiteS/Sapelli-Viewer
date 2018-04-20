package uk.ac.excites.ucl.sapelliviewer.datamodel;

import java.util.List;

/**
 * Created by Julia on 14/02/2018.
 */

public class UserInfo {
    private int id;
    private String email;
    private String display_name;
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
