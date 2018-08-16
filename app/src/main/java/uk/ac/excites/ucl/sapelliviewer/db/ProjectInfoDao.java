package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectProperties;

@Dao
public interface ProjectInfoDao {

    /* PROJECTS */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProjectInfo(List<ProjectInfo> projectInfos);

    @Update
    void updateProjectInfo(ProjectInfo projectInfo);

    @Delete
    void deleteProjectInfo(ProjectInfo projectInfo);

    @Query("SELECT * FROM ProjectInfo order by name")
    Single<List<ProjectInfo>> getProjectInfos();

    @Query("DELETE FROM ProjectInfo")
    public void clearProjectInfos();


    @Query("SELECT COUNT(*) FROM Contribution WHERE projectid = :projectId")
    Single<Integer> getContributionsCount(int projectId);

    @Query("SELECT count(*) FROM Document WHERE contribution_id IN (SELECT id FROM Contribution WHERE projectid = :projectId)")
    Single<Integer> getMediaCount(int projectId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProjectProperties(ProjectProperties projectProperties);

    @Query("SELECT mapPath FROM projectproperties WHERE id=:projectId")
    Maybe<String> getMapPath(int projectId);

    /* CATEGORIES */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProject(Project project);

    @Query("SELECT * FROM category WHERE projectid=:projectId")
    Single<List<Category>> getCategories(int projectId);


    /* FIELDS */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertField(Field field);

    @Query("SELECT * FROM Field")
    List<Field> getFields();

    @Query("SELECT * FROM Field WHERE id=:id")
    Field getFieldById(int id);

    @Query("SELECT * FROM Field WHERE `key`=:key")
    Single<Field> getFieldByKey(String key);

    @Query("SELECT * FROM Field WHERE category_id IN (SELECT Category.id FROM Category JOIN Project ON Project.id = Category.projectid WHERE Project.id = :projectid)")
    Single<List<Field>> getFieldsByProject(int projectid);

    /* LOOKUPVALUES */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLookupValue(LookUpValue lookUpValue);

    @Query("SELECT * FROM LookUpValue Where id=:id")
    Single<LookUpValue> getLookupValueById(String id);


    @Query("SELECT * FROM LookUpValue WHERE fieldid IN (SELECT field.id FROM Field JOIN Category ON Category.id = category_id JOIN Project ON Project.id = projectid WHERE project.id = :projectId);")
    Single<List<LookUpValue>> getLookupValueByProject(int projectId);

    @Query("SELECT * from LookUpValue where fieldId = :fieldId")
    Single<List<LookUpValue>> getLookupValueByField(int fieldId);

}
