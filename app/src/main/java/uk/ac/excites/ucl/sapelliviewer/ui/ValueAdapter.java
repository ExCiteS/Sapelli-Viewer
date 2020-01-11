package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.ValueListViewHolder> {

    //    private ArrayList<RecyclerViewItemData> items = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private LinkedHashMap<Integer, ArrayList<LookUpValue>> valueMap = new LinkedHashMap<>();
    private Context context;
    private List<LookUpValue> lookUpValues;
    private ValueAdapterClickListener listener;


    public ValueAdapter(Context context, List<LookUpValue> lookUpValues, ValueAdapterClickListener listener) {
        this.context = context;
        this.lookUpValues = lookUpValues;
        this.listener = listener;

        if (lookUpValues != null && lookUpValues.size() > 0) {
            int fieldId = lookUpValues.get(0).getFieldId();
            ArrayList<LookUpValue> list = new ArrayList<>();
            for (LookUpValue value : getVisibleLookupValues()) {
                if (fieldId == value.getFieldId()) {
                    list.add(value);
                } else {
                    valueMap.put(fieldId, list);
                    list = new ArrayList<>();
                    list.add(value);
                    fieldId = value.getFieldId();
                }
            }
        }
    }

    @NonNull
    @Override
    public ValueListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_value_list, parent, false);
        return new ValueAdapter.ValueListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ValueListViewHolder holder, int position) {
        holder.bindTo(valueMap.get((valueMap.keySet().toArray())[position]));
    }

    @Override
    public int getItemCount() {
        return valueMap.keySet().size();
    }

    List<LookUpValue> getAllLookUpValues() {
        return lookUpValues;
    }

    List<LookUpValue> getVisibleLookupValues() {
        List<LookUpValue> filteredList = new ArrayList<>();
        for (LookUpValue value : lookUpValues) {
            if (value.isVisible())
                filteredList.add(value);
        }
        return filteredList;
    }

    List<LookUpValue> getVisibleAndActiveLookupValues() {
        List<LookUpValue> filteredList = new ArrayList<>();
        for (LookUpValue value : lookUpValues) {
            if (value.isVisible() && value.isActive())
                filteredList.add(value);
        }
        return filteredList;
    }

    public interface ValueAdapterClickListener {
        void onClick(View v, LookUpValue value);
    }

    class ValueListViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvValueList;

        ValueListViewHolder(@NonNull View itemView) {
            super(itemView);
            rvValueList = itemView.findViewById(R.id.rvValueList);
        }

        void bindTo(ArrayList<LookUpValue> lookUpValues) {
            if (lookUpValues == null) return;
            if (lookUpValues.size() == 0) return;

            rvValueList.setLayoutManager(new LinearLayoutManager(rvValueList.getContext(), LinearLayoutManager.HORIZONTAL,false));
            rvValueList.setAdapter(new ValueListAdapter(lookUpValues));
        }

        private class ValueListAdapter extends RecyclerView.Adapter<ValueListAdapter.ValueViewHolder> {
            private final ArrayList<LookUpValue> lookUpValues;

            ValueListAdapter(ArrayList<LookUpValue> lookUpValues) {
                this.lookUpValues = lookUpValues;
            }

            @NonNull
            @Override
            public ValueListAdapter.ValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ValueViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_value, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ValueListAdapter.ValueViewHolder holder, int position) {
                holder.bindTo(lookUpValues.get(position));
            }

            @Override
            public int getItemCount() {
                return lookUpValues.size();
            }

            class ValueViewHolder extends RecyclerView.ViewHolder {
                ImageView valueImage;
                View valueFrame;

                ValueViewHolder(View itemView) {
                    super(itemView);
                    valueFrame = itemView.findViewById(R.id.value_frame);
                    valueImage = itemView.findViewById(R.id.value_image);
                }

                void bindTo(LookUpValue value) {

                    String path = MediaHelpers.dataPath + value.getSymbol();
                    if (MediaHelpers.isRasterImageFileName(path)) {
                        Glide.with(context)
                                .asBitmap()
                                .load(MediaHelpers.dataPath + value.getSymbol())
                                .into(valueImage);
                    } else if (MediaHelpers.isVectorImageFileName(path)) {
                        Glide.with(context)
                                .asDrawable()
                                .load(MediaHelpers.svgToDrawable(path))
                                .into(valueImage);
                    }

                    if (value.isActive())
                        valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
                    else
                        valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.background_dark, null));

                    valueImage.setOnClickListener(v -> {
//                    listener.onClick(v, getVisibleLookupValues().get(getAdapterPosition()));
                    });
                }
            }
        }
    }
}

