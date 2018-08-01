package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    private Context context;
    private List<Field> fields;
    private FieldCheckedChangeListener listener;

    public FieldAdapter(Context context, List<Field> fields, FieldCheckedChangeListener fieldCheckedChangeListener) {
        this.context = context;
        this.fields = fields;
        this.listener = fieldCheckedChangeListener;
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.field_item, parent, false);
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.field.setTextOff(fields.get(position).getName());
        holder.field.setTextOn(fields.get(position).getName());
        holder.field.setChecked(true);
        holder.field.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class FieldViewHolder extends RecyclerView.ViewHolder {

        ToggleButton field;

        public FieldViewHolder(View itemView) {
            super(itemView);
            field = itemView.findViewById(R.id.field_txt);
            field.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.checkedChanged((ToggleButton) buttonView, isChecked, fields.get(getAdapterPosition()));
                }
            });

        }
    }

    public interface FieldCheckedChangeListener {
        void checkedChanged(ToggleButton buttonView, boolean isChecked, Field field);
    }
}
