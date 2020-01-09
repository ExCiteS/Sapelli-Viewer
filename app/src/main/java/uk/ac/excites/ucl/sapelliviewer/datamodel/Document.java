package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Created by Julia on 19/02/2018.
 */
@Entity(foreignKeys = @ForeignKey(entity = Contribution.class, parentColumns = "id", childColumns = "contribution_id"))
public class Document {

    @PrimaryKey
    private int id;
    private int contribution_id;
    private String name;
    private String description;
    private String file_type;
    private boolean isowner;
    private String url;
    private String thumbnail_url;
    @Embedded
    private UserInfo creator;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Ignore
    private boolean active;

    @Ignore
    private Date created_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContribution_id() {
        return contribution_id;
    }

    public void setContribution_id(int contribution_id) {
        this.contribution_id = contribution_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public boolean isIsowner() {
        return isowner;
    }

    public void setIsowner(boolean isowner) {
        this.isowner = isowner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
