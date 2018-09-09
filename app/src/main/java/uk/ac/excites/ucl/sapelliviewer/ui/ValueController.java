package uk.ac.excites.ucl.sapelliviewer.ui;

import android.renderscript.Sampler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;

public class ValueController {

    private final DatabaseClient dbClient;
    private OfflineMapsActivity mapsActivity;
    private ValueAdapter valueAdapter;
    private RecyclerView valueRecyclerView;
    private CompositeDisposable disposables;
    private RecyclerView fieldRecyclerView;
    private ImageButton toggleOnButton;
    private ImageButton toggleOffButton;


    public ValueController(OfflineMapsActivity mapActivity, RecyclerView valueRecyclerView, CompositeDisposable disposables, DatabaseClient dbClient) {
        this.dbClient = dbClient;
        this.mapsActivity = mapActivity;
        this.valueRecyclerView = valueRecyclerView;
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(mapActivity.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.disposables = disposables;
        getValueAdapter();
    }

    private void getValueAdapter() {
        disposables.add(
                dbClient.getLookUpValues()
                        .subscribeWith(new DisposableSingleObserver<List<LookUpValue>>() {
                            @Override
                            public void onSuccess(List<LookUpValue> lookUpValues) {
                                valueAdapter = new ValueAdapter(mapsActivity.getContext(), lookUpValues, (View v, LookUpValue value) -> {
                                    value.setActive(!value.isActive());
                                    valueAdapter.notifyDataSetChanged();
                                    dbClient.loadMarkers(valueAdapter.getVisibleAndActiveLookupValues()).subscribe(contributions -> updateMarkers(contributions));
                                    if (value.isActive())
                                        dbClient.insertLog(Logger.VALUE_CHECKED , value.getId());
                                    else dbClient.insertLog(Logger.VALUE_UNCHECKED , value.getId());


                                });
                                valueRecyclerView.setAdapter(valueAdapter);
                                FieldController fieldController;
                                if (fieldRecyclerView != null) {
                                    fieldController = new FieldController(mapsActivity.getContext(), fieldRecyclerView, ValueController.this, mapsActivity.getProjectId(), disposables, dbClient);
                                    if (toggleOffButton != null && toggleOnButton != null)
                                        fieldController.setToggleAllValuesButtons(toggleOnButton, toggleOffButton);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getLookupvalues", e.getMessage());
                            }
                        }));
    }

    public ValueController addFieldController(RecyclerView recyclerView) {
        this.fieldRecyclerView = recyclerView;
        fieldRecyclerView.setLayoutManager(new LinearLayoutManager(mapsActivity.getContext(), LinearLayoutManager.HORIZONTAL, false));
        return this;

    }

    public ValueController addToggleButtons(ImageButton toggleOnButton, ImageButton toggleOffButton) {
        this.toggleOnButton = toggleOnButton;
        this.toggleOffButton = toggleOffButton;
        return this;
    }

    public ValueAdapter getAdapter() {
        return valueAdapter;
    }

    public void updateMarkers(List<Contribution> contributions) {
        mapsActivity.updateMarkers(contributions);
    }


}
