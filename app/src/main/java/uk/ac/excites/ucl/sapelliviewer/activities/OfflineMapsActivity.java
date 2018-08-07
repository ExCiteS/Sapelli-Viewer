package uk.ac.excites.ucl.sapelliviewer.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.transition.CircularPropagation;
import android.support.transition.Explode;
import android.support.transition.Slide;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
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
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.AddRastersParameters;
import com.esri.arcgisruntime.raster.MosaicDatasetRaster;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.ui.DetailsFragment;
import uk.ac.excites.ucl.sapelliviewer.ui.FieldAdapter;
import uk.ac.excites.ucl.sapelliviewer.ui.ValueController;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;


/**
 * Created by julia
 */
public class OfflineMapsActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener {
    private static final String DETAILS_FRAGMENT = "detailsFragment";
    public static String CONTRIBUTION_ID = "contribution_id";
    public static String DB_NAME = "/mosaicdb.sqlite";
    public static String RASTER_NAME = "raster";


    private MapView mapView;
    private Callout callout;
    private FieldAdapter fieldAdapter;
    private AppDatabase db;
    private int projectId;
    private CompositeDisposable disposables;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set full screen and landscape
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.offline_map);
        db = AppDatabase.getAppDatabase(getApplicationContext());
        projectId = getIntent().getIntExtra(SettingsActivity.PROJECT_ID, 0);
        disposables = new CompositeDisposable();
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
                                disposables.add(db.contributionDao().getContributions(projectId).observeOn(Schedulers.io())
                                        .subscribeOn(Schedulers.io()).subscribe(OfflineMapsActivity.this::showMarkers)); // initially load all markers
                            }
                        }));


        mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mapView) {
                                       @Override
                                       public boolean onSingleTapConfirmed(MotionEvent e) {

                                           // get the screen point where user tapped
                                           android.graphics.Point clickedPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());


                                           // identify graphics on the graphics overlay
                                           final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mapView.identifyGraphicsOverlayAsync(mapView.getGraphicsOverlays().get(0), clickedPoint, 10.0, false, 1);
                                           identifyGraphic.addDoneListener(new Runnable() {
                                               @Override
                                               public void run() {
                                                   try {
                                                       IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
                                                       // get the list of graphics returned by identify graphic overlay
                                                       if (!grOverlayResult.getGraphics().isEmpty()) {
                                                           Graphic graphic = grOverlayResult.getGraphics().get(0);
                                                           displayDetails((Integer) graphic.getAttributes().get(CONTRIBUTION_ID));
                                                           mapView.getGraphicsOverlays().get(0).clearSelection();
                                                           graphic.setSelected(true);
//                                                           mapView.setViewpointCenterAsync(new Point(((Point) graphic.getGeometry()).getX(), ((Point) graphic.getGeometry()).getY()));
                                                       }
                                                   } catch (InterruptedException | ExecutionException ie) {
                                                       Log.e("getGraphic", ie.getMessage());
                                                   }
                                               }
                                           });
                                           return super.onSingleTapConfirmed(e);
                                       }
                                   }
        );
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
            if (!graphicCanStay) {
                if (graphic.isSelected())
                    closeFragment();
                graphicsIterator.remove();
            }
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
        } catch (Exception e) {
            Log.e("Set Viewpoint", e.getMessage());
        }

    }


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
        disposables.clear();
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
                                    disposables.add(db.contributionDao().getContributions(projectId).observeOn(Schedulers.io())
                                            .subscribeOn(Schedulers.io()).subscribe(OfflineMapsActivity.this::showMarkers));
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

    public Context getContext() {
        return OfflineMapsActivity.this;
    }

    public int getProjectId() {
        return projectId;
    }

    public void displayDetails(int contributionId) {
        DetailsFragment shownFragment = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILS_FRAGMENT);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (shownFragment != null && shownFragment.isVisible()) {
            if (shownFragment.getContributionId() != contributionId) {
                DetailsFragment detailsFragment = DetailsFragment.newInstance(contributionId);
                fragmentManager
                        .beginTransaction()
//                        .addToBackStack(null)
                        .replace(R.id.fragment_container, detailsFragment, DETAILS_FRAGMENT)
                        .commit();
            }
        } else {
            DetailsFragment detailsFragment = DetailsFragment.newInstance(contributionId);
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, detailsFragment, DETAILS_FRAGMENT)
                    .commit();
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.fragment_container);
            params.addRule(RelativeLayout.ABOVE, R.id.value_recycler_view);
            params.setMargins(0, 0, 0, -24);
            mapView.setLayoutParams(params);
        }
    }


    @Override
    public void onFragmentInteraction() {
        closeFragment();
    }

    public void closeFragment() {

        DetailsFragment shownFragment = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILS_FRAGMENT);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (shownFragment != null && shownFragment.isVisible()) {
            fragmentManager.beginTransaction().remove(shownFragment).commit();
        }

        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.value_recycler_view);
        params.setMargins(0, 0, 0, -24);

        mapView.setLayoutParams(params);
        mapView.getGraphicsOverlays().get(0).clearSelection();
    }


}


