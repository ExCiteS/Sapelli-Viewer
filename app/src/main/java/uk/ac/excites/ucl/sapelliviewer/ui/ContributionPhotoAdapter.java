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

        if (photos.get(position).isActive())
            holder.contributionPhotoFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
        else
            holder.contributionPhotoFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.background_dark, null));

    }


    @Override
    public int getItemCount() {
        return photos == null ? 0 : photos.size();
    }

    public Document getPhotoByid(int photoId) {
        for (Document photo : photos) {
            if (photo.getId() == photoId)
                return photo;
        }
        return null;
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        View contributionPhotoFrame;
        ImageView contributionPhotoImage;

        ContributionViewHolder(View itemView) {
            super(itemView);
            contributionPhotoFrame = itemView.findViewById(R.id.photo_item_frame);
            contributionPhotoImage = itemView.findViewById(R.id.contrib_photo_image);
            contributionPhotoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(photos.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface PhotoAdapterClickListener {
        void onClick(Document photo);
    }
}
