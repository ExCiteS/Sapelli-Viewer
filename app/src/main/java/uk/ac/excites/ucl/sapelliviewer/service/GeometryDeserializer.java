package uk.ac.excites.ucl.sapelliviewer.service;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.maps.android.data.geojson.GeoJsonPolygon;


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
        Geometry result = new Geometry(type, coordinates);
        return result;
    }

    public String extractCoords(JsonElement coordinates, String type) {
        JsonArray coordArray;
        String result = "";
        if (type.equals("Point")) {
            coordArray = (JsonArray) coordinates.getAsJsonArray();
            result += "[" + coordArray.get(0).toString() + ", " + coordArray.get(1).toString() + "]";
        } else {
            coordArray = (JsonArray) coordinates.getAsJsonArray().get(0);
            result += "[";
            for (int i = 0; i < coordArray.size(); i++) {
                result += "[" + coordArray.get(i).getAsJsonArray().get(0) + ", " + coordArray.get(i).getAsJsonArray().get(1) + "],";
            }
            result = result.substring(0, result.length() - 1);
            result += "]";
        }
        return result;

    }
}
