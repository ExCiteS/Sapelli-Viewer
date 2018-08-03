package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ToggleButton;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.AddRastersParameters;
import com.esri.arcgisruntime.raster.MosaicDatasetRaster;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.util.ListenableList;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.ui.FieldAdapter;
import uk.ac.excites.ucl.sapelliviewer.ui.ValueAdapter;
import uk.ac.excites.ucl.sapelliviewer.ui.ValueController;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


/**
 * Created by julia
 */
public class OfflineMapsActivity extends BaseMapsActivity {
    // static variables
    public static String CONTRIBUTION_ID = "contribution_id";
    public static String DB_NAME = "/mosaicdb.sqlite";
    public static String RASTER_NAME = "raster";


    private MapView mapView;
    private Callout callout;
    private FieldAdapter fieldAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set full screen and landscape
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.offline_map);
        mapView = (MapView) findViewById(R.id.map);
        copyBlankMap();

        new ValueController(this, findViewById(R.id.value_recycler_view), disposables)
                .addFieldController(findViewById(R.id.field_recycler_view)); // ValueController should also work without Fields


        disposables.add(
                db.projectInfoDao().getMapPath(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableMaybeObserver<String>() {
                            @Override
                            public void onSuccess(String path) {
                                createMosaicDataset(path);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getMapPath", e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                ArcGISVectorTiledLayer vtpk = new ArcGISVectorTiledLayer(MediaHelpers.dataPath + File.separator + getString(R.string.blank_map));
                                mapView.setMap(new ArcGISMap(new Basemap(vtpk)));
                                disposables.add(getContributions(projectId).subscribe(OfflineMapsActivity.this::showMarkers)); // initially load all markers
                            }
                        }));


