package uk.ac.excites.ucl.sapelliviewer.utils;

import android.os.Build;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Logs;

public class Logger {
    public static final String INITIAL_EXTENT = "InitialExtent";
    public static final String PAN = "Pan";
    public static final String ZOOM_IN_BUTTON = "ZoomInButton";
    public static final String ZOOM_OUT_BUTTON = "ZoomOutButton";
    public static final String ZOOM_IN_PINCH = "ZoomInPinch";
    public static final String ZOOM_IN_DOUBLE_TAP = "ZoomInDoubleTap";
    public static final String ROTATE = "Rotate";
    public static final String ZOOM_OUT_PINCH = "ZoomOutPinch";
    public static final String FIELD_CHECKED = "FieldChecked";
    public static final String FIELD_UNCHECKED = "FieldUnchecked";
    public static final String VALUE_CHECKED = "LookUpValueChecked";
    public static final String VALUE_UNCHECKED = "LookUpValueUnchecked";
    public static final String CONTRIBUTION_DETAILS_OPENED = "ContributionDetailsOpened";
    public static final String CONTRIBUTION_DETAILS_CLOSED = "ContributionDetailsClosed";
    public static final String PHOTO_CLOSED = "PhotoClosed";
    public static final String PHOTO_OPENED = "PhotoOpened";
    public static final String AUDIO_CLOSED = "AudioClosed";
    public static final String AUDIO_OPENED = "AudioOpened";
    public static final String TOGGLE_ALL_OFF = "ToggleAllOff";
    public static final String TOGGLE_ALL_ON = "ToggleAllOn";


    public Logs log(int projectId, String event, Integer interactionId, MapView mapView) {
        String time;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = Instant.now().toString();
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            time = df.format(new Date());
        }

        Envelope extent = mapView.getVisibleArea().getExtent();

        double minX = extent.getXMin();
        double minY = extent.getYMin();
        double maxX = extent.getXMax();
        double maxY = extent.getYMax();

        String topLeft = getWGS84coord(new Point(minX, maxY, mapView.getSpatialReference()));
        String topRight = getWGS84coord(new Point(maxX, maxY, mapView.getSpatialReference()));
        String bottomLeft = getWGS84coord(new Point(minX, minY, mapView.getSpatialReference()));
        String bottomRight = getWGS84coord(new Point(maxX, minY, mapView.getSpatialReference()));

        Geometry geometry = new Geometry("Polygon", "[" + topLeft + ", " + topRight + ", " + bottomRight + ", " + bottomLeft + "]");
        return new Logs(projectId, time, event, interactionId, mapView.getMapScale(), geometry);
    }

    private String getWGS84coord(Point coord) {
        Point pnt = (Point) GeometryEngine.project(coord, SpatialReferences.getWgs84());
        return "[" + pnt.getX() + ", " + pnt.getY() + "]";
    }
}
