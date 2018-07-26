package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.ValueViewHolder> {

    private Context context;
    private List<LookUpValue> lookUpValues;
    private ValueAdapterClickListener onClickListener; // TODO: implement


    public ValueAdapter(Context context, List<LookUpValue> lookUpValues) {
        this.context = context;
        this.lookUpValues = lookUpValues;
    }

    @NonNull
    @Override
    public ValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.value_item, parent, false);
        return new ValueAdapter.ValueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ValueViewHolder holder, int position) {

        List<LookUpValue> values = getFilteredLookupValues();
        String path = MediaHelpers.dataPath + values.get(position).getSymbol();
        if (MediaHelpers.isRasterImageFileName(path)) {
            Glide.with(context)
                    .asBitmap()
                    .load(MediaHelpers.dataPath + values.get(position).getSymbol())
                    .into(holder.valueImage);
        } else if (MediaHelpers.isVectorImageFileName(path)) {
            Glide.with(context)
                    .asDrawable()
                    .load(MediaHelpers.svgToDrawable(path))
                    .into(holder.valueImage);
        }
    }


    @Override
    public int getItemCount() {
        return getFilteredLookupValues().size();
    }

    public List<LookUpValue> getAllLookUpValues() {
        return lookUpValues;
    }

    private List<LookUpValue> getFilteredLookupValues() {
        List<LookUpValue> filteredList = new ArrayList<>();
        for (LookUpValue value : lookUpValues) {
            if (value.isActive())
                filteredList.add(value);
        }
        return filteredList;
    }

    class ValueViewHolder extends RecyclerView.ViewHolder {
        ImageView valueImage;

        ValueViewHolder(View itemView) {
            super(itemView);
            valueImage = itemView.findViewById(R.id.value_image);
        }
    }

    public interface ValueAdapterClickListener {
        void onClick(View v, int position);

    }
}
