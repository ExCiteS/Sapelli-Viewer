package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;


/**
 * Thanks to JoCodes: https://www.codeproject.com/Tips/1229751/Handle-Click-Events-of-Multiple-Buttons-Inside-a
 */

public class GeoKeyProjectAdapter extends RecyclerView.Adapter<GeoKeyProjectAdapter.ProjectViewHolder> {

    private Context ctx;
    private List<ProjectInfo> projects;
    public DetailsAdapterListener onClickListener;

    public GeoKeyProjectAdapter(Context ctx, DetailsAdapterListener listener) {
        this.ctx = ctx;
        this.projects = new ArrayList<ProjectInfo>();
        this.onClickListener = listener;
    }

    public void setProjects(List<ProjectInfo> projectInfos) {
        this.projects = projectInfos;
        notifyDataSetChanged();
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        ProjectInfo project = projects.get(position);
        holder.projectName.setText(project.getName());
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public int getProjectId(int position) {
        return projects.get(position).getId();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout cardLayout;
        public TextView projectName;
        public ImageButton syncProjectButton;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            projectName = (TextView) itemView.findViewById(R.id.list_item_text);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.card);
            syncProjectButton = (ImageButton) itemView.findViewById(R.id.sync_project);

            syncProjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.syncOnClick(view, getAdapterPosition());
                }
            });
        }
    }

    public interface DetailsAdapterListener {
        void syncOnClick(View v, int position);

    }

}
