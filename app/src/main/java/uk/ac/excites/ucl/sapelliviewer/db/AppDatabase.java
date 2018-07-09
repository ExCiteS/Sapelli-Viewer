package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

@Database(entities = {UserInfo.class, ProjectInfo.class, Category.class, Project.class, Field.class, LookUpValue.class, Contribution.class, Document.class, Geometry.class, ContributionProperty.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract ProjectInfoDao projectInfoDao();

    public abstract ContributionDao contributionDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
