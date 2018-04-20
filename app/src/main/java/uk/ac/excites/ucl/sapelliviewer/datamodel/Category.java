package uk.ac.excites.ucl.sapelliviewer.datamodel;

import java.util.Date;
import java.util.List;

/**
 * Created by Julia on 13/02/2018.
 */

public class Category {
    private int id;
    private String name;
    private String description;
    private Date created_at;
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
}
