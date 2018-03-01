package com.genius.petr.breeer.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * Created by Petr on 24. 2. 2018.
 */

@Entity(tableName = CircuitStop.TABLE_NAME, foreignKeys = {
@ForeignKey(entity= CircuitBase.class, parentColumns = CircuitBase.COLUMN_ID, childColumns = CircuitStop.COLUMN_CIRCUIT_ID),
@ForeignKey(entity= Place.class, parentColumns = Place.COLUMN_ID, childColumns = CircuitStop.COLUMN_PLACE_ID)
})
public class CircuitStop {

    public static final String TABLE_NAME = "circuit_stops";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_CIRCUIT_ID = "circuit_id";

    public static final String COLUMN_PLACE_ID = "place_id";

    public static final String COLUMN_NUMBER = "number";

    @PrimaryKey()
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(index = true, name = COLUMN_CIRCUIT_ID)
    public long circuitId;

    @ColumnInfo(index = true, name = COLUMN_PLACE_ID)
    public long placeId;

    @ColumnInfo(name = COLUMN_NUMBER)
    public int number;

    public long getId() {
        return id;
    }

    public long getCircuitId() {
        return circuitId;
    }

    public long getPlaceId() {
        return placeId;
    }

    public int getNumber() {
        return number;
    }
}
