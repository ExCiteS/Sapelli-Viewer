package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ContributionValueAdapter extends RecyclerView.Adapter<ContributionValueAdapter.ContributionViewHolder> {
    public ArrayList<Integer> remove = new ArrayList<Integer>();
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
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            holder.itemView.post(() -> {
                                holder.itemView.getLayoutParams().height = 0;
                                holder.itemView.requestLayout();
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.contributionValueImage);
        } else if (MediaHelpers.isVectorImageFileName(path)) {
            Glide.with(context)
                    .asDrawable()
                    .load(MediaHelpers.svgToDrawable(path))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.itemView.post(() -> {
                                holder.itemView.getLayoutParams().height = 0;
                                holder.itemView.requestLayout();
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
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
