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

public class ContributionAudioAdapter extends RecyclerView.Adapter<ContributionAudioAdapter.ContributionViewHolder> {
    private Context context;
    private List<Document> audios;

    public ContributionAudioAdapter(Context context, List<Document> audios) {
        this.context = context;
        this.audios = audios;
    }


    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.audio_item, parent, false);
        return new ContributionAudioAdapter.ContributionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {
        Glide.with(context)
                .asDrawable()
                .load(R.drawable.audio)
                .into(holder.contributionAudioImage);
    }


    @Override
    public int getItemCount() {
        return audios.size();
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        ImageView contributionAudioImage;

        ContributionViewHolder(View itemView) {
            super(itemView);
            contributionAudioImage = itemView.findViewById(R.id.contrib_audio_image);

        }
    }
}
