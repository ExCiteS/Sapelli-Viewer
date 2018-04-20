package uk.ac.excites.ucl.sapelliviewer.datamodel;

import java.util.Date;

/**
 * Created by Julia on 19/02/2018.
 */

public class File {
    private int id;
    private String name;
    private String description;
    private String file_type;
    private boolean isowner;
    private String url;
    private String thumbnail_url;
    private UserInfo creator;
    private Date created_at;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFile_type() {
        return file_type;
    }

    public boolean isowner() {
        return isowner;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public Date getCreated_at() {
        return created_at;
    }
}
