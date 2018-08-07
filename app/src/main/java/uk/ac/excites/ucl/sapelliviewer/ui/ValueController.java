package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import uk.ac.excites.ucl.sapelliviewer.activities.OfflineMapsActivity;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.service.DatabaseClient;

public class ValueController extends DatabaseClient {

    private OfflineMapsActivity mapsActivity;
    private ValueAdapter valueAdapter;
    private RecyclerView valueRecyclerView;
    private CompositeDisposable disposables;
    private RecyclerView fieldRecyclerView;


    public ValueController(OfflineMapsActivity mapActivity, RecyclerView valueRecyclerView, CompositeDisposable disposables) {
        super(mapActivity.getContext());
        this.mapsActivity = mapActivity;
        this.valueRecyclerView = valueRecyclerView;
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(mapActivity.getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.disposables = disposables;
        getValueAdapter();
    }

    private void getValueAdapter() {
        disposables.add(
                getLookUpValues(mapsActivity.getProjectId())
                        .subscribeWith(new DisposableSingleObserver<List<LookUpValue>>() {
                            @Override
                            public void onSuccess(List<LookUpValue> lookUpValues) {
                                valueAdapter = new ValueAdapter(mapsActivity.getContext(), lookUpValues, (v, value) -> {
                                    value.setActive(!value.isActive());
                                    valueAdapter.notifyDataSetChanged();
                                    loadMarkers(valueAdapter.getVisibleAndActiveLookupValues()).subscribe(contributions -> updateMarkers(contributions));
                                });
                                valueRecyclerView.setAdapter(valueAdapter);
                                if (fieldRecyclerView != null)
                                    new FieldController(mapsActivity.getContext(), fieldRecyclerView, ValueController.this, mapsActivity.getProjectId(), disposables);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getLookupvalues", e.getMessage());
                            }
                        }));
    }

    public void addFieldController(RecyclerView recyclerView) {
        this.fieldRecyclerView = recyclerView;
        fieldRecyclerView.setLayoutManager(new LinearLayoutManager(mapsActivity.getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    public ValueAdapter getAdapter() {
        return valueAdapter;
    }

    public void updateMarkers(List<Contribution> contributions) {
        mapsActivity.updateMarkers(contributions);
    }

}
