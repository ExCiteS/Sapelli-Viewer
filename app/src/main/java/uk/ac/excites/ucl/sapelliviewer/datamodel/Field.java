package uk.ac.excites.ucl.sapelliviewer.datamodel;

import java.util.List;

/**
 * Created by Julia on 19/02/2018.
 */

public class Field {
    private int id;
    private String name;
    private String description;
    private String key;
    private boolean required;
    private String fieldtype;
    private double minval;
    private double maxval;
    private boolean textarea;
    private double maxlength;
    private List<LookUpValue> lookupvalues;
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
