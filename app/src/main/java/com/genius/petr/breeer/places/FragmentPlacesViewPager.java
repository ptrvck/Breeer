package com.genius.petr.breeer.places;

import android.os.AsyncTask;
import android.os.Bundle;
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
        View view = inflater.inflate(R.layout.fragment_circuits, container, false);

        Bundle args = getArguments();
        if(args != null) {
            position = args.getInt(ARGUMENT_CATEGORY);
        }

        viewPager = view.findViewById(R.id.viewpager_circuits);

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
        PlacesListPagerAdapter adapter = new PlacesListPagerAdapter(getContext(), viewModel, getPlaceClickListener()){
            @Override public void callback(Circuit circuit){
                MainActivity activity = (MainActivity)getActivity();
                activity.showCircuitOnMap(circuit.getId());
                Log.i("callbackTest", "from fragment");
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new PlacesListPagerTransformer());
        //todo: place position in view model?
        viewPager.setCurrentItem(position);
        viewPager.setVisibility(View.VISIBLE);
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
