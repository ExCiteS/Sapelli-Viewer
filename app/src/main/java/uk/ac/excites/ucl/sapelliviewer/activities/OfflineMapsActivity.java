package uk.ac.excites.ucl.sapelliviewer.activities;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;
import uk.ac.excites.ucl.sapelliviewer.ui.DetailsFragment;
import uk.ac.excites.ucl.sapelliviewer.ui.NavigationFragment;
import uk.ac.excites.ucl.sapelliviewer.ui.ValueController;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;


/**
 * Created by julia
 */
public class OfflineMapsActivity extends AppCompatActivity implements NavigationFragment.OnShowClickListener {
    private static final String DETAILS_FRAGMENT = "detailsFragment";
    public static String CONTRIBUTION_ID = "contribution_id";
    public static String DB_NAME = "/mosaicdb.sqlite";
    public static String RASTER_NAME = "raster";


    private MapView mapView;
    private AppDatabase db;
    private int projectId;
    private CompositeDisposable disposables;
    private ArcGISMap map;
    private int backCounter;
    private DatabaseClient dbClient;
    private int resetAngle;
    private DrawerLayout drawer;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        db = AppDatabase.getAppDatabase(getApplicationContext());
        projectId = getIntent().getIntExtra(SettingsActivity.PROJECT_ID, 0);
        disposables = new CompositeDisposable();
        mapView = (MapView) findViewById(R.id.map);
        dbClient = new DatabaseClient(OfflineMapsActivity.this, projectId, mapView);

        copyBlankMap();

        ((ViewGroup) findViewById(R.id.root)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGE_APPEARING);

        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /* Load Vector base map*/
        ArcGISVectorTiledLayer vtpk = new ArcGISVectorTiledLayer(MediaHelpers.dataPath + File.separator + getString(R.string.blank_map));
        map = new ArcGISMap(new Basemap(vtpk));


