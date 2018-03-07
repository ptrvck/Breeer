package com.genius.petr.breeer.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Petr on 23. 2. 2018.
 */

@Dao
public interface CircuitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCircuitBase(CircuitBase circuitBase);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNode(CircuitNode circuitNode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStop(CircuitStop circuitStop);

    @Query("SELECT * FROM " + CircuitBase.TABLE_NAME)
    LiveData<List<Circuit>> selectAllCircuits();

    @Query("SELECT * FROM " + CircuitBase.TABLE_NAME)
    List<Circuit> selectAllSynchronous();

    //nejaky takovy select potrebuju, pokud chci mista a ne jen ID
    /*
    @Query("SELECT " +
            Place.TABLE_NAME + "." + Place.COLUMN_ID +
            Place.TABLE_NAME + "." + Place.COLUMN_ID +

            +
            " FROM " + Place.TABLE_NAME + " INNER JOIN " + CircuitStop.TABLE_NAME +
            " ON " + Place.COLUMN_ID + " = " + CircuitStop.COLUMN_PLACE_ID + " WHERE " +
            CircuitStop.COLUMN_CIRCUIT_ID + " = :id")
    List<Place> getStopsOfCircuit(long id);
*/

    // List all users by deviceId
    /*
    @Query("SELECT " +
            "P._id, P.name, P.category, P.lat, P.lng, P.description, " +
            "P.phone, P.web, P.address, P.opening_hours " +
            " FROM " + Place.TABLE_NAME +
            " P INNER JOIN ( SELECT " +
            CircuitStop.COLUMN_PLACE_ID + ", " +
            CircuitStop.COLUMN_NUMBER +
            " FROM " + CircuitStop.TABLE_NAME +
            " WHERE " + CircuitStop.COLUMN_CIRCUIT_ID + " = :id" +
            " ORDER BY " + CircuitStop.COLUMN_NUMBER +" ) LINK  ON " +
            "LINK.place_id = P._id")
    List<Place> getStopsOfCircuit(long id);
    */

    // List all users by deviceId
    @Query("SELECT " +
            CircuitStop.COLUMN_PLACE_ID +
            " FROM " + CircuitStop.TABLE_NAME +
            " WHERE " + CircuitStop.COLUMN_CIRCUIT_ID + " = :id" +
            " ORDER BY " + CircuitStop.COLUMN_NUMBER)
    List<Integer> getStopsIdsOfCircuit(long id);


    @Query("SELECT * FROM " + CircuitBase.TABLE_NAME + " WHERE " + CircuitBase.COLUMN_ID + " = :id")
    LiveData<CircuitBase> selectById(long id);

    @Query("DELETE FROM " + CircuitBase.TABLE_NAME + " WHERE " + CircuitBase.COLUMN_ID + " = :id")
    int deleteById(long id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCircuit(CircuitBase circuitBase);

    @Query("DELETE FROM " + CircuitBase.TABLE_NAME)
    void removeAllPlaces();

    @Query("SELECT COUNT(*) FROM " + CircuitBase.TABLE_NAME)
    int count();
}
