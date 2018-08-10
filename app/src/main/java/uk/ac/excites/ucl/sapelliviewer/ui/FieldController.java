package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ToggleButton;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;

class FieldController extends DatabaseClient {

    private Context context;
    private ValueController valueController;
    private ValueAdapter valueAdapter;
    private CompositeDisposable disposibles;
    private RecyclerView fieldRecyclerView;

    FieldController(Context context, RecyclerView fieldRecyclerView, ValueController valueController, int projectId, CompositeDisposable disposibles) {
        super(context);
        this.context = context;
        this.valueController = valueController;
        this.valueAdapter = valueController.getAdapter();
        this.disposibles = disposibles;
        this.fieldRecyclerView = fieldRecyclerView;
        setAdapter(projectId);
    }


    private void setAdapter(int projectId) {
        disposibles.add(
                getFields(projectId)
                        .subscribeOn(Schedulers.io())
                        .toObservable().flatMapIterable(fields -> fields)
                        .filter(field -> !field.getKey().equals("DeviceId") && !field.getKey().equals("StartTime") && !field.getKey().equals("EndTime"))
                        .toList()
                        .subscribeWith(new DisposableSingleObserver<List<Field>>() {

                            @Override
                            public void onSuccess(List<Field> fields) {
                                FieldAdapter fieldAdapter = new FieldAdapter(context, fields, new FieldAdapter.FieldCheckedChangeListener() {
                                    @SuppressLint("CheckResult")
                                    @Override
                                    public void checkedChanged(ToggleButton buttonView, boolean isChecked, Field field) {
                                        for (LookUpValue lookUpValue : valueAdapter.getAllLookUpValues()) {
                                            if (lookUpValue.getFieldId() == field.getId())
                                                lookUpValue.setVisible(isChecked);
                                        }
                                        if (isChecked) {
                                            buttonView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                                        } else {
                                            buttonView.setBackgroundColor(Color.WHITE);
                                        }
                                        valueAdapter.notifyDataSetChanged();
                                        loadMarkers(valueAdapter.getVisibleAndActiveLookupValues()).subscribe(valueController::updateMarkers);
                                    }
                                });
                                fieldRecyclerView.setAdapter(fieldAdapter);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getFields", e.getMessage());
                            }
                        }));
    }


}
