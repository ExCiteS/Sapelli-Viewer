package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Logs {

    @PrimaryKey(autoGenerate = true)
    int id;
    private int projectId;
    private String time;
    private String event;
    private Integer interactionId;
    private double scale;
    @Embedded
    private Geometry geometry;
    private String mapPath;
    private String upDirection;

    public Logs(int projectId, String time, String event, Integer interactionId, double scale, Geometry geometry, String mapPath, String upDirection) {
        this.projectId = projectId;
        this.time = time;
        this.event = event;
        this.interactionId = interactionId;
        this.scale = scale;
        this.geometry = geometry;
        this.mapPath = mapPath;
        this.upDirection = upDirection;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Integer getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Integer interactionId) {
        this.interactionId = interactionId;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(String mapPath) {
        this.mapPath = mapPath;
    }

    public String getUpDirection() {
        return upDirection;
    }

    public void setUpDirection(String upDirection) {
        this.upDirection = upDirection;
    }
}
