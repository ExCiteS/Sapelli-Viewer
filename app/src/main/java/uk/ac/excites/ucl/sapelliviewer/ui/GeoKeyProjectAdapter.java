package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.MainThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;
import uk.ac.excites.ucl.sapelliviewer.utils.TokenManager;

import static uk.ac.excites.ucl.sapelliviewer.R.drawable.sync;


/**
 * Thanks to JoCodes: https://www.codeproject.com/Tips/1229751/Handle-Click-Events-of-Multiple-Buttons-Inside-a
 */

public class GeoKeyProjectAdapter extends RecyclerView.Adapter<GeoKeyProjectAdapter.ProjectViewHolder> {

    private final CompositeDisposable compositeDisposable;
    private Context ctx;
    private List<ProjectInfo> projects;
    private DetailsAdapterListener onClickListener;

    public GeoKeyProjectAdapter(Context ctx, CompositeDisposable compositeDisposable, DetailsAdapterListener listener) {
        this.ctx = ctx;
        this.projects = new ArrayList<ProjectInfo>();
        this.onClickListener = listener;
        this.compositeDisposable = compositeDisposable;
    }

    public void setProjects(List<ProjectInfo> projectInfos) {
        this.projects = projectInfos;
        toggleProject(TokenManager.getInstance().getActiveProject());
        notifyDataSetChanged();
        for (ProjectInfo project : projectInfos) {
            getCounts(project);
        }

    }

    public void getCounts(ProjectInfo project) {
        Single<Integer> contributionSingle = AppDatabase.getAppDatabase(ctx).projectInfoDao().getContributionsCount(project.getId())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(compositeDisposable::add)
                .doOnSuccess(project::setContributionCount);
        Single<Integer> mediaSingle = AppDatabase.getAppDatabase(ctx).projectInfoDao().getMediaCount(project.getId())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(compositeDisposable::add)
                .doOnSuccess(project::setMediaCount);
        Single.concat(contributionSingle, mediaSingle)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> notifyItemChanged(projects.indexOf(project)))
                .doOnError(e -> Log.e("Update Count", e.getMessage()))
                .subscribe();
    }

    public void getMapPath(ProjectInfo project, TextView view) {
        compositeDisposable.add(
                AppDatabase.getAppDatabase(ctx).projectInfoDao().getMapPath(project.getId()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<String>() {
                            @Override
                            public void onSuccess(String path) {
                                view.setText(path);
                                notifyItemChanged(getPosition(project));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("getMapPath", e.getMessage());
                            }
                        }));
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
        holder.mediaTxt.setText(ctx.getResources().getString(R.string.media) + project.getMediaCount());
        getMapPath(getProject(position), holder.mapPathTxt);
        if (project.isActive()) {
            holder.cardLayout.setBackgroundColor(Color.parseColor("#42a2ce"));
            holder.activeTxt.setText(R.string.active);
        } else {
            holder.cardLayout.setBackgroundColor(0);
            holder.activeTxt.setText("");
        }
        holder.syncProjectButton.setBackgroundResource(sync);

    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public ProjectInfo getProject(int position) {
        return projects.get(position);
    }

    public int getPosition(ProjectInfo project) {
        for (int i = 0; i < projects.size(); i++) {
            if (project.getId() == projects.get(i).getId())
                return i;
        }
        return -1;
    }

    public void toggleProject(int clickedProjectId) {
        for (ProjectInfo project : projects) {
            if (project.getId() == clickedProjectId) {
                if (project.isActive()) {
                    project.setActive(false);
                    TokenManager.getInstance().deleteActiveProject();
                } else {
                    project.setActive(true);
                    TokenManager.getInstance().saveActiveProject(clickedProjectId);
                }
            } else
                project.setActive(false);
            notifyItemChanged(getPosition(project));
        }
    }


    class ProjectViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout cardLayout;
        TextView projectName;
        ImageButton openMapButton;
        ImageButton syncProjectButton;
        TextView contributionsTxt;
        TextView mediaTxt;
        TextView activeTxt;
        TextView mapPathTxt;
        Button btnMapPath;

        ProjectViewHolder(View itemView) {
            super(itemView);
            projectName = (TextView) itemView.findViewById(R.id.list_item_text);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.card);
            openMapButton = (ImageButton) itemView.findViewById(R.id.open_map);
            syncProjectButton = (ImageButton) itemView.findViewById(R.id.sync_project);
            contributionsTxt = (TextView) itemView.findViewById(R.id.txt_contributions);
            btnMapPath = (Button) itemView.findViewById(R.id.btn_map_path);
            mapPathTxt = (TextView) itemView.findViewById(R.id.txt_map_path);
            mediaTxt = (TextView) itemView.findViewById(R.id.txt_media);
            activeTxt = (TextView) itemView.findViewById(R.id.active_txt);


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

            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.activateMapOnClick(view, getAdapterPosition());
                }
            });

            btnMapPath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.setMapPath(view, getAdapterPosition());
                }
            });
        }
    }

    public interface DetailsAdapterListener {
        void openMap(View v, int position);

        void syncProjectOnClick(View v, int position);

        void activateMapOnClick(View v, int position);

        void setMapPath(View v, int position);
    }

}
