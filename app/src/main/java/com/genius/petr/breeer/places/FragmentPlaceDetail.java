package com.genius.petr.breeer.places;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;

/**
 * Created by Petr on 21. 2. 2018.
 */

public class FragmentPlaceDetail extends Fragment {

    private PlaceDetailViewModel viewModel;

    private TextView tvPlaceName;
    private TextView tvPlaceType;
    private long id;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        this.id = args.getLong(Place.COLUMN_ID, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);
        this.tvPlaceName = view.findViewById(R.id.tv_placeName);
        this.tvPlaceType = view.findViewById(R.id.tv_placeType);

        viewModel= ViewModelProviders.of(this,
                new PlaceDetailViewModelFactory(this.getActivity().getApplication(), id))
                    .get(PlaceDetailViewModel.class);

        viewModel.getPlace().observe(this, new Observer<Place>() {
            @Override
            public void onChanged(@Nullable Place place) {
                tvPlaceName.setText(place.getName());
                tvPlaceType.setText(PlaceConstants.CATEGORY_NAMES.get(place.getCategory()));
            }
        });

        Button mapButton = view.findViewById(R.id.button_showOnMap);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity)getActivity();
                Place place = viewModel.getPlace().getValue();
                activity.showPlaceOnMap(place);
            }
        });

        return view;
    }

    public static FragmentPlaceDetail newInstance(long id) {
        FragmentPlaceDetail fragment = new FragmentPlaceDetail();
        Bundle args = new Bundle();
        args.putLong(Place.COLUMN_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    private static final Animation dummyAnimation = new AlphaAnimation(1,1);
    static{
        dummyAnimation.setDuration(500);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if(!enter && getParentFragment() != null){
            return dummyAnimation;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
