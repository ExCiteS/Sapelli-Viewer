package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.TypeConverter;

public class GeoKeyTypeConverters {

    @TypeConverter
    public static double[][][] stringToDoubleArray(String data) {
        if (data == null) {


        }
        return null;
    }

    @TypeConverter
    public static String DoubleArrayToString(double[][][] someObjects) {
        String result = "[";
        for (double[] coordinate : someObjects[0]) {
            result += "[" + coordinate[0] + ", " + coordinate[1] + "], ";
        }
        return result.substring(0, result.length() - 2) + "]";
    }
}

