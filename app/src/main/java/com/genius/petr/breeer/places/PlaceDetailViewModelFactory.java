package com.genius.petr.breeer.places;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/**
 * Created by Petr on 21. 2. 2018.
 */

public class PlaceDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application app;
    private long mParam;


    public PlaceDetailViewModelFactory(Application application, Long param) {
        app = application;
        mParam = param;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new PlaceDetailViewModel(app, mParam);
    }
}
