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

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNum_media() {
        return num_media;
    }

    public void setNum_media(int num_media) {
        this.num_media = num_media;
    }

    public boolean isIsowner() {
        return isowner;
    }

    public void setIsowner(boolean isowner) {
        this.isowner = isowner;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public UserInfo getUpdator() {
        return updator;
    }

    public void setUpdator(UserInfo updator) {
        this.updator = updator;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public void setNum_comments(int num_comments) {
        this.num_comments = num_comments;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
