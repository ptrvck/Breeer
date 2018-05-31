package com.genius.petr.breeer.places;

import android.support.annotation.NonNull;

import com.genius.petr.breeer.database.Place;

public class PlaceViewModel implements Comparable<PlaceViewModel>{
    private Place place;

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    private double distance = -1;


    @Override
    public int compareTo(@NonNull PlaceViewModel o) {
        if (distance < 0 || o.distance < 0) {
            return place.getName().compareTo(o.getPlace().getName());
        }

        if (distance < o.getDistance()) return -1;
        if (distance > o.getDistance()) return 1;

        return 0;
    }
}
