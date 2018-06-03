package com.genius.petr.breeer.map;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.circuits.CircuitMapPagerAdapter;
import com.genius.petr.breeer.circuits.CircuitMapViewModel;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import org.w3c.dom.Document;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * Created by Petr on 24. 2. 2018.
 */

public class FragmentMap
        extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLoadedCallback {

    private static final String TAG = "mapFragmentLog";
    private static final String PLACE_ESSENTIALS_TAG = "placeEssentialsFragment";

    private MapView mMapView;
    private GoogleMap map;
    private ClusterManager<PlaceCluster> clusterManager;

    private LinearLayout filtersLayout;
    private GridLayout filtersGrid;
    private CheckBox filtersCheckbox;

    private List<Marker> circuitMarkers;
    private CircuitMapViewModel activeCircuit;
    private Place activePlace;
    private Marker activeMarker = null;
    private long activePlaceId = -1;

    private boolean showingCategory = false;

    private static final int STATE_PLACES = 0;
    private static final int STATE_CIRCUIT = 1;
    private static final int STATE_SINGLE = 2;
    private static final int STATE_NAVIGATION = 3;
    private static final int STATE_NAVIGATION_DETAIL = 4;
    private int currentState = STATE_PLACES;


    private static final String SAVE_STATE_STATE = "state";
    private static final String SAVE_STATE_CIRCUIT = "active_circuit";
    private static final String SAVE_STATE_PLACE = "active_place";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i("Breeer", "OnCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        rootView.findViewById(R.id.loadingOverlay).setVisibility(View.VISIBLE);
        ProgressBar progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorLightGray), PorterDuff.Mode.SRC_IN );

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVE_STATE_STATE)) {
                currentState = savedInstanceState.getInt(SAVE_STATE_STATE);
            }
            if(currentState == STATE_CIRCUIT) {
                long id = savedInstanceState.getLong(SAVE_STATE_CIRCUIT);
                Log.i("circuitTest", "restored id: " + id);
                activeCircuit = new CircuitMapViewModel(id);
            }
            if(currentState == STATE_SINGLE || currentState == STATE_NAVIGATION || currentState == STATE_NAVIGATION_DETAIL) {
                long id = savedInstanceState.getLong(SAVE_STATE_PLACE);
                Log.i("placeTest", "restored id: " + id);
                activePlaceId = id;
            }
        }

        setupFilters(rootView);

        mMapView.getMapAsync(this);

        return rootView;
    }


    private void setupFilters(View rootView) {
        filtersLayout = rootView.findViewById(R.id.filtersLayout);
        filtersGrid = filtersLayout.findViewById(R.id.filtersGrid);
        filtersCheckbox = filtersLayout.findViewById(R.id.checkBoxFilters);


        final ViewTreeObserver viewTreeObserver = filtersGrid.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    filtersGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    final int width = filtersGrid.getWidth();

                    filtersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                filtersLayout.setTranslationX(width);
                                filtersLayout.animate()
                                        .translationXBy(-width)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                filtersGrid.setVisibility(View.VISIBLE);
                                            }
                                        });
                            } else {
                                //filtersLayout.setTranslationX();
                                filtersLayout.animate()
                                        .translationXBy(width)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                filtersGrid.setVisibility(View.INVISIBLE);
                                            }
                                        });
                            }
                        }
                    });

                    if (!filtersCheckbox.isChecked()) {
                        filtersLayout.setTranslationX(width);
                    }
                }

            });

            if (currentState != STATE_PLACES) {
                filtersLayout.setVisibility(View.GONE);
            }
        }


        for(int i=0; i < filtersGrid.getChildCount(); i++) {
            View child = filtersGrid.getChildAt(i);
            if (child instanceof CheckBox) {
                final CheckBox filter = (CheckBox) child;

                filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (!filter.isPressed())
                        {
                            // Not from user!
                            return;
                        }
                        refreshMarkers();
                    }
                });

            }
        }
    }

    private void refreshMarkers(){
        List<Integer> categories = new ArrayList<>();
        int category = 0;
        
        for(int i=0; i < filtersGrid.getChildCount(); i++) {
            View child = filtersGrid.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox filter = (CheckBox)child;
                if (filter.isChecked()) {
                    categories.add(category);
                }
            }
            category++;
        }

        for (int cat : categories) {
            Log.i(TAG, "cat displayed: " + cat);
        }

        AddMarkersAsyncTask task = new AddMarkersAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), categories);
        task.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        this.map = googleMap;
        map.setOnMapLoadedCallback(this);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(49.19522, 16.60796) , 14.0f) );

        clusterManager = new ClusterManager<>(getContext(), googleMap);
        clusterManager.setRenderer(new CustomMapClusterRenderer<>(getContext(),map, clusterManager));
        clusterManager.setAlgorithm(new ClusteringAlgorithm<PlaceCluster>());

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);


        if (currentState == STATE_SINGLE) {
            final RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
            placeDetail.setVisibility(View.GONE);
        }

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!isLocationAvailable()) {
                    Toast.makeText(getContext(), "Your phone has no idea where you are. Are you sure it has enabled location?", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

    }

    private boolean isLocationAvailable(){
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch (Exception ex){}

        return (gps_enabled || network_enabled);
    }

    @Override
    public void onMapLoaded() {
        MainActivity activity = (MainActivity)getActivity();
        activity.tryToEnableLocation();

        View overlay = getView().findViewById(R.id.loadingOverlay);
        overlay.setVisibility(View.GONE);
        if (currentState == STATE_PLACES) {
            refreshMarkers();
        }

        if (currentState == STATE_CIRCUIT) {
            Log.i("circuitTest", "showing circuit: " + activeCircuit.getId());
            showCircuit(activeCircuit.getId());
        }

        if (currentState == STATE_SINGLE || currentState == STATE_NAVIGATION || currentState == STATE_NAVIGATION_DETAIL) {
            selectPlace(activePlaceId);
        }
    }



    public boolean onMarkerClick(Marker marker) {

        if (currentState == STATE_SINGLE)  {
            return true;
        }

        if (currentState == STATE_PLACES || currentState == STATE_NAVIGATION) {
            Log.i(TAG, "onMarkerClick");
            //cluster
            if (marker.getTitle() == null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        marker.getPosition(), (float) Math.floor(map
                                .getCameraPosition().zoom + 1)), 300,
                        null);
                return true;
            } else {
                long id = Long.parseLong(marker.getTitle());
                selectPlace(id);
                return true;
            }
        }

        if (currentState == STATE_CIRCUIT) {
            updateCircuitActiveMarker(marker);
            selectCircuitNode(marker);
            return true;
        }

        return true;
    }

    private void updateCircuitActiveMarker(Marker active){
        int color = getResources().getColor(PlaceConstants.CATEGORY_COLORS.get(activeCircuit.getType()));
        BitmapDescriptor markerIcon = MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit, color);

        //old active
        if (activeMarker != null) {
            activeMarker.setIcon(markerIcon);
        }

        activeMarker = active;
        activeMarker.setIcon(MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit));
    }

    private void selectCircuitNode(Marker marker){

        RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
        ViewPager viewPager = circuitLayout.findViewById(R.id.viewpager_circuitStops);
        viewPager.setVisibility(View.VISIBLE);
        int position = 0;
        for (Marker m : circuitMarkers) {
            if (m.equals(marker)) {
                viewPager.setCurrentItem(position+1);
                break;
            }
            position++;
        }
    }


    private void animateLatLngZoom(LatLng latlng, int reqZoom, int offsetX, int offsetY) {

        if (map == null) {
            return;
        }

        // Save current zoom
        float originalZoom = map.getCameraPosition().zoom;

        // Move temporarily camera zoom
        map.moveCamera(CameraUpdateFactory.zoomTo(reqZoom));

        Point pointInScreen = map.getProjection().toScreenLocation(latlng);

        Point newPoint = new Point();
        newPoint.x = pointInScreen.x + offsetX;
        newPoint.y = pointInScreen.y + offsetY;

        LatLng newCenterLatLng = map.getProjection().fromScreenLocation(newPoint);

        // Restore original zoom
        map.moveCamera(CameraUpdateFactory.zoomTo(originalZoom));

        // Animate a camera with new latlng center and required zoom.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, reqZoom));

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentState == STATE_SINGLE) {
            hideCurrentState();
            activateStatePlaces();
            return;
        }

        if (currentState == STATE_NAVIGATION_DETAIL) {
            hidePlaceDetail();
            return;
        }

        if (currentState == STATE_CIRCUIT) {
            deactivateCircuitNode();
            return;
        }
    }

    private void deactivateCircuitNode(){
        int color = getResources().getColor(PlaceConstants.CATEGORY_COLORS.get(activeCircuit.getType()));
        BitmapDescriptor markerIcon = MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit, color);

        //old active
        if (activeMarker != null) {
            activeMarker.setIcon(markerIcon);
            activeMarker = null;
        }

        ViewPager viewPager = getView().findViewById(R.id.viewpager_circuitStops);
        viewPager.setVisibility(View.GONE);
    }

    public void navigationFail(){
        RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
        Button navigateButton = placeDetail.findViewById(R.id.button_startNavigation);
        navigateButton.setEnabled(true);
        Toast.makeText(getContext(), "Your phone has no idea where you are.", Toast.LENGTH_LONG).show();
    }

    public void navigationDenied(){
        RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
        Button navigateButton = placeDetail.findViewById(R.id.button_startNavigation);
        navigateButton.setEnabled(true);
        Toast.makeText(getContext(), "Can't navigate because we don't know where you are.", Toast.LENGTH_LONG).show();
    }

    private void navigateToActivePlace() {

        MainActivity activity = (MainActivity)getActivity();
        activity.tryToNavigateToActivePlace();
    }


    private void activateStateNavigation(NavResponse response) {

        currentState = STATE_NAVIGATION;

        activePlace = response.getPlace();
        activePlaceId = activePlace.getId();

        MarkerOptions markerOptions = new MarkerOptions().position(activePlace.getPosition()).title(Long.toString(activePlace.getId()))
                .icon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS.get(activePlace.getCategory())));

        if(map!=null) {
            activeMarker = map.addMarker(markerOptions);

            final TableLayout navigationLayout = getView().findViewById(R.id.navigationLayout);

            int colorId = PlaceConstants.CATEGORY_COLORS.get(activePlace.getCategory());
            navigationLayout.setBackgroundColor(getActivity().getResources().getColor(colorId));

            Button stopButton = navigationLayout.findViewById(R.id.button_stopNavigation);

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideCurrentState();
                    activateStatePlaces();
                }
            });


            TextView nameTextView = navigationLayout.findViewById(R.id.placeName);
            nameTextView.setText(activePlace.getName());


            navigationLayout.setVisibility(View.VISIBLE);
            navigationLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener(){

                        @Override
                        public void onGlobalLayout() {
                            Log.i(TAG, "on global layout");
                            // gets called after layout has been done but before display
                            navigationLayout.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                            int height = navigationLayout.getHeight();
                            map.setPadding(0, height, 0, 0);
                            int viewHeight = getView().getHeight();


                            navigationLayout.setTranslationY(-height);
                            navigationLayout.animate().translationY(0).setDuration(700).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    navigationLayout.clearAnimation();
                                    navigationLayout.animate().setListener(null);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    animation.removeAllListeners();
                                }
                            });
                        }

                    });


        }

    }

    private void startNavigation(NavResponse navResponse) {
        if (map == null) {
            return;
        }


        if (navResponse == null) {
            Toast.makeText(getContext(), "Unable to find directions. Check your internet connection and try again please.", Toast.LENGTH_LONG).show();

            RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
            if (placeDetail.getVisibility() == View.VISIBLE) {
                Button navigateButton = placeDetail.findViewById(R.id.button_startNavigation);
                navigateButton.setEnabled(true);
                return;
            }
        }

        hideCurrentState();
        activateStateNavigation(navResponse);

        Place place = navResponse.getPlace();
        ArrayList<LatLng> navPoints = navResponse.getNavPoints();

        int colorId = PlaceConstants.CATEGORY_COLORS.get(place.getCategory());
        PolylineOptions rectLine = new PolylineOptions().width(8).color(getActivity().getResources().getColor(colorId));

        Log.d("route", "navPoints: " + navPoints.size());
        for (LatLng navPoint : navPoints) {
            rectLine.add(navPoint);
        }

        Polyline route = map.addPolyline(rectLine);
    }


    public void startNavigationToActivePlace(LatLng userLocation){
        Log.i("route", "starting task");
        new GMapV2DirectionAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), activePlace.id, userLocation).execute();
    }



    public void selectPlace(final Place place) {


        Log.i(TAG, "showing place: " + place.getName());

        if ((currentState == STATE_NAVIGATION || currentState == STATE_NAVIGATION_DETAIL) && (place.getId() == activePlace.getId())) {
            currentState = STATE_NAVIGATION_DETAIL;
        } else {
            hideCurrentState();
            currentState = STATE_SINGLE;

            int category = place.getCategory();
            View f = filtersGrid.getChildAt(category);
            if (f instanceof CheckBox) {
                final CheckBox filter = (CheckBox) f;
                filter.setChecked(true);
            }


        }
        activePlace = place;
        activePlaceId = place.getId();

        MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId()))
                .icon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS_ACTIVE.get(place.getCategory())));

        if(map!=null) {
            Log.i(TAG, "map ok");
            if(activeMarker != null) {
                activeMarker.remove();
            }
            activeMarker = map.addMarker(markerOptions);

            final RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
            TextView tvName = placeDetail.findViewById(R.id.placeName);
            TextView tvCategory = placeDetail.findViewById(R.id.placeCategory);
            TextView tvDescription = placeDetail.findViewById(R.id.placeDescription);
            final Button navigateButton = placeDetail.findViewById(R.id.button_startNavigation);
            Button detailButton = placeDetail.findViewById(R.id.button_detail);


            navigateButton.setEnabled(true);
            navigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToActivePlace();
                    navigateButton.setEnabled(false);
                }
            });

            final long id = place.getId();

            detailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).showPlaceDetail(id);
                }
            });

            tvName.setText(place.getName());
            tvCategory.setText(PlaceConstants.CATEGORY_NAMES.get(place.getCategory()));
            tvDescription.setText(place.getDescription());
            int color = ContextCompat.getColor(getContext(), PlaceConstants.CATEGORY_COLORS.get(place.getCategory()));
            tvCategory.setTextColor(color);
            detailButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            final LatLng position = place.getPosition();
            placeDetail.setVisibility(View.VISIBLE);
            placeDetail.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener(){

                        @Override
                        public void onGlobalLayout() {
                            Log.i(TAG, "on global layout");
                            // gets called after layout has been done but before display
                            placeDetail.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                            int height = placeDetail.getHeight();
                            int viewHeight = getView().getHeight();

                            int newCenter = (viewHeight + height) / 2;
                            int offsetY = newCenter - (viewHeight/2);


                            if (map!=null) {
                                float zoom = map.getCameraPosition().zoom;
                                if (zoom < 16) {
                                    zoom = 16;
                                }
                                animateLatLngZoom(position, (int)zoom, 0, offsetY);
                            }

                            placeDetail.setTranslationY(height);
                            placeDetail.animate().translationY(0).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    Log.i(TAG, "on show anim end");
                                    Log.i(TAG, "visibility: " + placeDetail.getVisibility());
                                    placeDetail.clearAnimation();
                                    placeDetail.animate().setListener(null);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    animation.removeAllListeners();
                                }
                            });

                            final View content = placeDetail.findViewById(R.id.content);
                            content.setAlpha(0f);
                            content.animate().alpha(1f).setDuration(800).setStartDelay(250).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    content.clearAnimation();
                                    content.animate().setListener(null);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    animation.removeAllListeners();
                                }
                            });


                            /*
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            view.setVisibility(View.GONE);
                                        }
                                    });
                                    */
                        }

                    });

        }
    }

    public void selectPlace(long placeId) {
        Log.i(TAG, "selectPlace called: " + placeId);
        ShowPlaceAsyncTask task = new ShowPlaceAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), placeId);
        task.execute();
    }

    public void showCategory(int category) {

        hideCurrentState();

        for(int i=0; i < filtersGrid.getChildCount(); i++) {
            View child = filtersGrid.getChildAt(i);
            if (child instanceof CheckBox) {
                final CheckBox filter = (CheckBox) child;
                if (i==category) {
                    filter.setChecked(true);
                } else {
                    filter.setChecked(false);
                }
            }
        }

        showingCategory = true;
        activateStatePlaces();

    }

    public void closePlace(){
        hideCurrentState();
        activateStatePlaces();
    }


    private void hidePlaceDetail(){
        if (map != null) {

            if (currentState == STATE_NAVIGATION_DETAIL) {
                activeMarker.remove();
                MarkerOptions markerOptions = new MarkerOptions().position(activePlace.getPosition()).title(Long.toString(activePlace.getId()))
                        .icon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS.get(activePlace.getCategory())));
                activeMarker = map.addMarker(markerOptions);
                currentState = STATE_NAVIGATION;
            } else {
                map.clear();
                activePlace = null;
            }
        }

        final RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
        int height = placeDetail.getHeight();

        placeDetail.animate().translationY(height).setDuration(1000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                placeDetail.setVisibility(View.GONE);
                placeDetail.clearAnimation();
                placeDetail.animate().setListener(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });

        View content = placeDetail.findViewById(R.id.content);
        content.animate().alpha(0f).setDuration(700);
    }


    @Override
    public void onResume() {
        super.onResume();
        //activeMarker = null;
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        /*
        //todo: this is now called to prevent crash when app was suspended to background and restored (it crashed when you unselect marker)
        if (currentState == STATE_NAVIGATION_DETAIL) {
            hidePlaceDetail();
        }
        */

        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        Log.i(TAG, "onSaveCalled");
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
        if (currentState == STATE_NAVIGATION || currentState == STATE_NAVIGATION_DETAIL) {
            outState.putInt(SAVE_STATE_STATE, STATE_SINGLE);
        } else {
            outState.putInt(SAVE_STATE_STATE, currentState);
        }
        Log.i("circuitTest", "current state: " + currentState);
        if (currentState == STATE_CIRCUIT) {
            outState.putLong(SAVE_STATE_CIRCUIT, activeCircuit.getId());
            Log.i("circuitTest", "saving id: " + activeCircuit.getId());
        }
        if (currentState == STATE_SINGLE) {
            outState.putLong(SAVE_STATE_PLACE, activePlace.getId());
        }

        if (currentState == STATE_NAVIGATION) {
            outState.putLong(SAVE_STATE_PLACE, activePlace.getId());
        }

        if (currentState == STATE_NAVIGATION_DETAIL) {
            outState.putLong(SAVE_STATE_PLACE, activePlace.getId());
        }
    }

    private  void addClusters(List<PlaceCluster> clusters){

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        clusterManager.clearItems();
        for (PlaceCluster cluster : clusters) {
            clusterManager.addItem(cluster);
            boundsBuilder.include(cluster.getPosition());
        }

        clusterManager.cluster();

        //todo: find a more elegant way to handle this?
        if (showingCategory) {
            showingCategory = false;

            LatLngBounds bounds = boundsBuilder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int padding = (int)(width*0.2); // offset from edges of the map in pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cameraUpdate);
        }
    }

    public void showCircuit(Long id){
        hideCurrentState();
        Log.i("circuitTest", "id: " + id);
        ShowCircuitAsyncTask task = new ShowCircuitAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), id);
        task.execute();
    }

    private void hideCurrentState() {
        if (map != null) {
            map.setPadding(0, 0, 0, 0);
        }

        if (currentState == STATE_PLACES) {
            if (clusterManager != null) {
                clusterManager.clearItems();
                clusterManager.cluster();
            }
            if (map != null) {
                map.clear();
            }

            filtersLayout.setVisibility(View.GONE);
        }

        if (currentState == STATE_CIRCUIT) {
            if (map != null) {
                map.clear();
            }
            activeCircuit = null;
            if (circuitMarkers != null) {
                circuitMarkers.clear();
                circuitMarkers = null;
            }
            RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
            circuitLayout.setVisibility(View.GONE);
        }

        if (currentState == STATE_SINGLE) {
            hidePlaceDetail();
        }

        if (currentState == STATE_NAVIGATION) {
            TableLayout navigationLayout = getView().findViewById(R.id.navigationLayout);
            navigationLayout.setVisibility(View.GONE);
            if (map != null) {
                map.clear();
            }
        }

        if (currentState == STATE_NAVIGATION_DETAIL) {
            TableLayout navigationLayout = getView().findViewById(R.id.navigationLayout);
            navigationLayout.setVisibility(View.GONE);
            if (map != null) {
                map.clear();
            }
            if (activePlace != null) {
                hidePlaceDetail();
            }
        }
    }

    private void showCircuit(CircuitMapViewModel viewModel) {
        activeCircuit = viewModel;
        circuitMarkers = new ArrayList<>();
        int color = getResources().getColor(PlaceConstants.CATEGORY_COLORS.get(activeCircuit.getType()));
        BitmapDescriptor markerIcon = MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit, color);

        for (Place place : activeCircuit.getStops()) {
            MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId())).icon(markerIcon).anchor(0.5f, 0.5f);
            Marker marker = map.addMarker(markerOptions);
            circuitMarkers.add(marker);
        }

        activeMarker = circuitMarkers.get(0);

        List<LatLng> path = activeCircuit.getPath();

        if (path.size() < 2) {
            return;
        }

        RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
        RelativeLayout circuitLabelLayout = getView().findViewById(R.id.circuitLabelLayout);
        circuitLabelLayout.setBackgroundColor(color);
        circuitLayout.setVisibility(View.VISIBLE);



        TextView tvCircuitName = getView().findViewById(R.id.circuitName);
        tvCircuitName.setText(activeCircuit.getName());

        Button button_close = getView().findViewById(R.id.button_closeCircuit);
        button_close.setTextColor(color);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCircuit();
            }
        });

        final ViewPager viewPager = getView().findViewById(R.id.viewpager_circuitStops);

        //this is to add 2 dummy items to make viewpager "linked"
        List<Place> places = viewModel.getStops();
        places.add(places.get(0));
        places.add(0, places.get(places.size()-2));


        final CircuitMapPagerAdapter adapter = new CircuitMapPagerAdapter(getContext(), viewModel) {
            @Override
            public  void callback(long placeId){
                Log.i("callbackTest", "from fragment");
                Log.i("callbackTest", "id: " +placeId);
            MainActivity activity = (MainActivity)getActivity();
            activity.showPlaceDetail(placeId, false);
            }

            @Override
            public void leftButton(){
                viewPager.arrowScroll(View.FOCUS_LEFT);
            }

            @Override
            public void rightButton(){
                viewPager.arrowScroll(View.FOCUS_RIGHT);
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1, false);


        viewPager.setVisibility(View.VISIBLE);

        circuitLabelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RelativeLayout circuitLabelLayout = getView().findViewById(R.id.circuitLabelLayout);
                circuitLabelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int topPadding = circuitLabelLayout.getHeight();
                int bottomPadding = viewPager.getHeight();
                map.setPadding(0, topPadding, 0 , bottomPadding);
            }
        });


        //todo: remove this and rework panel so that viewpager is hidden until user selects a node
        int selectedPosition = viewPager.getCurrentItem();
        circuitMarkers.get(selectedPosition-1).setIcon(MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == circuitMarkers.size()+1) {
                    viewPager.setCurrentItem(1, false);
                    return;
                }
                if (position == 0) {
                    viewPager.setCurrentItem(circuitMarkers.size(), false); // false will prevent sliding                      animation of view pager
                    return;
                }


                LatLng location = circuitMarkers.get(position-1).getPosition();
                map.animateCamera(CameraUpdateFactory.newLatLng(location));
                updateCircuitActiveMarker(circuitMarkers.get(position-1));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        final PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng node : path) {
            polylineOptions.add(node);
            boundsBuilder.include(node);
        }


        map.addPolyline(polylineOptions);

        LatLngBounds bounds = boundsBuilder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width*0.2); // offset from edges of the map in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cameraUpdate);


        currentState = STATE_CIRCUIT;
    }

    public void closeCircuit(){
        hideCurrentState();
        activateStatePlaces();
    }

    public void activateStatePlaces(){
        refreshMarkers();
        //filtersLayout.setVisibility(View.VISIBLE);
        setupFilters(getView());
        filtersLayout.setVisibility(View.VISIBLE);
        currentState = STATE_PLACES;
    }


    private static class AddMarkersAsyncTask extends AsyncTask<Void, Void, List<PlaceCluster>> {

        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private List<Integer> categories;

        public AddMarkersAsyncTask(FragmentMap fragment, AppDatabase db, List<Integer> categories) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.categories = categories;
        }

        @Override
        protected List<PlaceCluster> doInBackground(final Void... params) {
            List<PlaceCluster> clusters = new ArrayList<>();

            for (int category : categories) {
                List<Place> placesOfCategory = mDb.place().getListByCategory(category);

                for (Place place : placesOfCategory) {
                    PlaceCluster cluster = new PlaceCluster(new LatLng(place.getLat(), place.getLng()), place.getId(), category);
                    clusters.add(cluster);
                }
            }


            return clusters;
        }

        @Override
        protected void onPostExecute(List<PlaceCluster> clusters) {
            final FragmentMap fragment = this.fragment.get();
            if (fragment != null) {
                fragment.addClusters(clusters);
            }
        }
    }

    private static class ShowCircuitAsyncTask extends AsyncTask<Void, Void, CircuitMapViewModel> {

        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private long circuitId;

        public ShowCircuitAsyncTask(FragmentMap fragment, AppDatabase db, long id) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.circuitId = id;
        }

        @Override
        protected CircuitMapViewModel doInBackground(final Void... params) {
            CircuitMapViewModel viewModel = new CircuitMapViewModel(mDb, circuitId);

            return viewModel;
        }

        @Override
        protected void onPostExecute(CircuitMapViewModel viewModel) {
            final FragmentMap fragment = this.fragment.get();
            if (fragment != null) {
                fragment.showCircuit(viewModel);
            }
        }
    }

    private static class ShowPlaceAsyncTask extends AsyncTask<Void, Void, Place> {

        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private long placeId;

        public ShowPlaceAsyncTask(FragmentMap fragment, AppDatabase db, long id) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.placeId = id;
        }

        @Override
        protected Place doInBackground(final Void... params) {
            Place place = mDb.place().selectByIdSynchronous(placeId);
            Log.i(TAG, "selecting place by id: " + placeId);
            Log.i(TAG, "place: " + place.getName());

            return place;
        }

        @Override
        protected void onPostExecute(Place place) {
            final FragmentMap fragment = this.fragment.get();
            if (fragment != null) {
                fragment.selectPlace(place);
            }
        }
    }

    private static class GMapV2DirectionAsyncTask extends AsyncTask<String, Void, NavResponse> {

        private LatLng start, end;
        private String mode;
        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private long placeId;
        private String apiKey;

        public GMapV2DirectionAsyncTask(FragmentMap fragment, AppDatabase db, long id, LatLng start) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.placeId = id;

            this.apiKey = fragment.getContext().getResources().getString(R.string.google_maps_key);

            this.start = start;
            this.mode = GMapV2DirectionParser.MODE_WALKING;
        }

        @Override
        protected NavResponse doInBackground(String... params) {
            Place place = mDb.place().selectByIdSynchronous(placeId);
            this.end = place.getPosition();

            String url = "https://maps.googleapis.com/maps/api/directions/xml?"
                    + "origin=" + start.latitude + "," + start.longitude
                    + "&destination=" + end.latitude + "," + end.longitude
                    + "&sensor=false&units=metric&mode=" + mode
                    +"&key=" + apiKey;
            Log.d("url", url);
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                int responseCode = con.getResponseCode();
                Log.d("route", "Sending 'Get' request to URL : " +    url+"--"+responseCode);

                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = builder.parse(con.getInputStream());

                GMapV2DirectionParser md = new GMapV2DirectionParser();
                ArrayList<LatLng> directionPoint = md.getDirection(doc);

                return new NavResponse(place, directionPoint);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(NavResponse navResponse) {
            try {
                Log.d("route", "doc != null");


                final FragmentMap fragment = this.fragment.get();
                if (fragment != null) {
                    fragment.startNavigation(navResponse);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    //returns true if back was handled
    public boolean backPressed(){

        if (currentState == STATE_CIRCUIT) {
            Log.i(TAG, "circuit state");
            if (activeMarker != null) {
                Log.i(TAG, "marker !null");
                deactivateCircuitNode();
                return true;
            }
        }

        if (currentState == STATE_NAVIGATION_DETAIL) {
            hidePlaceDetail();
            return true;
        }


        //todo: if there is circuit displayed, show dialog to make sure they want to close it
        if (currentState != STATE_PLACES) {
            hideCurrentState();
            activateStatePlaces();
            return true;
        }
        return false;
    }

    public void enableMyLocation() {
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {

        }
    }

}