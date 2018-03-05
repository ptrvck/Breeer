package com.genius.petr.breeer.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Petr on 4. 3. 2018.
 */

public class PlaceCluster implements ClusterItem {

    private long id;
    private LatLng position;

    public PlaceCluster(LatLng position, long id) {
        this.position = position;
        this.id = id;
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
}
