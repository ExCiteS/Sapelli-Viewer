package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by Julia on 13/02/2018.
 */

@Entity
public class ProjectInfo {
    @PrimaryKey
    private int id;
    private String name;
    private String description;
    @Ignore
    private int contributionCount;
    @Ignore
    private int mediaCount;

    @Ignore
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Embedded
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

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser_info(UserPrivlg user_info) {
        this.user_info = user_info;
    }

    public int getContributionCount() {
        return contributionCount;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setContributionCount(int contributionCount) {
        this.contributionCount = contributionCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

}
