package com.genius.petr.breeer.circuits;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.Place;

import java.util.List;

/**
 * Created by Petr on 11. 3. 2018.
 */

public class CircuitMapViewModel {

    private String TAG = "PlaceDetail";
    private final List<Place> circuitStops;

    private AppDatabase appDatabase;
    public CircuitMapViewModel(@NonNull Application application, long id) {
        appDatabase = AppDatabase.getDatabase(application);
        circuitStops = appDatabase.circuit().getStopsOfCircuit(id);
    }

    public CircuitMapViewModel(List<Place> stops) {
        circuitStops = stops;
    }

    public List<Place> getStops() {
        return circuitStops;
    }
}
