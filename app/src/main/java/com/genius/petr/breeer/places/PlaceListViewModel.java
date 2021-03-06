package com.genius.petr.breeer.places;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Place;

import java.util.List;

/**
 * Created by Petr on 20. 2. 2018.
 */

public class PlaceListViewModel extends AndroidViewModel {

    private final List<Place> placeList;

    private AppDatabase appDatabase;

    public PlaceListViewModel(Application application, long category) {
        super(application);

        appDatabase = AppDatabase.getDatabase(this.getApplication());

        placeList = appDatabase.place().selectByCategory(category);
    }

    public List<Place> getPlaceList() {
        return placeList;
    }

}
