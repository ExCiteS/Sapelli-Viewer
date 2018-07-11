package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ProjectProperties {
    @PrimaryKey
    private int id;
    private String mapPath;

    public ProjectProperties(int id, String mapPath) {
        this.id = id;
        this.mapPath = mapPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(String mapPath) {
        this.mapPath = mapPath;
    }
}
