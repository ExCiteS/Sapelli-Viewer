package uk.ac.excites.ucl.sapelliviewer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import uk.ac.excites.ucl.sapelliviewer.datamodel.UserInfo;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserInfo(UserInfo userInfo);

    @Update
    void updateUserInfo(UserInfo projectInfo);

    @Delete
    void deleteUserInfo(UserInfo projectInfo);

    @Query("SELECT * FROM UserInfo WHERE id = :id")
    List<UserInfo> getUserInfo(int id);

}