//        map.setOnSingleTapListener(new OnSingleTapListener() {
//
//            private static final long serialVersionUID = 1L;
//
//            public void onSingleTap(float x, float y) {
//
//                if (!map.isLoaded())
//                    return;
//                int[] uids = graphicsOverlay.getGraphicIDs(x, y, 2);
//                if (uids != null && uids.length > 0) {
//
//                    int targetId = uids[0];
//                    Graphic gr = graphicsOverlay.getGraphic(targetId);
//                    callout = map.getCallout();
//
//                    // Sets Callout style
//                    callout.setStyle(R.layout.popup);
//
//                    // Sets custom content view to Callout
//                    callout.setContent(loadView(gr));
//                    // map.centerAt(new Point(x, y), true);
//                    callout.show(map.toMapPoint(new Point(x, y)));
//                } else {
//                    if (callout != null && callout.isShowing()) {
//                        callout.hide();
//                    }
//                }
//
//            }
//        });
//

    }


    public void updateMarkers(List<Contribution> contributionsToDisplay) {
        Iterator contributionIterator;
        Iterator graphicsIterator;
        boolean graphicCanStay;

        graphicsIterator = mapView.getGraphicsOverlays().get(0).getGraphics().iterator();
        while (graphicsIterator.hasNext()) {
            graphicCanStay = false;
            Graphic graphic = (Graphic) graphicsIterator.next();
            contributionIterator = contributionsToDisplay.iterator();
            while (contributionIterator.hasNext()) {
                Contribution contribution = (Contribution) contributionIterator.next();
                if (contribution.getId() == (Integer) graphic.getAttributes().get(CONTRIBUTION_ID)) {
                    contributionIterator.remove();
                    graphicCanStay = true;
                }
            }
            if (!graphicCanStay)
                graphicsIterator.remove();
        }
        showMarkers(contributionsToDisplay);
    }


    protected void showMarkers(List<Contribution> contributions) {
        GraphicsOverlay graphicsOverlay;
        if (mapView.getGraphicsOverlays().isEmpty()) {
            graphicsOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(graphicsOverlay);
        } else {
            graphicsOverlay = mapView.getGraphicsOverlays().get(0);

        }

        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20);
        Map<String, Object> contributionId = new HashMap<String, Object>();
        for (Contribution contribution : contributions) {
            if (contribution.getGeometry().getType().equals("Point") && contribution.getContributionProperty() != null && contribution.getContributionProperty().getSymbol() != null) {
                try {
                    contributionId.put(CONTRIBUTION_ID, contribution.getId());
                    JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                    Point pnt = (Point) GeometryEngine.project(new Point(latLngCoordinates.getDouble(0), latLngCoordinates.getDouble(1)), SpatialReferences.getWgs84());
                    pnt = (Point) GeometryEngine.project(pnt, mapView.getSpatialReference());
                    Graphic graphic = new Graphic(pnt, contributionId, sms);
                    graphicsOverlay.getGraphics().add(graphic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (graphicsOverlay.getGraphics().size() > 1)
                mapView.setViewpointGeometryAsync(graphicsOverlay.getExtent(), 70);
            else if (graphicsOverlay.getGraphics().size() == 1)
                mapView.setViewpointCenterAsync(graphicsOverlay.getExtent().getCenter(), 3000);
//            mapView.setViewpointGeometryAsync(new Polygon(maarkerLocations).getExtent(), 70);
        } catch (Exception e) {
            Log.e("Set Viewpoint", e.getMessage());
        }

    }


//    // Creates custom content view with 'Graphic' attributes
//    private View loadView(Graphic graphic) {
//        View view = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
//
//        Set<String> keys = graphic.getAttributes().keySet();
//
//        for (String key : keys) {
//            if (key.equals(IMG_PATH)) {
//                ImageView imgView = (ImageView) view.findViewById(R.id.photo_view);
//                createView(imgView, graphic.getAttributes().get(key).toString());
//            }
//        }
//
//        return view;
//
//    }


    public void zoomIn(View view) {
        Viewpoint viewpoint = new Viewpoint(mapView.getVisibleArea().getExtent().getCenter(), mapView.getMapScale() / 2, mapView.getMapRotation());
        mapView.setViewpointAsync(viewpoint, 0.5f);

    }

    public void zoomOut(View view) {
        Viewpoint viewpoint = new Viewpoint(mapView.getVisibleArea().getExtent().getCenter(), mapView.getMapScale() * 2, mapView.getMapRotation());
        mapView.setViewpointAsync(viewpoint, 0.5f);
    }

    public void rotateNorth(View view) {
        mapView.setViewpointRotationAsync(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.dispose();
    }


    public void createMosaicDataset(String rasterPath) {
//        // create a new mobile mosaic dataset
        MosaicDatasetRaster mosaicDatasetRaster = MosaicDatasetRaster
                .create(MediaHelpers.dataPath + DB_NAME, RASTER_NAME, SpatialReferences.getWebMercator());

        // add some raster files to the mobile mosaic dataset
        mosaicDatasetRaster.addDoneLoadingListener(() -> {
            if (mosaicDatasetRaster.getLoadStatus() == LoadStatus.LOADED) {
                AddRastersParameters parameters = new AddRastersParameters();
                parameters.setInputDirectory(rasterPath);
                mosaicDatasetRaster.addRastersAsync(parameters).addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        MosaicDatasetRaster mosaicDatasetRaster = new MosaicDatasetRaster(MediaHelpers.dataPath + DB_NAME, RASTER_NAME);
                        RasterLayer mosaicDatasetRasterLayer = new RasterLayer(mosaicDatasetRaster);
                        Basemap basemap = new Basemap(mosaicDatasetRasterLayer);
                        ArcGISMap map = new ArcGISMap(basemap);
                        mapView.setMap(map);
                        map.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                            @Override
                            public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                                if (loadStatusChangedEvent.getNewLoadStatus().name().equals("LOADED"))
                                    getContributions(projectId).subscribe(contributions -> OfflineMapsActivity.this.showMarkers(contributions));
                            }
                        });
                    }
                });
            }
        });
        mosaicDatasetRaster.loadAsync();
    }

    public void copyBlankMap() {
        try {
            String name = getString(R.string.blank_map);
            if (!new File(MediaHelpers.dataPath, name).exists())
                MediaHelpers.copyFile(getAssets().open(name), new FileOutputStream(new File(MediaHelpers.dataPath, name)));
        } catch (Exception e) {
            Log.e("copyBlankMap", e.getMessage());
        }

    }

}


