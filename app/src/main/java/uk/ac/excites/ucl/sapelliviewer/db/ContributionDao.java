package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Project;

@Dao
public interface ContributionDao {

    /* CONTRIBUTION */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContribution(Contribution contribution);

    @Query("SELECT COUNT(*) FROM contribution where projectId = :projectId")
    Single<Integer> getNumberContributions(int projectId);

    /* CONTRIBUTION PROPERTIES*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContributionProperty(ContributionProperty contributionProperty);


}
