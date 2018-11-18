package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Contribution;
import uk.ac.excites.ucl.sapelliviewer.datamodel.ContributionProperty;
import uk.ac.excites.ucl.sapelliviewer.datamodel.Document;

@Dao
public interface ContributionDao {

    /* CONTRIBUTION */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContribution(Contribution contribution);

    @Update
    void updateContribution(Contribution contribution);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContributions(List<Contribution> contributions);

    @Query("SELECT COUNT(*) FROM contribution where projectId = :projectId")
    Single<Integer> getNumberContributions(int projectId);

    @Query("SELECT * FROM contribution where projectId = :projectId")
    Single<List<Contribution>> getContributions(int projectId);

    @Query("SELECT DISTINCT Contribution.* FROM LookUpValue JOIN ContributionProperty ON LookUpValue.name = ContributionProperty.value AND LookUpValue.symbol = ContributionProperty.symbol JOIN Contribution ON ContributionProperty.contributionId = Contribution.id WHERE LookUpValue.id = :valueID;")
    Single<List<Contribution>> getContributionsByValue(int valueID);

    @Query("SELECT DISTINCT Contribution.* FROM LookUpValue JOIN ContributionProperty ON LookUpValue.name = ContributionProperty.value AND LookUpValue.symbol = ContributionProperty.symbol JOIN Contribution ON ContributionProperty.contributionId = Contribution.id WHERE LookUpValue.id IN (:valueIDs);")
    Single<List<Contribution>> getContributionsByValues(List<Integer> valueIDs);

    @Query("Select distinct fieldId from Contribution where projectId=:projectId")
    Maybe<Integer> getDisplayField(int projectId);

    /* CONTRIBUTION PROPERTIES*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertContributionProperties(List<ContributionProperty> contributionProperty);
    void insertContributionProperties(ContributionProperty contributionProperty);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContributionProperties(List<ContributionProperty> contributionProperties);

    @Query("SELECT * FROM contributionProperty where contributionId = :contributionId")
    Single<List<ContributionProperty>> getContributionsProperties(int contributionId);

    @Query("SELECT ContributionProperty.* from Contribution join ContributionProperty on contribution.id = contributionproperty.contributionId WHERE  ContributionProperty.symbol is not null and Contribution.id = :contributionId")
    Single<List<ContributionProperty>> getPropertiesByContribution(int contributionId);

    @Query("SELECT ContributionProperty.* from Contribution join ContributionProperty on contribution.id = contributionproperty.contributionId WHERE  ContributionProperty.[key] = 'StartTime' and Contribution.id = :contributionId")
    Maybe<ContributionProperty> getDateByContribution(int contributionId);

    /* MEDIA FILES */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMediaFile(Document mediaFile);

    @Query("Select * from Document where id = :id")
    Maybe<Document> getDocumentById(int id);

    @Query("Select * from Document where file_type='ImageFile' and contribution_id = :contributionId")
    Single<List<Document>> getPhotosByContribution(int contributionId);

    @Query("Select * from Document where file_type='AudioFile' and contribution_id = :contributionId")
    Single<List<Document>> getAudiosByContribution(int contributionId);


}
