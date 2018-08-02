package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.service.DatabaseClient;

public class ValueController extends DatabaseClient {

    private Context context;

    public ValueController(Context context) {
        super(context);
        this.context = context;
    }


//    public Single<ValueAdapter> getValueAdapter(int projectId) {
//        return getLookUpValues(projectId)
//                .subscribeOn(Schedulers.io())
//                .map(lookUpValues -> new ValueAdapter(context,lookUpValues))
//                .observeOn(AndroidSchedulers.mainThread());
//    }


}
