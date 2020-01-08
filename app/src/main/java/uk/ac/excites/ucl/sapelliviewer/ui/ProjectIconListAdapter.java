package uk.ac.excites.ucl.sapelliviewer.ui;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;

public class ProjectIconListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ProjectAdapterClickListener listener;
    private ArrayList<RecyclerViewItemData> recyclerViewItems = new ArrayList<>();

    public ProjectIconListAdapter(ProjectAdapterClickListener listener) {
        this.listener = listener;
    }

    private RecyclerViewItemData getItem(int position) {
        return recyclerViewItems.get(position);
    }

    public void setProjects(List<ProjectInfo> projects) {
        recyclerViewItems.clear();

        for (ProjectInfo p : projects) {
            recyclerViewItems.add(new ProjectData(p));
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recyclerViewItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (getItem(i) instanceof ProjectData)
            return new ProjectViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_project_grid, viewGroup, false));
        else
            throw new RuntimeException("Invalid data type!");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        RecyclerViewItemData item = getItem(i);
        if (item instanceof ProjectData)
            ((ProjectViewHolder) viewHolder).bindTo(item, listener);
    }

    public interface ProjectAdapterClickListener {
        void openMap(ProjectInfo projectInfo);
    }

    private class ProjectData implements RecyclerViewItemData {
        private final ProjectInfo project;

        ProjectData(ProjectInfo project) {
            this.project = project;
        }
    }

    private class ProjectViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgIcon;
        private ConstraintLayout clContainer;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imgIcon = itemView.findViewById(R.id.imgIcon);
            this.clContainer = itemView.findViewById(R.id.clContainer);
        }

        void bindTo(RecyclerViewItemData item, ProjectAdapterClickListener listener) {
            ProjectInfo info = ((ProjectData) item).project;

            if (info.isRemote()) {
                imgIcon.setColorFilter(ContextCompat.getColor(imgIcon.getContext(), R.color.green_remote), android.graphics.PorterDuff.Mode.SRC_IN);
                clContainer.setBackground(ContextCompat.getDrawable(clContainer.getContext(), R.drawable.bg_green_stroke));
            } else {
                imgIcon.setColorFilter(ContextCompat.getColor(imgIcon.getContext(), R.color.blue_remote), android.graphics.PorterDuff.Mode.SRC_IN);
                clContainer.setBackground(ContextCompat.getDrawable(clContainer.getContext(), R.drawable.bg_blue_stroke));
            }

            imgIcon.setOnLongClickListener(v -> {
                Toast.makeText(imgIcon.getContext(), info.getName(), Toast.LENGTH_SHORT).show();
                return false;
            });

            imgIcon.setOnClickListener(v -> {
                if (listener != null) listener.openMap(info);
            });
        }
    }
}
