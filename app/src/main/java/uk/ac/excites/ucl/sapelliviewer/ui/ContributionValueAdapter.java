package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ContributionValueAdapter extends RecyclerView.Adapter<ContributionValueAdapter.ContributionViewHolder> {
    private Context context;
    private List<ContributionProperty> contributionProperties;

    public ContributionValueAdapter(Context context, List<ContributionProperty> contributionProperties) {
        this.context = context;
        this.contributionProperties = contributionProperties;
    }


    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_contribution, parent, false);
        return new ContributionValueAdapter.ContributionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {

        String path = MediaHelpers.dataPath + contributionProperties.get(position).getSymbol();
        if (MediaHelpers.isRasterImageFileName(path)) {
            Glide.with(context)
                    .asBitmap()
                    .load(MediaHelpers.dataPath + contributionProperties.get(position).getSymbol())
                    .into(holder.contributionValueImage);
        } else if (MediaHelpers.isVectorImageFileName(path)) {
            Glide.with(context)
                    .asDrawable()
                    .load(MediaHelpers.svgToDrawable(path))
                    .into(holder.contributionValueImage);
        }

    }


    @Override
    public int getItemCount() {
        return contributionProperties.size();
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        ImageView contributionValueImage;

        ContributionViewHolder(View itemView) {
            super(itemView);
            contributionValueImage = itemView.findViewById(R.id.contrib_value_image);

        }
    }
}
