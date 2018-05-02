package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;

@Dao
public interface ProjectInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProjectInfo(List<ProjectInfo> projectInfos);

    @Update
    void updateProjectInfo(ProjectInfo projectInfo);

    @Delete
    void deleteProjectInfo(ProjectInfo projectInfo);

    @Query("SELECT * FROM ProjectInfo WHERE id = :id")
    List<ProjectInfo> getProjectInfo(int id);

    @Query("SELECT * FROM ProjectInfo")
    Single<List<ProjectInfo>> getProjectInfos();

    @Query("DELETE FROM ProjectInfo")
    public void clearProjectInfos();
}
