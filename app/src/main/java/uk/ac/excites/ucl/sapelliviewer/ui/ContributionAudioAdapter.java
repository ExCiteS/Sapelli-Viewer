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

public class ContributionAudioAdapter extends RecyclerView.Adapter<ContributionAudioAdapter.ContributionViewHolder> {
    private Context context;
    private List<Document> audios;
    private AudioAdapterClickListener listener;

    public ContributionAudioAdapter(Context context, List<Document> audios, AudioAdapterClickListener listener) {
        this.context = context;
        this.audios = audios;
        this.listener = listener;

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

        if (audios.get(position).isActive())
            holder.contributionAudioFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null));
        else
            holder.contributionAudioFrame.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.background_dark, null));
    }


    @Override
    public int getItemCount() {
        return audios.size();
    }

    public Document getAudioByid(int audioId) {
        for (Document audio : audios) {
            if (audio.getId() == audioId)
                return audio;
        }
        return null;
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        ImageView contributionAudioImage;
        View contributionAudioFrame;

        ContributionViewHolder(View itemView) {
            super(itemView);

            contributionAudioFrame = itemView.findViewById(R.id.audio_item_frame);
            contributionAudioImage = itemView.findViewById(R.id.contrib_audio_image);
            contributionAudioImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(audios.get(getAdapterPosition()));
                }
            });

        }
    }


    public interface AudioAdapterClickListener {
        void onClick(Document audio);
    }
}
