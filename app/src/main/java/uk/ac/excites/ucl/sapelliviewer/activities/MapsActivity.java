package uk.ac.excites.ucl.sapelliviewer.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Point;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionCollection;
import uk.ac.excites.ucl.sapelliviewer.service.GeoKeyClient;
import uk.ac.excites.ucl.sapelliviewer.service.RetrofitBuilder;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private GeoKeyClient clientWithAuth;
    private int project_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientWithAuth = RetrofitBuilder.createServiceWithAuth(GeoKeyClient.class, TokenManager.getInstance());
        setContentView(R.layout.activity_maps);
        project_id = getIntent().getIntExtra(SettingsActivity.PROJECT_ID, 0);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getContributions(project_id);

    }

    public void getContributions(int projectId) {

        Call<ContributionCollection> call = clientWithAuth.getContributions(projectId);
        call.enqueue(new Callback<ContributionCollection>() {
            @Override
            public void onResponse(Call<ContributionCollection> call, Response<ContributionCollection> response) {
                List<Contribution> contributions = response.body().getFeatures();
                for (Contribution contribution : contributions) {
                    map.addMarker(new MarkerOptions().position(((Point) contribution.getGeometry()).getGeometryObject()).title(contribution.getDisplay_field().getKey()));
                }
            }
            @Override
            public void onFailure(Call<ContributionCollection> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "error :(", Toast.LENGTH_SHORT).show();
                System.out.println(t.getStackTrace().toString());

            }
        });

    }
}
