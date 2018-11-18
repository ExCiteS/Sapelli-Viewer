package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Category;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Field;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Geometry;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Logs;
import uk.ac.excites.ucl.sapelliviewer.datamodel.LookUpValue;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectInfo;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ProjectProperties;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

@Database(entities = {UserInfo.class, ProjectInfo.class, Category.class, Project.class, Field.class, LookUpValue.class, Contribution.class, Document.class, Geometry.class, ContributionProperty.class, ProjectProperties.class, Logs.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    private static String LOOKUP_INJECTION_CONTRIBUTIONPROPERTY =
            "CREATE TRIGGER trigger_lookup_injection_contributionproperty\n" +
                    "  AFTER\n" +
                    "  INSERT\n" +
                    "  ON ContributionProperty\n" +
                    "  WHEN ((SELECT fieldtype\n" +
                    "         FROM ContributionProperty\n" +
                    "           JOIN Field ON ContributionProperty.fieldId = Field.id\n" +
                    "         WHERE contribPropertyId = NEW.contribPropertyId) LIKE '%LookUpField%')\n" +
                    "BEGIN\n" +
                    "  UPDATE ContributionProperty\n" +
                    "  SET value = (SELECT name\n" +
                    "               FROM LookUpValue\n" +
                    "               WHERE id IN (\n" +
                    "                 SELECT value\n" +
                    "                 FROM ContributionProperty)),\n" +
                    "    symbol  = (SELECT symbol\n" +
                    "               FROM LookUpValue\n" +
                    "               WHERE id IN (\n" +
                    "                 SELECT value\n" +
                    "                 FROM ContributionProperty))\n" +
                    "  WHERE contribPropertyId = new.contribPropertyId;\n" +
                    "END;";
    private static String LOOKUP_INJECTION_CONTRIBUTION =
            "CREATE TRIGGER trigger_lookup_injection_contribution\n" +
                    "  AFTER\n" +
                    "  INSERT\n" +
                    "  ON Contribution\n" +
                    "  WHEN ((SELECT fieldtype\n" +
                    "         FROM Contribution\n" +
                    "           JOIN Field ON Contribution.fieldId = Field.id\n" +
                    "         WHERE Contribution.id = NEW.id) LIKE '%LookUpField%')\n" +
                    "BEGIN\n" +
                    "  UPDATE Contribution\n" +
                    "  SET value = (SELECT name\n" +
                    "               FROM LookUpValue\n" +
                    "               WHERE id IN (\n" +
                    "                 SELECT value\n" +
                    "                 FROM Contribution)),\n" +
                    "    symbol  = (SELECT symbol\n" +
                    "               FROM LookUpValue\n" +
                    "               WHERE id IN (\n" +
                    "                 SELECT value\n" +
                    "                 FROM Contribution))\n" +
                    "  WHERE Contribution.id = NEW.id;\n" +
                    "END;";

    public abstract UserDao userDao();

    public abstract ProjectInfoDao projectInfoDao();

    public abstract ContributionDao contributionDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database")
                            .addCallback(rdc)
                            .build();

//            LOOKUP_INJECTION_CONTRIBUTION = context.getResources().getString(R.string.sql_lookup_injection_contribution);
//            LOOKUP_INJECTION_CONTRIBUTIONPROPERTY = context.getResources().getString(R.string.sql_lookup_injection_contributionproperty);
        }
        return INSTANCE;
    }

    static RoomDatabase.Callback rdc = new RoomDatabase.Callback() {
        public void onCreate(SupportSQLiteDatabase db) {
            if (LOOKUP_INJECTION_CONTRIBUTION != null)
                db.execSQL(LOOKUP_INJECTION_CONTRIBUTION);
            if (LOOKUP_INJECTION_CONTRIBUTIONPROPERTY != null)
                db.execSQL(LOOKUP_INJECTION_CONTRIBUTIONPROPERTY);
        }
    };


    public static void destroyInstance() {
        INSTANCE = null;
    }
}
