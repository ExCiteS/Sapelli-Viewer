package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ProjectProperties {
    @PrimaryKey
    private int id;
    private String mapPath;
    private boolean logging;
    private String showFields = "none";
    private String upDirection = "north"; // default value

    public ProjectProperties(int id) {
        this.id = id;
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

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public String getShowFields() {
        return showFields;
    }

    public void setShowFields(String showFields) {
        this.showFields = showFields;
    }

    public String getUpDirection() {
        return upDirection;
    }

    public void setUpDirection(String upDirection) {
        this.upDirection = upDirection;
    }


}
