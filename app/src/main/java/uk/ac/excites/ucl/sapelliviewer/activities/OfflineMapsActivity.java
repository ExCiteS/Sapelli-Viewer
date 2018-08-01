package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ToggleButton;

import com.esri.arcgisruntime.geometry.Envelope;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.ui.FieldAdapter;
import uk.ac.excites.ucl.sapelliviewer.ui.ValueAdapter;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


/**
 * Created by julia
 */
public class OfflineMapsActivity extends BaseMapsActivity {
    // static variables
    public static String IMG_PATH = "img_path";
    public static String DB_NAME = "/mosaicdb.sqlite";
    public static String RASTER_NAME = "raster";


    private MapView mapView;
    private Callout callout;
    private FieldAdapter fieldAdapter;
    private ValueAdapter valueAdapter;

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
        RecyclerView fieldRecyclerView = findViewById(R.id.field_recycler_view);
        fieldRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerView valueRecyclerView = findViewById(R.id.value_recycler_view);
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

        disposables.add(
                getFields(projectId)
                        .toObservable().flatMapIterable(fields -> fields)
                        .filter(field -> !field.getKey().equals("DeviceId") && !field.getKey().equals("StartTime") && !field.getKey().equals("EndTime"))
                        .toList()
                        .subscribeWith(new DisposableSingleObserver<List<Field>>() {
                            @Override
                            public void onSuccess(List<Field> fields) {
                                fieldAdapter = new FieldAdapter(OfflineMapsActivity.this, fields, new FieldAdapter.FieldCheckedChangeListener() {
                                    @Override
                                    public void checkedChanged(ToggleButton buttonView, boolean isChecked, Field field) {

                                        for (LookUpValue lookUpValue : valueAdapter.getAllLookUpValues()) {
                                            if (lookUpValue.getFieldId() == field.getId())
                                                lookUpValue.setActive(isChecked);
                                        }
                                        if (isChecked) {
                                            buttonView.setBackgroundColor(ContextCompat.getColor(OfflineMapsActivity.this, R.color.colorPrimary));
                                        } else {
                                            buttonView.setBackgroundColor(Color.WHITE);
                                        }
                                        valueAdapter.notifyDataSetChanged();
                                    }
                                });
                                fieldRecyclerView.setAdapter(fieldAdapter);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getCategories", e.getMessage());
                            }
                        })
        );

        disposables.add(
                getLookUpValues(projectId)
                        .subscribeWith(new DisposableSingleObserver<List<LookUpValue>>() {
                            @Override
                            public void onSuccess(List<LookUpValue> lookUpValues) {
                                valueAdapter = new ValueAdapter(OfflineMapsActivity.this, lookUpValues);
                                valueRecyclerView.setAdapter(valueAdapter);

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getLookupvalues", e.getMessage());

                            }
                        })
        );


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
                                ArcGISMap map = new ArcGISMap(new Basemap(vtpk));
                                mapView.setMap(map);
                                disposables.add(getContributions(projectId).subscribe(OfflineMapsActivity.this::loadMarkers));
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


    protected void loadMarkers(List<Contribution> contributions) {
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 40);
        Map<String, Object> paths = new HashMap<String, Object>();
        PointCollection maarkerLocations = new PointCollection(mapView.getSpatialReference());
        for (Contribution contribution : contributions) {
            if (contribution.getGeometry().getType().equals("Point") && contribution.getContributionProperty() != null && contribution.getContributionProperty().getSymbol() != null) {
                try {
                    paths.put(IMG_PATH, MediaHelpers.dataPath + contribution.getContributionProperty().getSymbol());
                    JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                    Point pnt = (Point) GeometryEngine.project(new Point(latLngCoordinates.getDouble(0), latLngCoordinates.getDouble(1)), SpatialReferences.getWgs84());
                    pnt = (Point) GeometryEngine.project(pnt, mapView.getSpatialReference());
                    maarkerLocations.add(pnt);
                    Graphic graphic = new Graphic(pnt, paths, sms);
                    graphicsOverlay.getGraphics().add(graphic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            mapView.setViewpointGeometryAsync(new Polygon(maarkerLocations).getExtent(), 70);
        } catch (Exception e) {
            Log.e("Set Viewpoint", e.getMessage());
        }
        mapView.getGraphicsOverlays().clear();
        mapView.getGraphicsOverlays().add(graphicsOverlay);

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
                                    getContributions(projectId).subscribe(contributions -> OfflineMapsActivity.this.loadMarkers(contributions));
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


