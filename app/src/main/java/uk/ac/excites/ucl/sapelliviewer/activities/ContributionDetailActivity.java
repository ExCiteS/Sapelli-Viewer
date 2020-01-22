package uk.ac.excites.ucl.sapelliviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import uk.ac.excites.ucl.sapelliviewer.R;

import static uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity.CONTRIBUTION_ID;

public class ContributionDetailActivity extends AppCompatActivity {

    public static Intent newIntent(Context context, int contributionId) {
        Intent intent = new Intent(context, ContributionDetailActivity.class);
        intent.putExtra(CONTRIBUTION_ID, contributionId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribution_detail);
    }
}
