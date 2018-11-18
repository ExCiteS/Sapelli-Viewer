package uk.ac.excites.ucl.sapelliviewer.service;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;

public class ContributionDeserializer implements JsonDeserializer<Contribution> {
    @Override
    public Contribution deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Contribution contribution = new Contribution();
        JsonObject jsonObject = (JsonObject) json;
        contribution.setId(jsonObject.get("id").getAsInt());
        contribution.setGeometry(new GeometryDeserializer().deserialize(jsonObject.get("geometry").getAsJsonObject()));

        String displayFieldKey = extractString(jsonObject.get("display_field").getAsJsonObject().get("key"));
        String displayFieldValue = extractString(jsonObject.get("display_field").getAsJsonObject().get("value"));
        ContributionProperty displayField = new ContributionProperty(contribution.getId(), 0, displayFieldKey, displayFieldValue);
        contribution.setContributionProperty(displayField);

        Category category = new Category();
        JsonObject categoryJson = jsonObject.get("meta").getAsJsonObject().get("category").getAsJsonObject();
        category.setId(extractInt(categoryJson.get("id")));
        category.setName(extractString(categoryJson.get("name")));
        category.setColour(extractString(categoryJson.get("colour")));
        category.setSymbol(extractString(categoryJson.get("symbol")));
        category.setOrder(extractInt(categoryJson.get("order")));
        category.setDescription(extractString(categoryJson.get("description")));

        List<ContributionProperty> contributionProperties = new ArrayList<>();
        for (String propertyKey : jsonObject.get("properties").getAsJsonObject().keySet()) {
            int contributionId = contribution.getId();
            String value = jsonObject.get("properties").getAsJsonObject().get(propertyKey).getAsString();
            contributionProperties.add(new ContributionProperty(contributionId, 0, propertyKey, value));
        }
        contribution.setContributionproperties(contributionProperties);


        contribution.setCategory(category);
        contribution.setCategoryId(category.getId());

        return contribution;

    }

    private String extractString(JsonElement jsonElement) {
        try {
            return jsonElement.getAsString();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private int extractInt(JsonElement jsonElement) {
        try {
            return jsonElement.getAsInt();
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
}
