package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.sql.Timestamp;

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
    private
    Geometry geometry;

    public Logs(int projectId, String time, String event, Integer interactionId, double scale, Geometry geometry) {
        this.projectId = projectId;
        this.time = time;
        this.event = event;
        this.interactionId = interactionId;
        this.scale = scale;
        this.geometry = geometry;
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
}
