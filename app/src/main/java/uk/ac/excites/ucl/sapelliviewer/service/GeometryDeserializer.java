package uk.ac.excites.ucl.sapelliviewer.service;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.maps.android.data.Geometry;


import java.lang.reflect.Type;

import uk.ac.excites.ucl.sapelliviewer.utils.Parser;

/**
 * Created by Julia on 20/02/2018.
 */

public class GeometryDeserializer implements JsonDeserializer<Geometry> {
    @Override
    public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    Geometry result = null;

        try {

            JSONObject geometry = new JSONObject(json.toString());
            result = Parser.parseGeometry(geometry);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
