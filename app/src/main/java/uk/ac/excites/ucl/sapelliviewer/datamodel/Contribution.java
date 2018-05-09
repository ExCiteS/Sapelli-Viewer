package uk.ac.excites.ucl.sapelliviewer.datamodel;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.maps.android.data.Geometry;

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
//    private JSONObject getProperties;
    private Geometry geometry;
    private HashMap properties;
    private DisplayField display_field;
    private String expiry_field;
    private Meta meta;
    @Ignore
    private List<Comment> comments;
    private List<File> media;
    @Ignore
    private List<Comment> review_comments;

    public int getId() {
        return id;
    }

    public HashMap getProperties() {
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
}
