package uk.ac.excites.ucl.sapelliviewer.utils;

import android.graphics.Color;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.CompositeSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;
import com.esri.arcgisruntime.symbology.TextSymbol;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;

import static uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity.CONTRIBUTION_ID;


public class ClusterVectorLayer2 {
    final private int _clusterTolerance = 250;
    private ArrayList<Graphic> clusterGraphics = new ArrayList<>();
    private SimpleMarkerSymbol markerSymbolL = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.RED, 36);
    private SimpleMarkerSymbol markerSymbolM = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.BLUE, 30);
    private SimpleMarkerSymbol markerSymbolS = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.GREEN, 24);
    private SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.YELLOW, 18);
    private double clusterResolution;
    private MapView mapView;
    private GraphicsOverlay clusterGraphicsOverlay;
    private List<Map<String, Object>> clusterData;
    private int clusterID = 0;

    public ClusterVectorLayer2(final MapView mapView) {
        if (mapView == null) {
            return;
        }
        this.clusterResolution = _getExtent(mapView.getVisibleArea()).getWidth()
                / mapView.getWidth();
        this.mapView = mapView;
        this.clusterData = new ArrayList<>();
        this.clusterGraphicsOverlay = new GraphicsOverlay();
        this.mapView.getGraphicsOverlays().add(clusterGraphicsOverlay);

        mapView.addNavigationChangedListener(mapScaleChangedEvent -> {
            if (!mapView.isNavigating()) {
//                    clusterResolution = _getExtent(mapView.getVisibleArea())
//                            .getWidth() / mapView.getWidth();
//                    clusterData.clear();
//                    clusterGraphics.clear();
//                    clusterGraphicsOverlay.getGraphics().clear();
//                    clusterGraphics();
            }
        });
    }

    public GraphicsOverlay getGraphicLayer() {
        return this.clusterGraphicsOverlay;
    }

    public void setGraphicVisible(boolean visible) {
        if (clusterGraphicsOverlay != null) {
            clusterGraphicsOverlay.setVisible(visible);
        }
    }

    public void removeGraphiclayer() {
        if (mapView != null && clusterGraphicsOverlay != null) {
            try {
                this.mapView.getGraphicsOverlays().remove(clusterGraphicsOverlay);
                clusterGraphicsOverlay.getGraphics().clear();
            } catch (Exception e) {
            }
        }
    }

    public void clear() {
        if (clusterGraphicsOverlay != null) {
            clusterGraphicsOverlay.getGraphics().clear();
        }
    }

    public ArrayList<Graphic> getGraphicsByClusterID(int id) {
        ArrayList<Graphic> graphics = new ArrayList<>();
        for (Graphic gra : this.clusterGraphics
        ) {
            if (Integer.valueOf(gra.getAttributes().get("clusterID").toString()) == id) {
                graphics.add(gra);
            }
        }
        return graphics;
    }

    private void clusterGraphics(List<Contribution> contributions) {
        clusterID = 0;

        Map<String, Object> contributionMap = new HashMap<>();

        for (Contribution contribution : contributions) {
            if (contribution.getGeometry().getType().equals("Point")) {
                try {
                    contributionMap.put(CONTRIBUTION_ID, contribution.getId());
                    JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                    Point pnt = (Point) GeometryEngine.project(new Point(latLngCoordinates.getDouble(0), latLngCoordinates.getDouble(1)), SpatialReferences.getWgs84());
                    pnt = (Point) GeometryEngine.project(pnt, SpatialReferences.getWebMercator());

                    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20);
                    Graphic graphic = new Graphic(pnt, contributionMap, sms);

                    boolean clustered = false;
                    for (Map<String, Object> cluster : this.clusterData) {
                        Point pointCluster = new Point((Double) cluster.get("x"),
                                (Double) cluster.get("y"), mapView.getSpatialReference());

                        if (this._clusterTest(pnt, pointCluster)) {
                            this._clusterAddGraphic(graphic, cluster);
                            clustered = true;
                            break;
                        }
                    }

                    if (!clustered) {
                        this._clusterCreate(graphic);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

//        for (Graphic graphic : clusterGraphicsOverlay.getGraphics()) {
//            Point point = (Point) graphic.getGeometry();
//            boolean clustered = false;
//            for (Map<String, Object> cluster : this.clusterData) {
//                Point pointCluster = new Point((Double) cluster.get("x"),
//                        (Double) cluster.get("y"), mapView.getSpatialReference());
//
//                if (this._clusterTest(point, pointCluster)) {
//                    this._clusterAddGraphic(graphic, cluster);
//                    clustered = true;
//                    break;
//                }
//            }
//
//            if (!clustered) {
//                this._clusterCreate(graphic);
//            }
//        }
//        this._showAllClusters();
    }

    private void _clusterAddGraphic(Graphic graphic, Map<String, Object> cluster) {
        int count = (Integer) cluster.get("count");
        Point point = (Point) graphic.getGeometry();
        double xCluster = (Double) cluster.get("x");
        double yCluster = (Double) cluster.get("y");

        double x = (point.getX() + (xCluster * count)) / (count + 1);
        double y = (point.getY() + (yCluster * count)) / (count + 1);
        cluster.remove("x");
        cluster.remove("y");
        cluster.put("x", x);
        cluster.put("y", y);

        Envelope envelope = (Envelope) Geometry.fromJson(cluster.get("extent").toString());
        double xMin, yMin, xMax, yMax;
        xMin = envelope.getXMin();
        yMin = envelope.getYMin();
        xMax = envelope.getXMax();
        yMax = envelope.getYMax();

        if (point.getX() < xMin) {
            xMin = point.getX();
        }

        if (point.getX() > xMax) {
            xMax = point.getX();
        }

        if (point.getY() < yMin) {
            yMin = point.getY();
        }

        if (point.getY() > yMax) {
            yMax = point.getY();
        }

        Envelope envelopeNew = new Envelope(xMin, yMin, xMax, yMax, mapView.getSpatialReference());

        cluster.remove("extent");
        cluster.put("extent", envelopeNew.toJson());

        count++;
        cluster.remove("count");
        cluster.put("count", count);

        if (clusterGraphics != null) {
            clusterGraphics.add(graphic);
        }
    }

    private void _clusterCreate(Graphic graphic) {
        clusterGraphics.add(graphic);
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("count", 1);
        Point point = (Point) graphic.getGeometry();
        hashMap.put("extent", new Envelope((Point) graphic.getGeometry(), 0, 0).toJson());
        hashMap.put("x", point.getX());
        hashMap.put("y", point.getY());
        hashMap.put("clusterID", clusterID);
        graphic.getAttributes().put("clusterID", clusterID);
        clusterID++;

        this.clusterData.add(hashMap);
    }

    private boolean _clusterTest(Point point, Point pointCluster) {
        double distance = (Math.sqrt(Math.pow(
                (pointCluster.getX() - point.getX()), 2)
                + Math.pow((pointCluster.getY() - point.getY()), 2)) / this.clusterResolution);
        return (distance <= this._clusterTolerance);
    }

    private void _showAllClusters() {
        this.clusterGraphicsOverlay.getGraphics().clear();

        for (int i = 0, il = this.clusterData.size(); i < il; i++) {
            Map<String, Object> cluster = this.clusterData.get(i);
            Point pointCluster = new Point((Double) cluster.get("x"),
                    (Double) cluster.get("y"), mapView.getSpatialReference());

            Graphic graphic = new Graphic(pointCluster, cluster, createClusterSymbol(cluster));
            this.clusterGraphicsOverlay.getGraphics().add(graphic);
        }
    }

    private Symbol createClusterSymbol(Map<String, Object> cluster) {
        int count = (Integer) cluster.get("count");
        if (count == 1) {
            return markerSymbol;
        } else if (count > 1) {
            List<Symbol> symbols = new ArrayList<>();
            if (count <= 10) {
                symbols.add(markerSymbolS);
            } else if (count > 10 && count <= 20) {
                symbols.add(markerSymbolM);
            } else if (count > 20) {
                symbols.add(markerSymbolL);
            }
            TextSymbol textSymbol = new TextSymbol(18, count + "", Color.WHITE,
                    TextSymbol.HorizontalAlignment.CENTER,
                    TextSymbol.VerticalAlignment.MIDDLE);
            symbols.add(textSymbol);
            CompositeSymbol compositeSymbol = new CompositeSymbol(symbols);
            return compositeSymbol;
        }

        return null;
    }

    private Envelope _getExtent(Polygon polygon) {
        return polygon.getExtent();
    }

    public void updateCluster(List<Contribution> contributions) {
        this.clusterGraphics(contributions);
    }
}
