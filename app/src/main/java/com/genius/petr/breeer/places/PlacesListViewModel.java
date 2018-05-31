package com.genius.petr.breeer.places;

import android.location.Location;
import android.location.LocationManager;
import android.util.SparseArray;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class PlacesListViewModel {
    SparseArray<List<PlaceViewModel>> categories = new SparseArray<>();
    private Location location;

    public int getActivePostion() {
        return activePostion;
    }

    public void setActivePostion(int activePostion) {
        this.activePostion = activePostion;
    }

    int activePostion = 0;


    public PlacesListViewModel(AppDatabase appDatabase, Location location) {

        this.location = location;

        for (int category : PlaceConstants.CATEGORIES) {
            List<Place> placesOfCategory = appDatabase.place().selectByCategory(category);
            List<PlaceViewModel> placeViewModels = new ArrayList<>();

            for (Place p : placesOfCategory) {
                PlaceViewModel placeViewModel = new PlaceViewModel();
                placeViewModel.setPlace(p);
                if (location != null) {
                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                    temp.setLatitude(p.getLat());
                    temp.setLongitude(p.getLng());
                    placeViewModel.setDistance(location.distanceTo(temp));
                }
                placeViewModels.add(placeViewModel);
            }

            Collections.sort(placeViewModels);
            categories.append(category, placeViewModels);
        }
    }

    public List<PlaceViewModel> getPlacesOfCategory(int category) {
        return categories.get(category);
    }

    public int size() {
        return categories.size();
    }

    public Location getLocation() {
        return location;
    }
}
