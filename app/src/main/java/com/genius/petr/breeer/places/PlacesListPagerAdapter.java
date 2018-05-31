package com.genius.petr.breeer.places;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class PlacesListPagerAdapter extends PagerAdapter {

    private Context context;
    private PlacesListViewModel viewModel;
    private View.OnClickListener placeClickListener;

    private static final String TAG = "placesListAdapterLog";


    public PlacesListPagerAdapter(Context context, PlacesListViewModel viewModel, View.OnClickListener placeClickListener) {
        this.context = context;
        this.viewModel = viewModel;
        this.placeClickListener = placeClickListener;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {

        Log.i(TAG, "position: " + position);

        final List<PlaceViewModel> places = viewModel.getPlacesOfCategory(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_place_list, collection, false);
        View background = layout.findViewById(R.id.background);
        int color = ResourcesCompat.getColor(context.getResources(), PlaceConstants.CATEGORY_COLORS.get(position), null);
        background.setBackgroundColor(color);

        ImageView backgroundImage = layout.findViewById(R.id.backgroundImage);
        backgroundImage.setImageResource(PlaceConstants.CATEGORY_ICONS_BLACK.get(position));

        RecyclerView recyclerView = layout.findViewById(R.id.list);
        PlaceAdapter placeAdapter = new PlaceAdapter(places, placeClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(placeAdapter);

        Button mapButton = layout.findViewById(R.id.button_map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMapClick(viewModel.getActivePostion());
                Log.i(TAG, "showcing cat: " + viewModel.getActivePostion());
            }
        });

        collection.addView(layout);
        return layout;
    }

    public void onMapClick(int position) {

    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return viewModel.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PlaceConstants.CATEGORY_NAMES.get(position);
    }
}
