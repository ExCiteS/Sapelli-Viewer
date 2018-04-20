package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

/**
 * Created by Julia on 13/02/2018.
 */

public class GeoKeyProjectAdapter extends RecyclerView.Adapter<GeoKeyProjectAdapter.ProjectViewHolder> {

    private Context ctx;
    private List<ProjectInfo> projects;

    public GeoKeyProjectAdapter(Context ctx, List<ProjectInfo> projects) {
        this.ctx = ctx;
        this.projects = projects;
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

        holder.cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "You clicked " + project.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout cardLayout;
        public TextView projectName;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            projectName = (TextView) itemView.findViewById(R.id.list_item_text);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.card);
        }
    }

}
