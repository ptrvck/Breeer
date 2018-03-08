package com.genius.petr.breeer.places;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Place;

/**
 * Created by ludek on 08.03.18.
 */

public class PlaceEssentialsViewModel extends AndroidViewModel {

    private final LiveData<Place> place;

    private AppDatabase appDatabase;

    public PlaceEssentialsViewModel(Application application, long id) {
        super(application);
        appDatabase = AppDatabase.getDatabase(this.getApplication());
        place = appDatabase.place().selectById(id);
    }

    public LiveData<Place> getPlace() {
        return place;
    }
}