        mapView.setMap(map);


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
                                                           if (!graphic.isSelected()) {
                                                               Integer contributionId = (Integer) graphic.getAttributes().get(CONTRIBUTION_ID);
                                                               displayDetails(contributionId);
                                                               mapView.getGraphicsOverlays().get(0).clearSelection();
                                                               graphic.setSelected(true);
                                                               dbClient.insertLog(Logger.CONTRIBUTION_DETAILS_OPENED, contributionId);
                                                           } else {
                                                               closeFragment();
                                                               graphic.setSelected(false);
                                                               dbClient.insertLog(Logger.CONTRIBUTION_DETAILS_CLOSED, (Integer) graphic.getAttributes().get(CONTRIBUTION_ID));
                                                           }
//                                                           mapView.setViewpointCenterAsync(new Point(((Point) graphic.getGeometry()).getX(), ((Point) graphic.getGeometry()).getY()));
                                                       }
                                                   } catch (InterruptedException | ExecutionException ie) {
                                                       Log.e("getGraphic", ie.getMessage());
                                                   }
                                               }
                                           });
                                           return super.onSingleTapConfirmed(e);
                                       }

                                       @Override
                                       public boolean onRotate(MotionEvent event, double rotationAngle) {
                                           if (rotationAngle > 10)
                                               dbClient.setPendingRotation(true);
                                           return super.onRotate(event, rotationAngle);
                                       }


                                       @Override
                                       public boolean onDoubleTap(MotionEvent e) {
                                           dbClient.insertLog(Logger.ZOOM_IN_DOUBLE_TAP);
                                           return super.onDoubleTap(e);
                                       }

                                       @Override
                                       public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                                           dbClient.insertLog(Logger.PAN);
                                           return super.onFling(e1, e2, velocityX, velocityY);
                                       }

                                       @Override
                                       public boolean onUp(MotionEvent e) {
                                           if (dbClient.isPendingotation()) {
                                               dbClient.insertLog(Logger.ROTATE);
                                               dbClient.setPendingRotation(false);
                                           }
                                           return super.onUp(e);
                                       }

                                       @Override
                                       public boolean onScaleBegin(ScaleGestureDetector detector) {
                                           dbClient.setScale(mapView.getMapScale());
                                           return super.onScaleBegin(detector);
                                       }

                                       @Override
                                       public void onScaleEnd(ScaleGestureDetector detector) {
                                           if (mapView.getMapScale() < dbClient.getScale())
                                               dbClient.insertLog(Logger.ZOOM_IN_PINCH);
                                           else if (mapView.getMapScale() > dbClient.getScale())
                                               dbClient.insertLog(Logger.ZOOM_OUT_PINCH);
                                           super.onScaleEnd(detector);
                                       }
                                   }
        );
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
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
        showMarkers(contributionsToDisplay, false);
    }


    protected void showMarkers(List<Contribution> contributions, boolean reCenter) {
        Log.e("Show Markers entered", String.valueOf(reCenter));
        GraphicsOverlay graphicsOverlay;
        if (mapView.getGraphicsOverlays().isEmpty()) {
            graphicsOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(graphicsOverlay);
        } else {
            graphicsOverlay = mapView.getGraphicsOverlays().get(0);
        }
        graphicsOverlay.setSelectionColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20);
        Map<String, Object> contributionId = new HashMap<String, Object>();
        for (Contribution contribution : contributions) {
            if (contribution.getGeometry().getType().equals("Point")) {
                try {
                    contributionId.put(CONTRIBUTION_ID, contribution.getId());
                    JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                    Point pnt = (Point) GeometryEngine.project(new Point(latLngCoordinates.getDouble(0), latLngCoordinates.getDouble(1)), SpatialReferences.getWgs84());
                    pnt = (Point) GeometryEngine.project(pnt, SpatialReferences.getWebMercator());
                    Graphic graphic = new Graphic(pnt, contributionId, sms);
                    graphicsOverlay.getGraphics().add(graphic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (reCenter) {
            disposables.add(
                    Observable.timer(500, TimeUnit.MILLISECONDS).subscribe(__ -> {
                        if (graphicsOverlay.getGraphics().size() > 1)
                            mapView.setViewpointGeometryAsync(graphicsOverlay.getExtent(), 70).addDoneListener(() -> dbClient.insertLog(Logger.INITIAL_EXTENT));
                        else if (graphicsOverlay.getGraphics().size() == 1)
                            mapView.setViewpointCenterAsync(graphicsOverlay.getExtent().getCenter(), 3000).addDoneListener(() -> dbClient.insertLog(Logger.INITIAL_EXTENT));
                    }, e -> Log.e("Set Viewpoint", e.getMessage())));
        }

    }


    public void zoomIn(View view) {
        Viewpoint viewpoint = new Viewpoint(mapView.getVisibleArea().getExtent().getCenter(), mapView.getMapScale() / 2, mapView.getMapRotation());
        mapView.setViewpointAsync(viewpoint, 0.5f);
        dbClient.insertLog(Logger.ZOOM_IN_BUTTON);

    }

    public void zoomOut(View view) {
        Viewpoint viewpoint = new Viewpoint(mapView.getVisibleArea().getExtent().getCenter(), mapView.getMapScale() * 2, mapView.getMapRotation());
        mapView.setViewpointAsync(viewpoint, 0.5f);
        dbClient.insertLog(Logger.ZOOM_OUT_BUTTON);

    }

    public void rotateNorth(View view) {
        mapView.setViewpointRotationAsync(resetAngle);
        dbClient.insertLog(Logger.ROTATE_BUTTON);
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
        ImageButton northButton = findViewById(R.id.rotate_north_btn);
        disposables.add(dbClient.getProjectProperties()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(projectProperties -> {
                            switch (projectProperties.getUpDirection()) {
                                case "east":
                                    resetAngle = 90;
                                    northButton.setImageDrawable(getResources().getDrawable(R.drawable.east));
                                    break;
                                case "south":
                                    resetAngle = 180;
                                    northButton.setImageDrawable(getResources().getDrawable(R.drawable.south));
                                    break;
                                case "west":
                                    resetAngle = 270;
                                    northButton.setImageDrawable(getResources().getDrawable(R.drawable.west));
                                    break;
                                default:
                                    northButton.setImageDrawable(getResources().getDrawable(R.drawable.north));

                            }
                            mapView.setViewpointRotationAsync(resetAngle);

                            // TODO: For now other projectProperties.getShowFields() are ignored, they will be implemented as needed in the future
                            new ValueController(this, getRecyclerView(), disposables, dbClient, null);
                        }


                ));
        // Listener on change in map load status
        map.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.e("LoadStatus", map.getLoadStatus().name());
                disposables.add(
                        db.contributionDao().getContributions(projectId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .subscribe(contributions -> showMarkers(contributions, true))); // initially load all markers
                disposables.add(db.projectInfoDao().getMapPath(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(OfflineMapsActivity.this::createMosaicDataset));
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        float eventX = ev.getX();
        float eventY = ev.getY();
        int[] posXY = new int[2];

        getRecyclerView().getLocationOnScreen(posXY);
        int viewX = posXY[0];
        int viewY = posXY[1];

        boolean isOnView = eventX > viewX && eventX < viewX + getRecyclerView().getWidth() &&
                eventY > viewY && eventY < viewY + getRecyclerView().getHeight() &&
                getRecyclerView().getVisibility() == View.VISIBLE;

        if (isOnView)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        return super.dispatchTouchEvent(ev);
    }

    private RecyclerView getRecyclerView() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContainerNavigationMenu);
        if (fragment instanceof NavigationFragment)
            return ((NavigationFragment) fragment).rvNavigation;
        else
            return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        mapView.dispose();
    }


    public void createMosaicDataset(String rasterPath) {
        map.getOperationalLayers().clear();
        MosaicDatasetRaster mosaicDatasetRaster;
        mosaicDatasetRaster = MosaicDatasetRaster.create(MediaHelpers.dataPath + DB_NAME, RASTER_NAME, SpatialReferences.getWebMercator());
        mosaicDatasetRaster.loadAsync();
        mosaicDatasetRaster.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.d("CREATED", mosaicDatasetRaster.getLoadStatus().name());
                if (mosaicDatasetRaster.getLoadStatus() == LoadStatus.LOADED) {
                    Log.d("CREATED", "successful");
                    AddRastersParameters parameters = new AddRastersParameters();
                    parameters.setInputDirectory(rasterPath);
                    mosaicDatasetRaster.addRastersAsync(parameters).addDoneListener(new Runnable() {

                        @Override
                        public void run() {
                            if (mosaicDatasetRaster.getLoadStatus() == LoadStatus.LOADED) {
                                showRasters();
                            } else if (mosaicDatasetRaster.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                                Log.e("FAILED TO LOAD", mosaicDatasetRaster.getLoadError().getAdditionalMessage());
                            }
                        }
                    });
                } else if (mosaicDatasetRaster.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    Log.e("FAILED TO LOAD", mosaicDatasetRaster.getLoadError().getAdditionalMessage());
                    mosaicDatasetRaster.retryLoadAsync();
                }
            }
        });
    }

    public void showRasters() {
        if (!(new File(MediaHelpers.dataPath + DB_NAME).exists())) {
            disposables.add(db.projectInfoDao().getMapPath(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(OfflineMapsActivity.this::createMosaicDataset));
        } else {
            MosaicDatasetRaster mosaicDatasetRaster = new MosaicDatasetRaster(MediaHelpers.dataPath + DB_NAME, RASTER_NAME);
            RasterLayer mosaicDatasetRasterLayer = new RasterLayer(mosaicDatasetRaster);
            map.getOperationalLayers().add(mosaicDatasetRasterLayer);
            map.loadAsync();
            map.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    Log.e("LAYERS", "SHOWN");
                }
            });
        }
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


    public CompositeDisposable getDisposables() {
        return disposables;
    }

    public DatabaseClient getDbClient() {
        return dbClient;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Handler handler = new Handler();

            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    backCounter = 0;
                }
            };

            handler.postDelayed(mRunnable, 2000);

            if (backCounter < 2)
                backCounter++;
            else {
                backCounter = 0;
                TokenManager.getInstance().deleteActiveProject();
                finish();
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onShowClicked(List<LookUpValue> lookUpValues) {
        drawer.closeDrawer(Gravity.LEFT);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirmation);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        RecyclerView rv = dialog.findViewById(R.id.rvConfirm);
        rv.setAdapter(new ConfirmRVAdapter(lookUpValues));
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        dialog.findViewById(R.id.imgConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Filter selection succeeded!", Toast.LENGTH_SHORT).show();
        });

        dialog.findViewById(R.id.imgReselect).setOnClickListener(v -> {
            dialog.dismiss();
            drawer.openDrawer(Gravity.LEFT);
        });

        dialog.show();
    }

    private class ConfirmRVAdapter extends RecyclerView.Adapter<ConfirmRVAdapter.ViewHolder> {

        private final List<LookUpValue> lookupValues;

        public ConfirmRVAdapter(List<LookUpValue> lookUpValues) {
            this.lookupValues = lookUpValues;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public ConfirmRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_value, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ConfirmRVAdapter.ViewHolder holder, int position) {
            String path = MediaHelpers.dataPath + lookupValues.get(position).getSymbol();
            if (MediaHelpers.isRasterImageFileName(path)) {
                Glide.with(holder.itemView.getContext())
                        .asBitmap()
                        .load(MediaHelpers.dataPath + lookupValues.get(position).getSymbol())
                        .into(holder.value_image);
            } else if (MediaHelpers.isVectorImageFileName(path)) {
                Glide.with(holder.itemView.getContext())
                        .asDrawable()
                        .load(MediaHelpers.svgToDrawable(path))
                        .into(holder.value_image);
            }
        }

        @Override
        public int getItemCount() {
            return lookupValues.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            ImageView value_image;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                value_image = itemView.findViewById(R.id.value_image);
            }
        }
    }
}


