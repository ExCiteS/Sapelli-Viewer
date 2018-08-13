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
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
import uk.ac.excites.ucl.sapelliviewer.utils.MediaHelpers;

public class ContributionPhotoAdapter extends RecyclerView.Adapter<ContributionPhotoAdapter.ContributionViewHolder> {
    private Context context;
    private List<Document> photos;
    private PhotoAdapterClickListener listener;


    public ContributionPhotoAdapter(Context context, List<Document> photos, PhotoAdapterClickListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;

    }


    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.photo_item, parent, false);
        return new ContributionPhotoAdapter.ContributionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {

        String path = MediaHelpers.dataPath + photos.get(position).getUrl();
        if (MediaHelpers.isRasterImageFileName(path)) {
            Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .thumbnail(0.1f)
                    .into(holder.contributionPhotoImage);
        } else if (MediaHelpers.isVectorImageFileName(path)) {
            Glide.with(context)
                    .asDrawable()
                    .load(MediaHelpers.svgToDrawable(path))
                    .into(holder.contributionPhotoImage);
        }

    }


    @Override
    public int getItemCount() {
        return photos.size();
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        ImageView contributionPhotoImage;

        ContributionViewHolder(View itemView) {
            super(itemView);
            contributionPhotoImage = itemView.findViewById(R.id.contrib_photo_image);
            contributionPhotoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(v, photos.get(getAdapterPosition()).getUrl());
                }
            });
        }
    }

    public interface PhotoAdapterClickListener {
        void onClick(View v, String photoUrl);
    }
}