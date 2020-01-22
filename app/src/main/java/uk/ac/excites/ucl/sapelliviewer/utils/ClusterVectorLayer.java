package uk.ac.excites.ucl.sapelliviewer.utils;

import android.graphics.Color;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.CompositeSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;
import com.esri.arcgisruntime.symbology.TextSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClusterVectorLayer {
    final private int _clusterTolerance = 250;
    private ArrayList<Graphic> _clusterGraphics = new ArrayList<Graphic>();
    private SimpleMarkerSymbol markerSymbolL = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.RED, 36);
    private SimpleMarkerSymbol markerSymbolM = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.BLUE, 30);
    private SimpleMarkerSymbol markerSymbolS = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.GREEN, 24);
    private SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            Color.YELLOW, 18);
    private double _clusterResolution;
    private MapView _mapView;
    private GraphicsOverlay _GraphicsOverlay;
    private GraphicsOverlay _clusterGraphicsOverlay;
    private List<Map<String, Object>> _clusterData;
    private int clusterID = 0;

    public ClusterVectorLayer(final MapView mapView, GraphicsOverlay GraphicsOverlay, GraphicsOverlay drawingOverlay) {
        if (mapView == null || GraphicsOverlay == null) {
            return;
        }
        if (mapView.getVisibleArea() == null) return;
        this._clusterResolution = _getExtent(mapView.getVisibleArea()).getWidth()
                / mapView.getWidth();
        this._mapView = mapView;
        this._GraphicsOverlay = GraphicsOverlay;
        GraphicsOverlay.setVisible(false);
        this._clusterData = new ArrayList<>();
        this._clusterGraphicsOverlay = new GraphicsOverlay();
        if (this._mapView.getGraphicsOverlays() != null) {
            this._mapView.getGraphicsOverlays().clear();
            this._mapView.getGraphicsOverlays().add(_GraphicsOverlay);
            this._mapView.getGraphicsOverlays().add(this._clusterGraphicsOverlay);
            if (drawingOverlay != null) this._mapView.getGraphicsOverlays().add(drawingOverlay);
        }
        this._clusterGraphics();
    }

    public GraphicsOverlay getGraphicLayer() {
        return this._clusterGraphicsOverlay;
    }

    public void setGraphicVisible(boolean visible) {
        if (_clusterGraphicsOverlay != null) {
            _clusterGraphicsOverlay.setVisible(visible);
        }
    }

    public void removeGraphiclayer() {
        if (_mapView != null && _clusterGraphicsOverlay != null) {
            try {
                this._mapView.getGraphicsOverlays().remove(_clusterGraphicsOverlay);
                _clusterGraphicsOverlay.getGraphics().clear();
            } catch (Exception e) {
            }
        }
    }

    public void clear() {
        if (_clusterGraphicsOverlay != null) {
            _clusterGraphicsOverlay.getGraphics().clear();
        }
    }

    public ArrayList<Graphic> getGraphicsByClusterID(int id) {
        ArrayList<Graphic> graphics = new ArrayList<>();
        for (Graphic gra : this._clusterGraphics
        ) {
            if (Integer.valueOf(gra.getAttributes().get("clusterID").toString()) == id) {
                graphics.add(gra);
            }
        }
        return graphics;
    }

    private void _clusterGraphics() {
        clusterID = 0;

        for (Graphic graphic : _GraphicsOverlay.getGraphics()) {
            Point point = (Point) graphic.getGeometry();
            boolean clustered = false;
            for (Map<String, Object> cluster : this._clusterData) {
                Point pointCluster = new Point((Double) cluster.get("x"),
                        (Double) cluster.get("y"), _mapView.getSpatialReference());

                if (this._clusterTest(point, pointCluster)) {
                    this._clusterAddGraphic(graphic, cluster);
                    clustered = true;
                    break;
                }
            }

            if (!clustered) {
                this._clusterCreate(graphic);
            }
        }
        this._showAllClusters();
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

        Envelope envelopeNew = new Envelope(xMin, yMin, xMax, yMax, _mapView.getSpatialReference());

        cluster.remove("extent");
        cluster.put("extent", envelopeNew.toJson());

        count++;
        cluster.remove("count");
        cluster.put("count", count);

        if (_clusterGraphics != null) {
            _clusterGraphics.add(graphic);
        }
    }

    private void _clusterCreate(Graphic graphic) {
        _clusterGraphics.add(graphic);
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("count", 1);
        Point point = (Point) graphic.getGeometry();
        hashMap.put("extent", new Envelope((Point) graphic.getGeometry(), 0, 0).toJson());
        hashMap.put("x", point.getX());
        hashMap.put("y", point.getY());
        hashMap.put("clusterID", clusterID);
        graphic.getAttributes().put("clusterID", clusterID);
        clusterID++;

        this._clusterData.add(hashMap);
    }

    private boolean _clusterTest(Point point, Point pointCluster) {
        double distance = (Math.sqrt(Math.pow(
                (pointCluster.getX() - point.getX()), 2)
                + Math.pow((pointCluster.getY() - point.getY()), 2)) / this._clusterResolution);
        return (distance <= this._clusterTolerance);
    }

    private void _showAllClusters() {
        this._clusterGraphicsOverlay.getGraphics().clear();

        for (int i = 0, il = this._clusterData.size(); i < il; i++) {
            Map<String, Object> cluster = this._clusterData.get(i);
            Point pointCluster = new Point((Double) cluster.get("x"),
                    (Double) cluster.get("y"), _mapView.getSpatialReference());

            Graphic graphic = new Graphic(pointCluster, cluster, createClusterSymbol(cluster));
            this._clusterGraphicsOverlay.getGraphics().add(graphic);
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
}
