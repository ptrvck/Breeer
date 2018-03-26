package com.genius.petr.breeer.map;

import com.genius.petr.breeer.database.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by ludek on 08.03.18.
 */

class CircuitMapWrapper {
    List<Place> stops;
    List<LatLng> path;
    String name;

    public CircuitMapWrapper(String name, List<Place> stops, List<LatLng> path) {
        this.name = name;
        this.stops = stops;
        this.path = path;
    }

    public List<Place> getStops() {
        return stops;
    }

    public List<LatLng> getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
