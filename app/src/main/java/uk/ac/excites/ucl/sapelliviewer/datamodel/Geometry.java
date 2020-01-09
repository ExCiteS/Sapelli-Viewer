package uk.ac.excites.ucl.sapelliviewer.datamodel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Geometry {

    @PrimaryKey(autoGenerate = true)
    public int geometryId;
    public String type;
    public String coordinates;

    public Geometry(String type, String coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }


}
