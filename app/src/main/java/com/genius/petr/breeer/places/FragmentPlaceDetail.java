package com.genius.petr.breeer.places;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.view.KeyEvent;
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

    public static final String ARGUMENT_SHOW_MAP_BUTTON = "showMapButton";

    private TextView tvPlaceName;
    private TextView tvPlaceType;
    private TextView tvAddress;
    private TextView tvDescription;
    private TextView tvPhone;
    private TextView tvWeb;
    private Button mapButton;

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
        this.tvAddress = view.findViewById(R.id.tv_address);
        this.tvDescription = view.findViewById(R.id.tv_description);
        this.tvPhone = view.findViewById(R.id.tv_phone);
        this.tvWeb = view.findViewById(R.id.tv_web);
        this.mapButton = view.findViewById(R.id.button_showOnMap);

        viewModel= ViewModelProviders.of(this,
                new PlaceDetailViewModelFactory(this.getActivity().getApplication(), id))
                    .get(PlaceDetailViewModel.class);

        viewModel.getPlace().observe(this, new Observer<Place>() {
            @Override
            public void onChanged(@Nullable Place place) {
                if (place == null) {
                    return;
                }

                int color = ContextCompat.getColor(getContext(), PlaceConstants.CATEGORY_COLORS.get(place.getCategory()));


                tvPlaceName.setText(place.getName());
                tvPlaceType.setText(PlaceConstants.CATEGORY_NAMES.get(place.getCategory()));

                tvAddress.setText(place.getAddress());
                tvAddress.setTextColor(color);
                if (place.getAddress().isEmpty()) {
                    tvAddress.setVisibility(View.GONE);
                }

                tvDescription.setText(place.getDescription());
                tvPhone.setText(place.getPhone());
                tvWeb.setText(place.getWeb());


                mapButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                tvDescription.setLinkTextColor(color);
                tvPlaceType.setTextColor(color);
                tvPhone.setTextColor(color);
                tvPhone.setLinkTextColor(color);
                tvWeb.setTextColor(color);
                tvWeb.setLinkTextColor(color);
            }
        });



        AppCompatImageButton upButton = view.findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity)getActivity();
                activity.onBackPressed();
            }
        });


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity)getActivity();
                Place place = viewModel.getPlace().getValue();
                activity.showPlaceOnMap(place);
            }
        });

        Bundle args = getArguments();
        if (args.containsKey(ARGUMENT_SHOW_MAP_BUTTON)) {
            boolean showMapButton = args.getBoolean(ARGUMENT_SHOW_MAP_BUTTON);
            if (!showMapButton) {
                mapButton.setVisibility(View.GONE);
            }
        }

        view.findViewById(R.id.loadingOverlay).setVisibility(View.GONE);

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

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = ((MainActivity)getActivity());
        if (activity!=null) {
            activity.secondLayerFragmentSelected();
        }
    }
}
