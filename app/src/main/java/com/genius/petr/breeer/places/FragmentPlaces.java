package com.genius.petr.breeer.places;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Place;

/**
 * Created by Petr on 9. 2. 2018.
 */

public class FragmentPlaces extends Fragment implements View.OnLongClickListener{

    private PlaceListViewModel viewModel;
    private PlaceAdapter placeAdapter;
    private RecyclerView recyclerView;
    public static final String ARGUMENT_CATEGORY = "category";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_list, container, false);

        long category = 0;
        Bundle arg = getArguments();
        if (arg.containsKey(ARGUMENT_CATEGORY)) {
            category = arg.getLong(ARGUMENT_CATEGORY);
        } else {
            //category to display not set
            return view;
        }


        //this is way too complicated for what i need to do
        recyclerView = view.findViewById(R.id.list);
        placeAdapter = new PlaceAdapter(new ArrayList<Place>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(placeAdapter);

        viewModel = ViewModelProviders.of(this,
                new PlaceListViewModelFactory(getActivity().getApplication(), category))
                .get(PlaceListViewModel.class);

        viewModel.getPlaceList().observe(getActivity(), new Observer<List<Place>>() {
            @Override
            public void onChanged(@Nullable List<Place> places) {
                placeAdapter.addItems(places);
            }
        });

        return view;
    }

    @Override
    public boolean onLongClick(View v) {
        Place place = (Place) v.getTag();

        ((MainActivity)this.getActivity()).showPlaceDetail(place.getId());

        return true;
    }
}