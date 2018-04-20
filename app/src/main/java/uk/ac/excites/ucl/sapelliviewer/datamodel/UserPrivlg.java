package uk.ac.excites.ucl.sapelliviewer.datamodel;

/**
 * Created by Julia on 13/02/2018.
 */

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
}