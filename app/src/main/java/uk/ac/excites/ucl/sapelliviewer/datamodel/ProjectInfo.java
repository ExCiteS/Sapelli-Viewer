package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by Julia on 13/02/2018.
 */

@Entity
public class ProjectInfo implements Serializable {
    @PrimaryKey
    private int id;
    private String name;
    private String description;
    @Ignore
    private int contributionCount;
    @Ignore
    private int mediaCount;
    private boolean isRemote;
    @Ignore
    private boolean active;
    @Embedded
    private UserPrivlg user_info;

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserPrivlg getUser_info() {
        return user_info;
    }

    public void setUser_info(UserPrivlg user_info) {
        this.user_info = user_info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContributionCount() {
        return contributionCount;
    }

    public void setContributionCount(int contributionCount) {
        this.contributionCount = contributionCount;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

}
