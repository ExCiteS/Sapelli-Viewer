package uk.ac.excites.ucl.sapelliviewer.datamodel;

//import com.github.filosganga.geogson.model.Polygon;


import java.util.Date;
import java.util.List;

/**
 * Created by Julia on 13/02/2018.
 */

public class Project {
    public int id;
    public String name;
    public String description;
    public Date created_at;
    public List<Subset> subsets;
    public List<Category> categories;
    public UserPrivlg user_info;
    public Extent geographic_extent;

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

    public List<Subset> getSubsets() {
        return subsets;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public UserPrivlg getUser_info() {
        return user_info;
    }

    public Extent getGeographic_extent() {
        return geographic_extent;
    }

    //
    private class Extent {
        public String type;
        double[][][] coordinates;

        public String getType() {
            return type;
        }

        public double[][] getCoordinates() {
            return coordinates[0];
        }
    }
}
