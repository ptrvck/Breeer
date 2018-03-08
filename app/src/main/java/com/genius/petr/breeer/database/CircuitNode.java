package com.genius.petr.breeer.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Petr on 23. 2. 2018.
 */

@Entity(tableName = CircuitNode.TABLE_NAME, foreignKeys =
@ForeignKey(entity= CircuitBase.class, parentColumns = CircuitBase.COLUMN_ID, childColumns = CircuitNode.COLUMN_CIRCUIT_ID))
public class CircuitNode {

    public static final String TABLE_NAME = "circuit_nodes";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_CIRCUIT_ID = "circuit_id";

    public static final String COLUMN_LATITUDE = "lat";

    public static final String COLUMN_LONGITUDE = "lng";

    public static final String COLUMN_NUMBER = "number";

    @PrimaryKey()
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(index = true, name = COLUMN_CIRCUIT_ID)
    public long circuitId;

    @ColumnInfo(name = COLUMN_LATITUDE)
    public double lat;

    @ColumnInfo(name = COLUMN_LONGITUDE)
    public double lng;

    @ColumnInfo(name = COLUMN_NUMBER)
    public int number;

    public long getId() {
        return id;
    }

    public long getCircuitId() {
        return circuitId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getNumber() {
        return number;
    }

    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }
}
