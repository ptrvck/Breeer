package com.genius.petr.breeer.places;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/**
 * Created by ludek on 08.03.18.
 */

public class PlaceEssentialsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application app;
    private long mParam;


    public PlaceEssentialsViewModelFactory(Application application, Long param) {
        app = application;
        mParam = param;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new PlaceEssentialsViewModel(app, mParam);
    }
}
