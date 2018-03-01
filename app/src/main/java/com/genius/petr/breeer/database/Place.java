package com.genius.petr.breeer.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.provider.BaseColumns;

/**
 * Created by Petr on 10. 2. 2018.
 */

@Entity(tableName = Place.TABLE_NAME)
public class Place {

    public static final String TABLE_NAME = "places";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_LATITUDE = "lat";

    public static final String COLUMN_LONGITUDE = "lng";

    public static final String COLUMN_DESCRIPTION = "description";

    public static final String COLUMN_PHONE = "phone";

    public static final String COLUMN_WEB = "web";

    public static final String COLUMN_ADDRESS = "address";

    public static final String COLUMN_OPENING_HOURS = "opening_hours";



    @PrimaryKey()
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_TYPE)
    public int type;

    @ColumnInfo(name = COLUMN_LATITUDE)
    public double lat;

    @ColumnInfo(name = COLUMN_LONGITUDE)
    public double lng;

    @ColumnInfo(name = COLUMN_DESCRIPTION)
    public String description;

    @ColumnInfo(name = COLUMN_PHONE)
    public String phone;

    @ColumnInfo(name = COLUMN_WEB)
    public String web;

    @ColumnInfo(name = COLUMN_ADDRESS)
    public String address;

    @ColumnInfo(name = COLUMN_OPENING_HOURS)
    public String openingHours;


    public static Place fromContentValues(ContentValues values) {
        final Place place = new Place();
        if (values.containsKey(COLUMN_ID)) {
            place.id = values.getAsLong(COLUMN_ID);
        }
        if (values.containsKey(COLUMN_NAME)) {
            place.name = values.getAsString(COLUMN_NAME);
        }

        if (values.containsKey(COLUMN_TYPE)) {
            place.type = values.getAsInteger(COLUMN_TYPE);
        }
        return place;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getWeb() {
        return web;
    }

    public String getAddress() {
        return address;
    }

    public String getOpeningHours() {
        return openingHours;
    }

}
