package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;

@Dao
public interface ProjectInfoDao {

    @Insert
    void insertProjectInfo(ProjectInfo projectInfo);

    @Update
    void updateProjectInfo(ProjectInfo projectInfo);

    @Delete
    void deleteProjectInfo(ProjectInfo projectInfo);

    @Query("SELECT * FROM ProjectInfo WHERE id = :id")
    List<ProjectInfo> getProjectInfo(int id);

    @Query("SELECT * FROM ProjectInfo")
    ProjectInfo getProjectInfos();
}
