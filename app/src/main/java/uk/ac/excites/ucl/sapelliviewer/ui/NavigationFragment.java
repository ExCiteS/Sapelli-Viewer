package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;

public class NavigationFragment extends Fragment {

    public RecyclerView rvNavigation;
    private OnShowClickListener listener;
    private ValueAdapter valueAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShowClickListener)
            this.listener = (OnShowClickListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        rvNavigation = view.findViewById(R.id.rvNavigation);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnShow).setOnClickListener(v -> {
            if (listener != null) {
                RecyclerView.Adapter adapter = rvNavigation.getAdapter();
                if (adapter instanceof ValueAdapter) {
                    valueAdapter = (ValueAdapter) adapter;
                    listener.onShowClicked(valueAdapter.getAllActiveValues());
                }
            }
        });
        CheckBox checkBoxSelectAll = view.findViewById(R.id.cbSelectAll);
        checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RecyclerView.Adapter adapter = rvNavigation.getAdapter();
            if (adapter instanceof ValueAdapter) {
                valueAdapter = (ValueAdapter) adapter;
                valueAdapter.selectAll(isChecked);
            }
            Log.d("asd", "onViewCreated: ");
        });
    }

    public interface OnShowClickListener {
        void onShowClicked(List<LookUpValue> lookUpValues);
    }
}
