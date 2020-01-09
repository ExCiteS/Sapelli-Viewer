package uk.ac.excites.ucl.sapelliviewer.datamodel;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

/**
 * Created by Julia on 19/02/2018.
 */

@Entity()
public class Field {
    @PrimaryKey
    private int id;
    private int category_id;
    private String name;
    private String description;
    private String key;
    private boolean required;
    private String fieldtype;
    private double minval;
    private double maxval;
    private boolean textarea;
    private double maxlength;
    @Ignore
    private boolean active = true;
    @Ignore
    private List<LookUpValue> lookupvalues;
    private int order;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public double getMinval() {
        return minval;
    }

    public void setMinval(double minval) {
        this.minval = minval;
    }

    public double getMaxval() {
        return maxval;
    }

    public void setMaxval(double maxval) {
        this.maxval = maxval;
    }

    public boolean isTextarea() {
        return textarea;
    }

    public void setTextarea(boolean textarea) {
        this.textarea = textarea;
    }

    public double getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(double maxlength) {
        this.maxlength = maxlength;
    }

    public List<LookUpValue> getLookupvalues() {
        return lookupvalues;
    }

    public void setLookupvalues(List<LookUpValue> lookupvalues) {
        this.lookupvalues = lookupvalues;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
