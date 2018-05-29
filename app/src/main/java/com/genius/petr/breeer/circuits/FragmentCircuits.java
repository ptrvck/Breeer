package com.genius.petr.breeer.circuits;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.PlaceConstants;
import com.genius.petr.breeer.places.PlacesListPagerAdapter;
import com.genius.petr.breeer.places.PlacesListPagerTransformer;
import com.genius.petr.breeer.places.PlacesListViewModel;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Petr on 24. 3. 2018.
 */

public class FragmentCircuits extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CircuitListViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circuits, container, false);

        viewPager = view.findViewById(R.id.viewpager_circuits);

        AppDatabase db = AppDatabase.getDatabase(getActivity().getApplication());
        ShowCircuitsAsyncTask task = new ShowCircuitsAsyncTask(this, db);
        task.execute();

        return view;
    }

    private void showViewPager(final CircuitListViewModel viewModel) {
        this.viewModel = viewModel;
        CircuitListPagerAdapter adapter = new CircuitListPagerAdapter(getContext(), viewModel){
            @Override public void callback(Circuit circuit){
                MainActivity activity = (MainActivity)getActivity();
                activity.showCircuitOnMap(circuit.getId());
                Log.i("callbackTest", "from fragment");
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new CircuitsPagerTransformer());

        tabLayout = getView().findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager, false);

        int pos = viewPager.getCurrentItem();
        Circuit circuit = viewModel.getCircuits().get(pos);
        int color = ContextCompat.getColor(getContext(), PlaceConstants.CATEGORY_COLORS.get(circuit.getType()));
        tabLayout.setSelectedTabIndicatorColor(color);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (viewModel != null) {
                    Circuit circuit = viewModel.getCircuits().get(tab.getPosition());
                    int color = ContextCompat.getColor(FragmentCircuits.this.getContext(), PlaceConstants.CATEGORY_COLORS.get(circuit.getType()));
                    tabLayout.setSelectedTabIndicatorColor(color);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }



    private static class ShowCircuitsAsyncTask extends AsyncTask<Void, Void, CircuitListViewModel> {

        private WeakReference<FragmentCircuits> fragment;
        private final AppDatabase mDb;

        public ShowCircuitsAsyncTask(FragmentCircuits fragment, AppDatabase db) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
        }

        @Override
        protected CircuitListViewModel doInBackground(final Void... params) {
            List<Circuit> circuits = mDb.circuit().selectAllSynchronous();
            CircuitListViewModel viewModel = new CircuitListViewModel(circuits);
            return viewModel;
        }

        @Override
        protected void onPostExecute(CircuitListViewModel viewModel) {
            final FragmentCircuits fragment = this.fragment.get();
            if (fragment != null) {
                fragment.showViewPager(viewModel);
            }
        }
    }
}
