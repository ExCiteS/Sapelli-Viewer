package uk.ac.excites.ucl.sapelliviewer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.ValueListViewHolder> {

    @SuppressLint("UseSparseArrays")
    private LinkedHashMap<Integer, ArrayList<LookUpValue>> valueMap = new LinkedHashMap<>();
    private Context context;
    private List<LookUpValue> lookUpValues;
    private ValueAdapterClickListener listener;


    public ValueAdapter(Context context, List<LookUpValue> lookUpValues, ValueAdapterClickListener listener) {
        this.context = context;
        this.lookUpValues = lookUpValues;
        this.listener = listener;

        if (lookUpValues != null && getVisibleLookupValues().size() > 0) {
            int fieldId = getVisibleLookupValues().get(0).getFieldId();
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
            valueMap.put(fieldId, list);
        }
    }

    public ArrayList<LookUpValue> getAllActiveValues() {
        if (valueMap.keySet().size() == 0) return null;

        ArrayList<LookUpValue> activeOnes = new ArrayList<>();

        for (ArrayList<LookUpValue> values : valueMap.values()) {
            for (LookUpValue v : values) {
                if (v.isActive()) activeOnes.add(v);
            }
        }

        if (activeOnes.size() == 0) return null;

        return activeOnes;
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
        holder.bindTo(valueMap.get((valueMap.keySet().toArray())[position]), listener);
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

    void selectAll(boolean isSelect) {
        for (LookUpValue value : getAllLookUpValues()) {
            value.setActive(isSelect);
        }
        notifyDataSetChanged();
    }

    public interface ValueAdapterClickListener {
        void onClick(View v, LookUpValue value);
    }

    private interface OnValueClickListener {
        void onValueClicked(View v, LookUpValue value, int position);
    }

    class ValueListViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvValueList;

        ValueListViewHolder(@NonNull View itemView) {
            super(itemView);
            rvValueList = itemView.findViewById(R.id.rvValueList);
        }


        void bindTo(ArrayList<LookUpValue> lookUpValues, ValueAdapterClickListener listener) {
            if (lookUpValues == null) return;
            if (lookUpValues.size() == 0) return;

            rvValueList.setLayoutManager(new LinearLayoutManager(rvValueList.getContext(), LinearLayoutManager.HORIZONTAL, false));

            ValueListAdapter adapter = new ValueListAdapter();
            OnValueClickListener onValueClickListener = (v, value, position) -> {
                listener.onClick(v, value);
                adapter.notifyItemChanged(position);
            };
            adapter.initAdapter(lookUpValues, onValueClickListener);
            rvValueList.setAdapter(adapter);
        }

        private class ValueListAdapter extends RecyclerView.Adapter<ValueListAdapter.ValueViewHolder> {
            private ArrayList<LookUpValue> lookUpValues;
            private OnValueClickListener listener;

            @NonNull
            @Override
            public ValueListAdapter.ValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ValueViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_value, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ValueListAdapter.ValueViewHolder holder, int position) {
                holder.bindTo(lookUpValues.get(position), listener);
            }

            @Override
            public int getItemCount() {
                return lookUpValues.size();
            }

            void initAdapter(ArrayList<LookUpValue> lookUpValues, OnValueClickListener onValueClickListener) {
                this.lookUpValues = lookUpValues;
                this.listener = onValueClickListener;
                notifyDataSetChanged();
            }

            class ValueViewHolder extends RecyclerView.ViewHolder {
                ImageView valueImage;
                View valueFrame;

                ValueViewHolder(View itemView) {
                    super(itemView);
                    valueFrame = itemView.findViewById(R.id.value_frame);
                    valueImage = itemView.findViewById(R.id.value_image);
                }

                void bindTo(LookUpValue value, OnValueClickListener listener) {

                    String path = MediaHelpers.dataPath + value.getSymbol();
                    if (MediaHelpers.isRasterImageFileName(path)) {
                        Glide.with(context)
                                .asBitmap()
                                .load(MediaHelpers.dataPath + value.getSymbol())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        itemView.post(() -> {
                                            itemView.getLayoutParams().width = 0;
                                            itemView.requestLayout();
                                        });
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into(valueImage);
                    } else if (MediaHelpers.isVectorImageFileName(path)) {
                        Glide.with(context)
                                .asDrawable()
                                .load(MediaHelpers.svgToDrawable(path))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        itemView.post(() -> {
                                            itemView.getLayoutParams().width = 0;
                                            itemView.requestLayout();
                                        });
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into(valueImage);
                    }

                    if (value.isActive()) {
                        valueImage.setAlpha(1f);
                        valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
                    } else {
                        valueImage.setAlpha(0.5f);
                        valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.background_dark, null));
                    }

                    valueImage.setOnClickListener(v -> listener.onValueClicked(v, value, getAdapterPosition()));
                }
            }
        }
    }
}

