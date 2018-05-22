package uk.ac.excites.ucl.sapelliviewer.datamodel;


import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Julia on 18/02/2018.
 */
@Entity
public class Contribution {
    @PrimaryKey
    private int id;
    private int projectId;
    private int category;
    @Embedded
    private Geometry geometry;
    @Ignore // TODO: insert properly
    private HashMap<String, String> properties;
    @Ignore // TODO: insert properly
    private DisplayField display_field;
    private String expiry_field;
    @Ignore // Ingore for now, might need later
    private Meta meta;
    @Ignore
    private List<Comment> comments;
    @Ignore // TODO: insert properly
    private List<File> media;
    @Ignore
    private List<Comment> review_comments;

    public int getId() {
        return id;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public DisplayField getDisplay_field() {
        return display_field;
    }

    public String getExpiry_field() {
        return expiry_field;
    }

    public Meta getMeta() {
        return meta;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<File> getMedia() {
        return media;
    }

    public List<Comment> getReview_comments() {
        return review_comments;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void setDisplay_field(DisplayField display_field) {
        this.display_field = display_field;
    }

    public void setExpiry_field(String expiry_field) {
        this.expiry_field = expiry_field;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setMedia(List<File> media) {
        this.media = media;
    }

    public void setReview_comments(List<Comment> review_comments) {
        this.review_comments = review_comments;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
