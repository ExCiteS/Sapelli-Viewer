package uk.ac.excites.ucl.sapelliviewer.datamodel;

//import com.github.filosganga.geogson.model.Polygon;


import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.text.format.DateUtils;

import com.google.maps.android.data.Geometry;

import java.util.Date;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.db.GeoKeyTypeConverters;
import uk.ac.excites.ucl.sapelliviewer.db.ProjectInfoDao;

/**
 * Created by Julia on 13/02/2018.
 */
@Entity
public class Project {
    @PrimaryKey
    public int id;
    public String name;
    public String description;
    @Ignore
    public String created_at; //TODO: HANDLE DATES PROPERLY
    @Ignore
    public List<Subset> subsets;
    @Ignore
    public List<Category> categories;
    @Embedded
    public UserPrivlg user_info;
    @Embedded
    public Extent geographic_extent; //TODO: HANDLE EXTENT

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSubsets(List<Subset> subsets) {
        this.subsets = subsets;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setUser_info(UserPrivlg user_info) {
        this.user_info = user_info;
    }

    public void setGeographic_extent(Extent geographic_extent) {
        this.geographic_extent = geographic_extent;
    }

    @Entity
    public class Extent {
        public String type;

        @TypeConverters(GeoKeyTypeConverters.class)
        public double[][][] coordinates;

        public String getType() {
            return type;
        }

        public double[][] getCoordinates() {
            return coordinates[0];
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setCoordinates(double[][][] coordinates) {
            this.coordinates = coordinates;
        }
    }
}
