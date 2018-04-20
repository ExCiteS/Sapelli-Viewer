package uk.ac.excites.ucl.sapelliviewer.datamodel;

import java.util.Date;
import java.util.List;

/**
 * Created by Julia on 19/02/2018.
 */

public class Comment {
    private int id;
    private String text;
    private UserInfo creator;
    private int responds_to;
    private Date created_at;
    private List<Comment> responses;
    private String review_status;

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public int getResponds_to() {
        return responds_to;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public List<Comment> getResponses() {
        return responses;
    }

    public String getReview_status() {
        return review_status;
    }
}
