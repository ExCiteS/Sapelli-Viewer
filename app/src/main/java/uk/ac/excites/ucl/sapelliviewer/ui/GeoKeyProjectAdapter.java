package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;


/**
 * Thanks to JoCodes: https://www.codeproject.com/Tips/1229751/Handle-Click-Events-of-Multiple-Buttons-Inside-a
 */

public class GeoKeyProjectAdapter extends RecyclerView.Adapter<GeoKeyProjectAdapter.ProjectViewHolder> {

    private final CompositeDisposable compositeDisposable;
    private Context ctx;
    private List<ProjectInfo> projects;
    public DetailsAdapterListener onClickListener;

    public GeoKeyProjectAdapter(Context ctx, CompositeDisposable compositeDisposable, DetailsAdapterListener listener) {
        this.ctx = ctx;
        this.projects = new ArrayList<ProjectInfo>();
        this.onClickListener = listener;
        this.compositeDisposable = compositeDisposable;
    }

    public void setProjects(List<ProjectInfo> projectInfos) {
        this.projects = projectInfos;
        notifyDataSetChanged();
        for (ProjectInfo project : projectInfos) {
            getContributionCount(project);
        }

    }


    public void getContributionCount(ProjectInfo project) {
        Log.d("getContributionCount", " reached");
        AppDatabase.getAppDatabase(ctx).projectInfoDao().getContributionsCount(project.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(Integer number) {
                        project.setContributionCount(number);
                        notifyItemChanged(projects.indexOf(project));


                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("setProjects", e.getMessage());
                    }
                });
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
        holder.contributionsTxt.setText(ctx.getResources().getString(R.string.contributions) + project.getContributionCount());
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public ProjectInfo getProject(int position) {
        return projects.get(position);
    }


    class ProjectViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout cardLayout;
        TextView projectName;
        ImageButton openMapButton;
        ImageButton syncProjectButton;
        TextView contributionsTxt;

        ProjectViewHolder(View itemView) {
            super(itemView);
            projectName = (TextView) itemView.findViewById(R.id.list_item_text);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.card);
            openMapButton = (ImageButton) itemView.findViewById(R.id.open_map);
            syncProjectButton = (ImageButton) itemView.findViewById(R.id.sync_project);
            contributionsTxt = (TextView) itemView.findViewById(R.id.txt_contributions);

            openMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.openMap(view, getAdapterPosition());
                }
            });

            syncProjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.syncProjectOnClick(view, getAdapterPosition());
                }
            });
        }
    }

    public interface DetailsAdapterListener {
        void openMap(View v, int position);

        void syncProjectOnClick(View v, int position);
    }

}
