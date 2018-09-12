package uk.ac.excites.ucl.sapelliviewer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.ToggleButton;

import uk.ac.excites.ucl.sapelliviewer.R;


public class SettingsFragment extends Fragment {
    private static final String PROJECT_ID = "projectID";

    private int projectId;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance(int projectId) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt(PROJECT_ID);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_settings, container, false);

        /* INSTANTIATE ALL VIEWS */
        ToggleButton toggleLogging = view.findViewById(R.id.tggl_logging);
        Button exportLogs = view.findViewById(R.id.btn_logs);
        ToggleButton showFields = view.findViewById(R.id.tggl_show_fields);
        ImageButton reloadProject = view.findViewById(R.id.btn_reload);

        return view;
    }


    public void onMapDirectionChanged(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_north:
                if (checked)
                    break;
            case R.id.radio_east:
                if (checked)
                    break;
            case R.id.radio_south:
                if (checked)
                    break;
            case R.id.radio_west:
                if (checked)
                    break;
        }
    }


}
