package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.android.map.Callout;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;

import static uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers.createView;


/**
 * Created by julia
 */
public class OfflineMapsActivity extends BaseMapsActivity {
    // static variables
    public static String IMG_PATH = "img_path";


    private MapView map;
    private GraphicsLayer markerLayer = new GraphicsLayer();
    private Callout callout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set full screen and landscape
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.offline_map);
        map = (MapView) findViewById(R.id.map);

        disposables.add(
                db.projectInfoDao().getMapPath(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<String>() {
                            @Override
                            public void onSuccess(String path) {
                                ArcGISLocalTiledLayer tpk = new ArcGISLocalTiledLayer(path);
                                map.addLayer(tpk);
                                map.addLayer(markerLayer);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getMapPath", e.getMessage());

                            }
                        }));


        map.setOnSingleTapListener(new OnSingleTapListener() {

            private static final long serialVersionUID = 1L;

            public void onSingleTap(float x, float y) {

                if (!map.isLoaded())
                    return;
                int[] uids = markerLayer.getGraphicIDs(x, y, 2);
                if (uids != null && uids.length > 0) {

                    int targetId = uids[0];
                    Graphic gr = markerLayer.getGraphic(targetId);
                    callout = map.getCallout();

                    // Sets Callout style
                    callout.setStyle(R.layout.popup);

                    // Sets custom content view to Callout
                    callout.setContent(loadView(gr));
                    // map.centerAt(new Point(x, y), true);
                    callout.show(map.toMapPoint(new Point(x, y)));
                } else {
                    if (callout != null && callout.isShowing()) {
                        callout.hide();
                    }
                }

            }
        });

        // when map is initialised
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            private static final long serialVersionUID = 1L;

            // Once map is loaded set scale
            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == map && status == STATUS.INITIALIZED) {
                    map.setMapBackground(Color.WHITE, Color.WHITE, 0, 0);
                    markerLayer.removeAll();
                    disposables.add(
                            db.contributionDao().getContributions(projectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                    .subscribeWith(new DisposableSingleObserver<List<Contribution>>() {
                                        @Override
                                        public void onSuccess(List<Contribution> contributions) {
                                            loadMarkers(contributions);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e("getContributions", e.getMessage());

                                        }
                                    }));
                }
            }
        });
    }


    protected void loadMarkers(List<Contribution> contributions) {
        SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 40, SimpleMarkerSymbol.STYLE.CIRCLE);
        Map<String, Object> paths = new HashMap<String, Object>();

        for (Contribution contribution : contributions) {
            if (contribution.getGeometry().getType().equals("Point") && contribution.getContributionProperty() != null && contribution.getContributionProperty().getSymbol() != null) {
                try {
                    paths.put(IMG_PATH, "/data/data/" + getPackageName() + contribution.getContributionProperty().getSymbol());
                    JSONArray latLngCoordinates = new JSONArray(contribution.getGeometry().getCoordinates());
                    Point pnt = new Point(latLngCoordinates.getDouble(0), latLngCoordinates.getDouble(1));
                    Graphic graphic = new Graphic(GeometryEngine.project(pnt, SpatialReference.create(SpatialReference.WKID_WGS84), map.getSpatialReference()), sms, paths);
                    markerLayer.addGraphic(graphic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        if (markerLayer.getGraphicIDs() != null) {
            Envelope point = new Envelope();
            Envelope allPoints = new Envelope();
            for (int i : markerLayer.getGraphicIDs()) {
                Point p = (Point) markerLayer.getGraphic(i).getGeometry();
                p.queryEnvelope(point);
                allPoints.merge(point);
            }
            map.setExtent(allPoints, 100, false);


        }
    }


    // Creates custom content view with 'Graphic' attributes
    private View loadView(Graphic graphic) {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);

        Set<String> keys = graphic.getAttributes().keySet();

        for (String key : keys) {
            if (key.equals(IMG_PATH)) {
                ImageView imgView = (ImageView) view.findViewById(R.id.photo_view);
                createView(imgView, graphic.getAttributes().get(key).toString());
            }
        }

        return view;

    }


    public void zoomIn(View view) {
        map.zoomin();
    }

    public void zoomOut(View view) {
        map.zoomout();
    }


}


