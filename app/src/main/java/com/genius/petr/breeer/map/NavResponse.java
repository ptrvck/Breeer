package com.genius.petr.breeer.map;

import com.genius.petr.breeer.database.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class NavResponse {
    private Place place;
    private ArrayList<LatLng> navPoints;

    public NavResponse(Place place, ArrayList<LatLng>  navPoints) {
        this.place = place;
        this.navPoints = navPoints;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public ArrayList<LatLng>  getNavPoints() {
        return navPoints;
    }

    public void setNavPoints(ArrayList<LatLng>  navPoints) {
        this.navPoints = navPoints;
    }
}
