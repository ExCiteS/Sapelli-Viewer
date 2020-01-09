package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

/**
 * Created by Julia on 13/02/2018.
 */

@Entity()
public class Category {
    @PrimaryKey
    private int id;
    private int projectid;
    private String name;
    private String description;
    @Ignore
    private Date created_at; //TODO: Handle dates properly
    @Ignore
    private List<Field> fields;
    private String colour;
    private String symbol;
    private int order;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getColour() {
        return colour;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getOrder() {
        return order;
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

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getProjectid() {
        return projectid;
    }

    public void setProjectid(int projectid) {
        this.projectid = projectid;
    }
}
