package uk.ac.excites.ucl.sapelliviewer.datamodel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Contribution.class, parentColumns = "id", childColumns = "contributionId"),
        @ForeignKey(entity = Field.class, parentColumns = "id", childColumns = "fieldId")})
public class ContributionProperty {

    @PrimaryKey(autoGenerate = true)
    public int contribPropertyId;
    public int contributionId;
    public int fieldId;
    public String key;
    public String value;
    public String symbol;

    public ContributionProperty(int contributionId, int fieldId, String key, String value) {
        this.contributionId = contributionId;
        this.fieldId = fieldId;
        this.key = key;
        this.value = value;
    }

    public int getId() {
        return contribPropertyId;
    }

    public void setId(int id) {
        this.contribPropertyId = id;
    }

    public int getContributionId() {
        return contributionId;
    }

    public void setContributionId(int contributionId) {
        this.contributionId = contributionId;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
