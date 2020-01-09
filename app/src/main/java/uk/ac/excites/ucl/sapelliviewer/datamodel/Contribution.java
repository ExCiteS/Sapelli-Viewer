package uk.ac.excites.ucl.sapelliviewer.datamodel;


import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


import java.util.HashMap;
import java.util.List;

/**
 * Created by Julia on 18/02/2018.
 */
@Entity
public class Contribution {
    @PrimaryKey
    private int id;
    private int projectId;
    private int categoryId;
    private int displayFieldId;
    private String displayFieldKey;

    @Ignore
    private Category category;

    @Embedded
    private Geometry geometry;
    @Ignore
    private HashMap<String, String> properties;

    @Ignore
    private List<ContributionProperty> contributionProperties;

    @Ignore // Ignore for now, might need later
    private Meta meta;

    public int getId() {
        return id;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }


    public Meta getMeta() {
        return meta;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }


    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int category) {
        this.categoryId = category;
    }

    public int getDisplayFieldId() {
        return displayFieldId;
    }

    public void setDisplayFieldId(int displayFieldId) {
        this.displayFieldId = displayFieldId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setContributionproperties(List<ContributionProperty> contributionproperties) {
        this.contributionProperties = contributionproperties;
    }

    public List<ContributionProperty> getContributionProperties() {
        return contributionProperties;
    }

    public String getDisplayFieldKey() {
        return displayFieldKey;
    }

    public void setDisplayFieldKey(String displayFieldKey) {
        this.displayFieldKey = displayFieldKey;
    }
}
