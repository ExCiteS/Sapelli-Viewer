package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
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

    public ValueController(OfflineMapsActivity mapActivity, RecyclerView valueRecyclerView, CompositeDisposable disposables, DatabaseClient dbClient, Integer displayField) {
        this.dbClient = dbClient;
        this.mapsActivity = mapActivity;
        this.valueRecyclerView = valueRecyclerView;
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(mapActivity.getContext(), LinearLayoutManager.VERTICAL, false));
        this.disposables = disposables;
        getValueAdapter(displayField);
    }

    private void getValueAdapter(Integer displayField) {
        disposables.add(
                dbClient.getLookUpValues(displayField)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<LookUpValue>>() {
                            @SuppressLint("CheckResult")
                            @Override
                            public void onSuccess(List<LookUpValue> lookUpValues) {
                                valueAdapter = new ValueAdapter(mapsActivity.getContext(), lookUpValues, (View v, LookUpValue value) -> {
                                    value.setActive(!value.isActive());
                                    if (value.isActive())
                                        dbClient.insertLog(Logger.VALUE_CHECKED, value.getId());
                                    else
                                        dbClient.insertLog(Logger.VALUE_UNCHECKED, value.getId());
                                });
                                valueRecyclerView.setAdapter(valueAdapter);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getLookupvalues", e.getMessage());
                            }
                        }));
    }

    public ValueAdapter getAdapter() {
        return valueAdapter;
    }

    public void updateMarkers(List<Contribution> contributions) {
        mapsActivity.updateMarkers(contributions);
    }
}
