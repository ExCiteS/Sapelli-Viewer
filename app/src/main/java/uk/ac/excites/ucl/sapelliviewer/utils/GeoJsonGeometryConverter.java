package uk.ac.excites.ucl.sapelliviewer.utils;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;

public class GeoJsonGeometryConverter {

    private static Gson gson = new Gson();

    public static Point convertToPoint(String coordinates) {
        if (coordinates != null) {
            Type listType = new TypeToken<List<Double>>() {
            }.getType();
            List<Double> data = gson.fromJson(coordinates, listType);

            return new Point(data.get(0), data.get(1));

        } else {
            return null;
        }
    }

    public static PointCollection convertToLine(String coordinates) {
        if (coordinates != null) {
            Type listType = new TypeToken<List<List<Double>>>() {
            }.getType();

            List<List<Double>> data = gson.fromJson(coordinates, listType);
            PointCollection points = new PointCollection(SpatialReferences.getWgs84());

            for (int i = 0; i < data.size(); i++) {
                points.add(new Point(data.get(i).get(1), data.get(i).get(2)));
            }
            return points;
        } else {
            return null;
        }
    }

    public static PointCollection convertToPolygon(String coordinates) {
        if (coordinates != null) {
            Type listType = new TypeToken<List<List<List<Double>>>>() {
            }.getType();
            List<List<List<Double>>> data = gson.fromJson(coordinates, listType);

            PointCollection points = new PointCollection(SpatialReferences.getWgs84());
            for (int i = 0; i < data.size(); i++) {
                for (int j = 0; j < data.get(i).size(); j++) {
                    Double latitude = data.get(i).get(j).get(0);
                    Double longitude = data.get(i).get(j).get(1);
                    points.add(new Point(latitude, longitude));
                }
            }
            return points;
        } else {
            return null;
        }
    }

    public static String convertToString(Contribution contribution) {
        return gson.toJson(contribution);
    }

    public static String convertToString(ArrayList<String> list) {
        return gson.toJson(list);
    }

    public static ArrayList<String> convertFromString(String listJson) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(listJson, listType);
    }

    public static Contribution convertToContribution(String contributionString) {
        return gson.fromJson(contributionString, Contribution.class);
    }
}
