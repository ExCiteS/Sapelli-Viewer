package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Single;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;

@Dao
public interface ContributionDao {

    /* CONTRIBUTION */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContribution(Contribution contribution);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContributions(List<Contribution> contributions);

    @Query("SELECT COUNT(*) FROM contribution where projectId = :projectId")
    Single<Integer> getNumberContributions(int projectId);

    @Query("SELECT * FROM contribution where projectId = :projectId")
    Single<List<Contribution>> getContributions(int projectId);

    /* CONTRIBUTION PROPERTIES*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertContributionProperties(List<ContributionProperty> contributionProperty);
    void insertContributionProperties(ContributionProperty contributionProperty);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContributionProperties(List<ContributionProperty> contributionProperties);



    @Query("SELECT * FROM contributionProperty where contributionId = :contributionId")
    Single<List<ContributionProperty>> getContributionsProperties(int contributionId);

    /* MEDIA FILES */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMediaFile(Document mediaFile);
}
