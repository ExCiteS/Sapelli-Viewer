package uk.ac.excites.ucl.sapelliviewer.utils;

import android.graphics.Color;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.CompositeSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;


public class ClusterVectorLayer {
    final private int _clusterTolerance = 200;
    private double lastMapScale;
    private ArrayList<Graphic> clusterGraphics = new ArrayList<>();
    private SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.YELLOW, 18);
    private double clusterResolution;
    private MapView mapView;
    private GraphicsOverlay clusterGraphicsOverlay;
    private List<Map<String, Object>> clusterData;
    private int clusterID = 0;
    private List<Contribution> contributions;
    private Random random = new Random();
    private int[] colors = new int[]{Color.DKGRAY, Color.GRAY, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
    private List<ContributionProperty> properties;

    public ClusterVectorLayer(final MapView mapView) {
        if (mapView == null) return;
        if (mapView.getVisibleArea() == null) return;

        this.mapView = mapView;
        this.clusterData = new ArrayList<>();
        this.clusterGraphicsOverlay = new GraphicsOverlay();
        this.mapView.getGraphicsOverlays().add(clusterGraphicsOverlay);

        mapView.addNavigationChangedListener(navigationChangedEvent -> {
            if (((MapView) navigationChangedEvent.getSource()).getMapScale() != lastMapScale) {
                reCluster();
            }
        });
    }

    public static String stringToARGB(String s) {
        int i = s.hashCode();
        return Integer.toHexString(((i >> 24) & 0xFF)) +
                Integer.toHexString(((i >> 16) & 0xFF)) +
                Integer.toHexString(((i >> 8) & 0xFF)) +
                Integer.toHexString((i & 0xFF));
    }

    public void reCluster() {
        clusterData.clear();
        clusterGraphics.clear();
        clusterGraphicsOverlay.getGraphics().clear();
        clusterGraphics(contributions);
        lastMapScale = mapView.getMapScale();
    }

    private CompositeSymbol generateSymbol(int size, int count) {
        List<Symbol> symbols = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                    colors[random.nextInt(8)], size);

            int offsetX = random.nextInt(30);
            int offsetY = random.nextInt(30);
            markerSymbol.setOffsetX((float) offsetX);
            markerSymbol.setOffsetY((float) offsetY);

            symbols.add(markerSymbol);
        }

        return new CompositeSymbol(symbols);
    }

    private Symbol generateSingleSymbol(int size, int color) {
        return new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                colors[random.nextInt(8)], size);
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
        if (contributions == null) {
            clear();
            return;
        }
        if (contributions.size() == 0) {
            clear();
            return;
        }

        calculateResolution();

        clusterID = 0;

        Map<String, Object> contributionMap = new HashMap<>();

        for (Contribution contribution : contributions) {
            uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry geometry = contribution.getGeometry();

            contributionMap.put("contributionId", contribution.getId());

            Graphic graphic;
            Point centerPoint;
            if ("Point".equals(geometry.getType())) {
                Point point = GeoJsonGeometryConverter.convertToPoint(geometry.getCoordinates());
                Point pnt = (Point) GeometryEngine.project(point, SpatialReferences.getWgs84());
                centerPoint = (Point) GeometryEngine.project(pnt, SpatialReferences.getWebMercator());
                graphic = createPointGraphics(pnt, contributionMap);
            } /*else if ("Polyline".equals(geometry.getType())) {
                PointCollection points = GeoJsonGeometryConverter.convertToLine(geometry.getCoordinates());
                graphic = createPolylineGraphics(points, contributionMap);
                centerPoint = graphic.getGraphicsOverlay().getExtent().getCenter();
            }*/
//            else if ("Polygon".equals(geometry.getType())) {
//                PointCollection points = GeoJsonGeometryConverter.convertToPolygon(geometry.getCoordinates());
//                graphic = createPolygonGraphics(points, contributionMap);
//                centerPoint = graphic.getGraphicsOverlay().getExtent().getCenter();
//            }
            else
                continue;
            // TODO: else case will be handled

            boolean clustered = false;
            for (Map<String, Object> clusterAttrs : this.clusterData) {
                Point pointCluster = new Point(
                        (Double) clusterAttrs.get("x"),
                        (Double) clusterAttrs.get("y"),
                        mapView.getSpatialReference()
                );

                if (this._clusterTest(centerPoint, pointCluster)) {
                    this._clusterAddGraphic(graphic, clusterAttrs, contribution);
                    clustered = true;
                    break;
                }
            }

            if (!clustered) {
                this._clusterCreate(graphic, contribution);
            }
        }
        this._showAllClusters();
    }

    private void calculateResolution() {
        this.clusterResolution = _getExtent(mapView.getVisibleArea()).getWidth() / mapView.getWidth();
    }

    private Graphic createPointGraphics(Point point, Map<String, Object> contributionData) {
        // Create symbol
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));

        // Create point
        Point pnt = (Point) GeometryEngine.project(point, SpatialReferences.getWgs84());
        pnt = (Point) GeometryEngine.project(pnt, SpatialReferences.getWebMercator());

        // Create graphic
        Graphic pointGraphic = new Graphic(pnt, contributionData, pointSymbol);
        return pointGraphic;
    }

    private Graphic createPolylineGraphics(PointCollection polylinePoints, Map<String, Object> contributionData) {
        // Create symbol
        SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3.0f);

        // Create points
        Polyline polyline = new Polyline(polylinePoints);

        // Create graphic
        Graphic polylineGraphic = new Graphic(polyline, contributionData, polylineSymbol);
        return polylineGraphic;
    }

    private Graphic createPolygonGraphics(PointCollection polygonPoints, Map<String, Object> contributionData) {
        // Create symbol
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.rgb(226, 119, 40),
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));

        // Create points
        Polygon polygon = new Polygon(polygonPoints);

        // Create graphic
        Graphic polygonGraphic = new Graphic(polygon, contributionData, polygonSymbol);
        return polygonGraphic;
    }

    private void _clusterAddGraphic(Graphic graphic, Map<String, Object> cluster, Contribution contribution) {
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

        if (cluster.containsKey("contributions")) {
            ArrayList<String> contributions = GeoJsonGeometryConverter.convertFromString((String) cluster.get("contributions"));
            contributions.add(String.valueOf(contribution.getId()));
            cluster.remove("contributions");
            cluster.put("contributions", GeoJsonGeometryConverter.convertToString(contributions));
        }

        if (clusterGraphics != null) {
            clusterGraphics.add(graphic);
        }
    }

    private void _clusterCreate(Graphic graphic, Contribution contribution) {
        clusterGraphics.add(graphic);
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("count", 1);
        Point point = (Point) graphic.getGeometry();
        hashMap.put("extent", new Envelope((Point) graphic.getGeometry(), 0, 0).toJson());
        hashMap.put("x", point.getX());
        hashMap.put("y", point.getY());
        hashMap.put("clusterID", clusterID);
        ArrayList<String> contributionList = new ArrayList<>();
        contributionList.add(String.valueOf(contribution.getId()));
        hashMap.put("contributions", GeoJsonGeometryConverter.convertToString(contributionList));
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

        for (Map<String, Object> cluster : this.clusterData) {
            Point pointCluster = new Point(
                    (Double) cluster.get("x"),
                    (Double) cluster.get("y"),
                    mapView.getSpatialReference()
            );

            Graphic graphic = new Graphic(pointCluster, cluster, createClusterSymbol(cluster));
            this.clusterGraphicsOverlay.getGraphics().add(graphic);
        }
    }

    private Symbol createClusterSymbol(Map<String, Object> cluster) {
        int count = (Integer) cluster.get("count");
        if (count == 1) {
            ArrayList<String> contributions = GeoJsonGeometryConverter.convertFromString((String) cluster.get("contributions"));
//            Contribution c = GeoJsonGeometryConverter.convertToContribution(contributions.get(0));

            return new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                    Color.parseColor("#1" + contributions.get(0)), 18);
        } else if (count > 1) {
            int size;
            if (count <= 10) {
                size = 24;
            } else if (count <= 100) {
                size = 28;
            } else {
                size = 32;
            }

            if (cluster.containsKey("contributions")) {
                ArrayList<String> contributions = GeoJsonGeometryConverter.convertFromString((String) cluster.get("contributions"));

                List<Symbol> symbols = new ArrayList<>();
                for (String s : contributions) {

                    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                            Color.parseColor("#1" + s), size);

                    int offsetX = random.nextInt(30);
                    int offsetY = random.nextInt(30);
                    markerSymbol.setOffsetX((float) offsetX);
                    markerSymbol.setOffsetY((float) offsetY);

                    symbols.add(markerSymbol);
                }

                return new CompositeSymbol(symbols);
            }
        }

        return null;
    }

//    private String getColor(int cid) {
//        if (properties == null) return "FFFFFF";
//
//        for (ContributionProperty p : properties) {
//            if (p.contributionId == cid)
//                return stringToARGB(p.)
//        }
//    }

    private Envelope _getExtent(Polygon polygon) {
        if (polygon == null) return null;

        return polygon.getExtent();
    }

    public void updateCluster(List<Contribution> contributions, List<ContributionProperty> properties) {
        if (contributions == null) clear();

        this.contributions = contributions;
        this.properties = properties;

        this.clusterGraphics(contributions);
    }
}
