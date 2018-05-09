package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

/**
 * Created by Julia on 19/02/2018.
 */

@Entity(foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id"))
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
    private List<LookUpValue> lookupvalues;  // TODO: IMPLEMENT LOOKUPVALuES
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

    public String getKey() {
        return key;
    }

    public boolean isRequired() {
        return required;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public double getMinval() {
        return minval;
    }

    public double getMaxval() {
        return maxval;
    }

    public boolean isTextarea() {
        return textarea;
    }

    public double getMaxlength() {
        return maxlength;
    }

    public List<LookUpValue> getLookupvalues() {
        return lookupvalues;
    }

    public int getOrder() {
        return order;
    }
}
