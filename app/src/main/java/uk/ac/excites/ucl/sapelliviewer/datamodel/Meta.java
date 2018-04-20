package uk.ac.excites.ucl.sapelliviewer.datamodel;

/**
 * Created by Julia on 19/02/2018.
 */

public class Meta {
    private String status;
    private int num_media;
    private boolean isowner;
    private int version;
    private UserInfo updator;
    private UserInfo creator;
    private int num_comments;
    private String created_at;
    private Category category;
    private String updated_at;

    public String getStatus() {
        return status;
    }

    public int getNum_media() {
        return num_media;
    }

    public boolean isowner() {
        return isowner;
    }

    public int getVersion() {
        return version;
    }

    public UserInfo getUpdator() {
        return updator;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Category getCategory() {
        return category;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
