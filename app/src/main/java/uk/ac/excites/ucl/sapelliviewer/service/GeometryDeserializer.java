package uk.ac.excites.ucl.sapelliviewer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry;

/**
 * Created by Julia on 20/02/2018.
 */

public class GeometryDeserializer implements JsonDeserializer<Geometry> {
    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = (JsonObject) json;
        String type = jsonObject.get("type").getAsString();
        String coordinates = extractCoords(jsonObject.get("coordinates"), type);
        return new Geometry(type, coordinates);
    }

    private String extractCoords(JsonElement coordinates, String type) {
        JsonArray coordArray;
        StringBuilder result = new StringBuilder();
        switch (type) {
            case "Point":
                coordArray = coordinates.getAsJsonArray();
                result.append("[").append(coordArray.get(0).toString()).append(", ").append(coordArray.get(1).toString()).append("]");

                break;
            case "LineString":
                coordArray = coordinates.getAsJsonArray();
                result.append("[");
                for (int i = 0; i < coordArray.size(); i++) {
                    result.append("[").append(coordArray.get(i).getAsJsonArray().get(0)).append(", ").append(coordArray.get(i).getAsJsonArray().get(1)).append("],");
                }
                result = new StringBuilder(result.substring(0, result.length() - 1));
                result.append("]");

                break;
            default:
                coordArray = (JsonArray) coordinates.getAsJsonArray().get(0);
                result.append("[");
                for (int i = 0; i < coordArray.size(); i++) {
                    result.append("[").append(coordArray.get(i).getAsJsonArray().get(0)).append(", ").append(coordArray.get(i).getAsJsonArray().get(1)).append("],");
                }
                result = new StringBuilder(result.substring(0, result.length() - 1));
                result.append("]");
                break;
        }
        return result.toString();

    }

    public Geometry deserialize(JsonElement geometry) {
        JsonObject jsonObject = (JsonObject) geometry;
        String type = jsonObject.get("type").getAsString();
        String coordinates = extractCoords(jsonObject.get("coordinates"), type);
        return new Geometry(type, coordinates);
    }
}
