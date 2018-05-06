package com.genius.petr.breeer.map;

import android.util.Log;

import com.genius.petr.breeer.database.PlaceConstants;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Petr on 4. 3. 2018.
 */

public class PlaceCluster implements ClusterItem {

    private long id;
    private int category;
    private LatLng position;
    private boolean active;

    public PlaceCluster(LatLng position, long id, int category) {
        this.position = position;
        this.id = id;
        this.category = category;
        this.active = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive(){
        return this.active;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        Log.i("magic", "magic happens");
        return Long.toString(id);
    }

    @Override
    public String getSnippet() {
        return Integer.toString(category);
    }

    public int getCategory() {
        return category;
    }
}
