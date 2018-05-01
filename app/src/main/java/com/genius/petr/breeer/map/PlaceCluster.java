package com.genius.petr.breeer.map;

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

    public PlaceCluster(LatLng position, long id, int category) {
        this.position = position;
        this.id = id;
        this.category = category;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return Long.toString(id);
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public int getCategory() {
        return category;
    }
}
