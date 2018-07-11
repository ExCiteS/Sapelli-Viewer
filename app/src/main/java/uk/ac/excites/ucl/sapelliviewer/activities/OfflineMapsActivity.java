package uk.ac.excites.ucl.sapelliviewer.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

import uk.ac.excites.ucl.sapelliviewer.R;

public class OfflineMapsActivity extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_map);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mapView = findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS_NIGHT_VECTOR, 34.056295, -117.195800, 10);
        mapView.setMap(map);

    }
}
