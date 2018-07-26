package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Julia on 19/02/2018.
 */
@Entity(foreignKeys = @ForeignKey(entity = Field.class, parentColumns = "id", childColumns = "fieldId"))
public class LookUpValue {
    @PrimaryKey
    private int id;
    private int fieldId;
    private String name;
    private String symbol;
    @Ignore
    private boolean active = true;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
