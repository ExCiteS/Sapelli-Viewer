package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;

/**
 * Created by Julia on 13/02/2018.
 */

@Entity
public class UserPrivlg {
    private boolean is_involved;
    private boolean can_moderate;
    private boolean is_admin;
    private boolean can_contribute;

    public boolean is_involved() {
        return is_involved;
    }

    public boolean can_moderate() {
        return can_moderate;
    }

    public boolean is_admin() {
        return is_admin;
    }

    public boolean can_contribute() {
        return can_contribute;
    }

    public void setIs_involved(boolean is_involved) {
        this.is_involved = is_involved;
    }

    public void setCan_moderate(boolean can_moderate) {
        this.can_moderate = can_moderate;
    }

    public void setIs_admin(boolean is_admin) {
        this.is_admin = is_admin;
    }

    public void setCan_contribute(boolean can_contribute) {
        this.can_contribute = can_contribute;
    }
}