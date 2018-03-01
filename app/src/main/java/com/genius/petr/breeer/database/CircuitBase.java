package com.genius.petr.breeer.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * Created by Petr on 23. 2. 2018.
 */

@Entity(tableName = CircuitBase.TABLE_NAME)
public class CircuitBase {

    public static final String TABLE_NAME = "circuits";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_DESCRIPTION = "description";

    @PrimaryKey()
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_TYPE)
    public int type;

    @ColumnInfo(name = COLUMN_DESCRIPTION)
    public String description;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
