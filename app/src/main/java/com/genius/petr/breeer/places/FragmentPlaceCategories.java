package com.genius.petr.breeer.places;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;

/**
 * Created by Petr on 10. 3. 2018.
 */

public class FragmentPlaceCategories extends Fragment {

    private static final String TAG = "placeCategoriesFragment";
    public static final String ARGUMENT_CATEGORY = "category";
    public static final String ARGUMENT_LOCATION = "location";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place_categories, container, false);

        GridView gridView = rootView.findViewById(R.id.gridview);
        final CategoriesAdapter categoriesAdapter = new CategoriesAdapter(this.getContext());
        gridView.setAdapter(categoriesAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                PlaceCategory category = (PlaceCategory)categoriesAdapter.getItem(position);
                Log.i(TAG, "category clicked: " + category.getName());

                FragmentPlacesViewPager fragmentPlaces = new FragmentPlacesViewPager();
                Bundle args = new Bundle();
                args.putInt(ARGUMENT_CATEGORY, category.getNumber());
                fragmentPlaces.setArguments(args);

                MainActivity activity = (MainActivity) getActivity();

                activity.showFragmentPlaces(fragmentPlaces);
            }
        });

        return rootView;
    }
}
