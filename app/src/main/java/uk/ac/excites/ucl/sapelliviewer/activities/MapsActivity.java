package uk.ac.excites.ucl.sapelliviewer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.QuickHullLatLng;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private int project_id;
    private AppDatabase db;
    private CompositeDisposable disposables;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        project_id = getIntent().getIntExtra(SettingsActivity.PROJECT_ID, 0);
        TokenManager tokenManager = TokenManager.getInstance();
        disposables = new CompositeDisposable();
        db = AppDatabase.getAppDatabase(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        disposables.add(
                db.contributionDao().getContributions(project_id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Contribution>>() {
                            @Override
                            public void onSuccess(List<Contribution> contributions) {
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                List<LatLng> points = new ArrayList<LatLng>();
                                for (Contribution contribution : contributions) {
                                    if (contribution.getGeometry().getType().equals("Point")) {
                                        try {
                                            JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                                            LatLng marker = new LatLng(latLngCoordinates.getDouble(1), latLngCoordinates.getDouble(0));
                                            points.add(marker);
                                            builder.include(marker);


//                                            getContributionProperties(contribution, marker);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                /* ADD HEATMAP */
                                // Create the gradient.
                                if (!points.isEmpty()) {

                                    int[] colors = {
//                                        Color.rgb(153, 194, 255), // green
                                            Color.rgb(0, 102, 255)    // red
                                    };

                                    float[] startPoints = {
                                            1f
                                    };

                                    Gradient gradient = new Gradient(colors, startPoints);

                                    HeatmapTileProvider provider = new HeatmapTileProvider.Builder().data(points).gradient(gradient).build();
                                    map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

                                    LatLngBounds bounds = builder.build();
                                    int padding = 100; // offset from edges of the map in pixels
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                    map.moveCamera(cu);
                                }


                                QuickHullLatLng hullLatLng = new QuickHullLatLng();
                                ArrayList<LatLng> convHull = new ArrayList<LatLng>();
                                if (points.size() > 2)
                                    convHull = hullLatLng.quickHull(points);
                                if (!convHull.isEmpty())
                                    map.addPolygon(new PolygonOptions().addAll(convHull).strokeWidth(0).fillColor(Color.rgb(153, 194, 255)));


                                Log.d("hull", " created");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getContributions", e.getMessage());
                            }
                        })
        );
    }

    public void getContributionProperties(Contribution contribution, LatLng marker) {
        disposables.add(
                db.contributionDao().getContributionsProperties(contribution.getId())
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ContributionProperty>>() {
                            @Override
                            public void onSuccess(List<ContributionProperty> contributionProperties) {
                                for (ContributionProperty contributionProperty : contributionProperties) {
                                    if (contributionProperty.getSymbol() != null) {
                                        String url = getFilesDir() + File.separator + (contributionProperty.getSymbol().split("/")[3]);
                                        Bitmap icon = BitmapFactory.decodeFile(url);


                                        map.addMarker(new MarkerOptions().position(marker).title(contributionProperty.getValue())
                                                .icon(BitmapDescriptorFactory.fromBitmap(icon)));

                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getContribProperties", e.getMessage());
                            }
                        })
        );
    }

    public static LatLng getCenter(List<LatLng> points) {
        double totalLatitude = 0;
        double totalLongitude = 0;
        for (LatLng point : points) {
            totalLatitude += point.latitude;
            totalLongitude += point.longitude;
        }

        return new LatLng(totalLatitude / points.size(), totalLongitude / points.size());
    }


}
