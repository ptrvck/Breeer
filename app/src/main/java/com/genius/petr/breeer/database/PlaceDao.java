package com.genius.petr.breeer.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import java.util.List;

/**
 * Created by Petr on 10. 2. 2018.
 */

@Dao
public interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Place place);

    @Insert
    long[] insertAll(Place[] places);

    @Query("SELECT * FROM " + Place.TABLE_NAME)
    LiveData<List<Place>> selectAll();

    @Query("SELECT * FROM " + Place.TABLE_NAME)
    List<Place> selectAllSynchronous();

    @Query("SELECT * FROM " + Place.TABLE_NAME + " WHERE " + Place.COLUMN_ID + " = :id")
    LiveData<Place> selectById(long id);


    @Query("SELECT * FROM " + Place.TABLE_NAME + " WHERE " + Place.COLUMN_CATEGORY + " = :category")
    LiveData<List<Place>> selectByCategory(long category);

    @Query("SELECT * FROM " + Place.TABLE_NAME + " WHERE " + Place.COLUMN_CATEGORY + " = :category")
    List<Place> getListByCategory(long category);

    @Query("DELETE FROM " + Place.TABLE_NAME + " WHERE " + Place.COLUMN_ID + " = :id")
    int deleteById(long id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePlace(Place place);

    @Query("DELETE FROM " + Place.TABLE_NAME)
    void removeAllPlaces();

    @Query("SELECT COUNT(*) FROM " + Place.TABLE_NAME)
    int count();
}
