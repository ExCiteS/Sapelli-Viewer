package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.db.DatabaseClient;
import uk.ac.excites.ucl.sapelliviewer.utils.Logger;

class FieldController{

    private final DatabaseClient dbClient;
    private Context context;
    private ValueController valueController;
    private ValueAdapter valueAdapter;
    private CompositeDisposable disposibles;
    private RecyclerView fieldRecyclerView;
    private ImageButton toggleOnButton;
    private ImageButton toggleOffButton;

    FieldController(Context context, RecyclerView fieldRecyclerView, ValueController valueController, int projectId, CompositeDisposable disposibles, DatabaseClient dbClient) {
        this.context = context;
        this.valueController = valueController;
        this.valueAdapter = valueController.getAdapter();
        this.disposibles = disposibles;
        this.fieldRecyclerView = fieldRecyclerView;
        this.dbClient = dbClient;
        setAdapter(projectId);
    }


    private void setAdapter(int projectId) {
        disposibles.add(
                dbClient.getFields()
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
                                            dbClient.insertLog(Logger.FIELD_CHECKED + field.getId());

                                        } else {
                                            buttonView.setBackgroundColor(Color.WHITE);
                                            dbClient.insertLog(Logger.FIELD_UNCHECKED + field.getId());

                                        }
                                        valueAdapter.notifyDataSetChanged();
                                        dbClient.loadMarkers(valueAdapter.getVisibleAndActiveLookupValues()).subscribe(valueController::updateMarkers);
                                    }
                                });
                                fieldRecyclerView.setAdapter(fieldAdapter);
                                if (!fields.isEmpty()) {
                                    toggleOffButton.setVisibility(View.VISIBLE);
                                    toggleOnButton.setVisibility(View.VISIBLE);
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getFields", e.getMessage());
                            }
                        }));
    }

    public void setToggleAllValuesButtons(ImageButton toggleOnButton, ImageButton toggleOffButton) {
        this.toggleOnButton = toggleOnButton;
        this.toggleOffButton = toggleOffButton;
        toggleOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOffValues();
            }
        });
        toggleOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOnValues();
            }
        });
    }

    private void toggleOffValues() {
        for (LookUpValue value : valueAdapter.getVisibleLookupValues()) {
            value.setActive(false);
        }
        valueAdapter.notifyDataSetChanged();
        valueController.updateMarkers(new ArrayList<Contribution>());
        dbClient.insertLog(Logger.TOGGLE_ALL_OFF);
    }

    private void toggleOnValues() {
        for (LookUpValue value : valueAdapter.getVisibleLookupValues()) {
            value.setActive(true);
        }
        valueAdapter.notifyDataSetChanged();
        dbClient.loadMarkers(valueAdapter.getVisibleLookupValues()).subscribe(valueController::updateMarkers);
        dbClient.insertLog(Logger.TOGGLE_ALL_ON);
    }

}