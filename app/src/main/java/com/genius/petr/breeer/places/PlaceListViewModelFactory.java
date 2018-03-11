package com.genius.petr.breeer.places;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/**
 * Created by Petr on 10. 3. 2018.
 */

public class PlaceListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application app;
    private long category;


    public PlaceListViewModelFactory(Application application, long category) {
        app = application;
        this.category = category;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new PlaceListViewModel(app, category);
    }
}
