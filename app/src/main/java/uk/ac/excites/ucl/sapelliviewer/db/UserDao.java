package uk.ac.excites.ucl.sapelliviewer.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.Single;
import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserInfo(UserInfo userInfo);

    @Update
    void updateUserInfo(UserInfo projectInfo);

    @Delete
    void deleteUserInfo(UserInfo projectInfo);

    @Query("SELECT * FROM UserInfo")
    Single<UserInfo> getUserInfo();

    @Query("DELETE FROM UserInfo")
    public void clearPrevUser();

}