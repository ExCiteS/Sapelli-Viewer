package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.widget.ToggleButton;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.service.DatabaseClient;

public class FieldController extends DatabaseClient {

    private Context context;
    private ValueAdapter valueAdapter;

    public FieldController(Context context, ValueAdapter valueAdapter) {
        super(context);
        this.context = context;
        this.valueAdapter = valueAdapter;

    }

    public Single<FieldAdapter> getAdapter(int projectId) {
        return getFields(projectId)
                .subscribeOn(Schedulers.io())
                .toObservable().flatMapIterable(fields -> fields)
                .filter(field -> !field.getKey().equals("DeviceId") && !field.getKey().equals("StartTime") && !field.getKey().equals("EndTime"))
                .toList()
                .map(fields -> new FieldAdapter(context, fields, new FieldAdapter.FieldCheckedChangeListener() {
                            @Override
                            public void checkedChanged(ToggleButton buttonView, boolean isChecked, Field field) {
                                for (LookUpValue lookUpValue : valueAdapter.getAllLookUpValues()) {
                                    if (lookUpValue.getFieldId() == field.getId())
                                        lookUpValue.setVisible(isChecked);
                                }
                            }
                        }
                        )
                )
                .observeOn(AndroidSchedulers.mainThread());


    }

}
