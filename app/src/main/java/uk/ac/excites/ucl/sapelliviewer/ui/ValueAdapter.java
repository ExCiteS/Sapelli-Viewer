package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
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
    private ValueAdapterClickListener listener; // TODO: implement


    public ValueAdapter(Context context, List<LookUpValue> lookUpValues, ValueAdapterClickListener listener) {
        this.context = context;
        this.lookUpValues = lookUpValues;
        this.listener = listener;

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

        List<LookUpValue> values = getVisibleLookupValues();
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

        if (values.get(position).isActive())
            holder.valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
        else
            holder.valueFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.background_dark, null));
    }


    @Override
    public int getItemCount() {
        return getVisibleLookupValues().size();
    }

    public List<LookUpValue> getAllLookUpValues() {
        return lookUpValues;
    }

    private List<LookUpValue> getVisibleLookupValues() {
        List<LookUpValue> filteredList = new ArrayList<>();
        for (LookUpValue value : lookUpValues) {
            if (value.isVisible())
                filteredList.add(value);
        }
        return filteredList;
    }

    public List<LookUpValue> getVisibleAndActiveLookupValues() {
        List<LookUpValue> filteredList = new ArrayList<>();
        for (LookUpValue value : lookUpValues) {
            if (value.isVisible() && value.isActive())
                filteredList.add(value);
        }
        return filteredList;
    }

    class ValueViewHolder extends RecyclerView.ViewHolder {
        ImageView valueImage;
        View valueFrame;

        ValueViewHolder(View itemView) {
            super(itemView);
            valueFrame = itemView.findViewById(R.id.value_frame);
            valueImage = itemView.findViewById(R.id.value_image);
            valueImage.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v, getVisibleLookupValues().get(getAdapterPosition()));
                }
            });
        }
    }

    public interface ValueAdapterClickListener {
        void onClick(View v, LookUpValue value);

    }
}

