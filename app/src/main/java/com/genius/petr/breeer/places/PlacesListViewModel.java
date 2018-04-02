package com.genius.petr.breeer.places;

import android.util.SparseArray;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;

import java.util.List;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class PlacesListViewModel {
    SparseArray<List<Place>> categories = new SparseArray<>();

    public PlacesListViewModel(AppDatabase appDatabase) {

        for (int category : PlaceConstants.CATEGORIES) {
            categories.append(category, appDatabase.place().selectByCategory(category));
        }
    }

    public List<Place> getPlacesOfCategory(int category) {
        return categories.get(category);
    }

    public int size() {
        return categories.size();
    }
}
