package com.genius.petr.breeer.places;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.Place;

import java.lang.ref.WeakReference;

import static com.genius.petr.breeer.places.FragmentPlaceCategories.ARGUMENT_CATEGORY;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class FragmentPlacesViewPager extends Fragment{

    private ViewPager viewPager;
    private int position = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places_viewpager, container, false);

        Bundle args = getArguments();
        if(args != null) {
            position = args.getInt(ARGUMENT_CATEGORY);
        }

        viewPager = view.findViewById(R.id.viewpager_places);

        AppDatabase db = AppDatabase.getDatabase(getActivity().getApplication());
        ShowPlacesAsyncTask task = new ShowPlacesAsyncTask(this, db);
        task.execute();

        return view;
    }

    private View.OnClickListener getPlaceClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Place place = (Place) view.getTag();

                MainActivity activity = (MainActivity)FragmentPlacesViewPager.this.getActivity();
                activity.showPlaceDetail(place.getId());
            }
        };

        return listener;
    }

    private void showViewPager(PlacesListViewModel viewModel) {
        PlacesListPagerAdapter adapter = new PlacesListPagerAdapter(getContext(), viewModel, getPlaceClickListener());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new PlacesListPagerTransformer());
        //todo: place position in view model?
        viewPager.setCurrentItem(position);
        viewPager.setVisibility(View.VISIBLE);

        TabLayout tabLayout = getView().findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private static class ShowPlacesAsyncTask extends AsyncTask<Void, Void, PlacesListViewModel> {

        private WeakReference<FragmentPlacesViewPager> fragment;
        private final AppDatabase mDb;

        public ShowPlacesAsyncTask(FragmentPlacesViewPager fragment, AppDatabase db) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
        }

        @Override
        protected PlacesListViewModel doInBackground(final Void... params) {
            PlacesListViewModel viewModel = new PlacesListViewModel(mDb);
            return viewModel;
        }

        @Override
        protected void onPostExecute(PlacesListViewModel viewModel) {
            final FragmentPlacesViewPager fragment = this.fragment.get();
            if (fragment != null) {
                fragment.showViewPager(viewModel);
            }
        }
    }
}
