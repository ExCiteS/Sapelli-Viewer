package uk.ac.excites.ucl.sapelliviewer.datamodel;

/**
 * Created by Julia on 13/02/2018.
 */

public class ProjectInfo {
    private int id;
    private String name;
    private String description;
    private UserPrivlg user_info;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public UserPrivlg getUser_info() {
        return user_info;
    }

    public String getName() {
        return name;
    }


}
