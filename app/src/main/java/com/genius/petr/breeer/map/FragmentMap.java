package com.genius.petr.breeer.map;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.activity.MainActivity;
import com.genius.petr.breeer.circuits.CircuitMapPagerAdapter;
import com.genius.petr.breeer.circuits.CircuitMapViewModel;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.CircuitBase;
import com.genius.petr.breeer.database.CircuitNode;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.genius.petr.breeer.places.FragmentPlaceEssentials;
import com.genius.petr.breeer.places.FragmentPlaces;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Petr on 24. 2. 2018.
 */

public class FragmentMap
        extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveStartedListener {

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
    private float currentZoom = -1;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final int STATE_PLACES = 0;
    private static final int STATE_CIRCUIT = 1;
    private static final int STATE_SINGLE = 2;
    private int currentState = STATE_PLACES;


    private static final String SAVE_STATE_STATE = "state";
    private static final String SAVE_STATE_CIRCUIT = "active_circuit";
    private static final String SAVE_STATE_PLACE = "active_place";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i("Breeer", "OnCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

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
            if(currentState == STATE_SINGLE) {
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
        map.setOnCameraMoveStartedListener(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); // two minute interval
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                this.map.setMyLocationEnabled(true);
                Log.i(TAG, "location permission granted");
            } else {
                //Request Location Permission
                Log.i(TAG, "requesting location permission");
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            googleMap.setMyLocationEnabled(true);
        }


        // For showing a move to my location button1
        //googleMap.setMyLocationEnabled(true);

        // For dropping a marker at a point on the Map
        //LatLng sydney = new LatLng(-34, 151);
        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
        //CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
        //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (currentState == STATE_PLACES) {
            refreshMarkers();
        }

        if (currentState == STATE_CIRCUIT) {
            Log.i("circuitTest", "showing circuit: " + activeCircuit.getId());
            showCircuit(activeCircuit.getId());
        }

        if (currentState == STATE_SINGLE) {
            selectPlace(activePlaceId);
        }
    }



    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i(TAG, "show explanation");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                Log.i(TAG, "explanation not needed");
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        map.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }


    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());
                //mLastLocation = location;

                /*
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = googleMap.addMarker(markerOptions);

                //move map camera
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                */
            }
        };

    };

    /*
    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();

        if (currentState == STATE_SINGLE)  {
            return true;
        }

        if (currentState == STATE_PLACES) {
            Log.i(TAG, "onMarkerClick");
            //cluster
            if (marker.getTitle() == null) {
                hidePlaceInfo();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        marker.getPosition(), (float) Math.floor(map
                                .getCameraPosition().zoom + 1)), 300,
                        null);
                return true;
            } else {
                if (marker == activeMarker) {
                    return true;
                }
                unselectActiveMarker();
                long id = Long.parseLong(marker.getTitle());
                showPlaceInfo(id);
                int category = Integer.parseInt(marker.getSnippet());
                marker.setIcon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS_ACTIVE.get(category)));
                activeMarker = marker;
                return true;
            }
        }

        if (currentState == STATE_CIRCUIT) {
            //TODO
            return true;
        }

        return true;
    }
    */

    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();

        if (currentState == STATE_SINGLE)  {
            return true;
        }

        if (currentState == STATE_PLACES) {
            Log.i(TAG, "onMarkerClick");
            //cluster
            if (marker.getTitle() == null) {
                hidePlaceInfo();
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
            //TODO
            return true;
        }

        return true;
    }


    @Override
    public void onCameraMoveStarted(int reason) {
        CameraPosition cameraPosition = map.getCameraPosition();
        float zoom = cameraPosition.zoom;
        if (zoom != currentZoom) {
            currentZoom = zoom;
            hidePlaceInfo();
        }
    }



    @Override
    public void onMapClick(LatLng latLng) {
        hidePlaceInfo();
    }

    public void selectPlace(Place place) {
        hideCurrentState();

        Log.i(TAG, "showing place: " + place.getName());

        currentState = STATE_SINGLE;
        activePlace = place;
        activePlaceId = place.getId();

        if (map!=null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getPosition(), 16));
        }

        MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId()))
                .icon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS_ACTIVE.get(place.getCategory())));

        if(map!=null) {
            Marker marker = map.addMarker(markerOptions);

            RelativeLayout placeDetail = getView().findViewById(R.id.placeLayout);
            TextView tvName = placeDetail.findViewById(R.id.placeName);
            TextView tvCategory = placeDetail.findViewById(R.id.placeCategory);
            TextView tvDescription = placeDetail.findViewById(R.id.placeDescription);
            Button closeButton = placeDetail.findViewById(R.id.button_closePlace);
            Button detailButton = placeDetail.findViewById(R.id.button_detail);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closePlace();
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

            placeDetail.setVisibility(View.VISIBLE);

            Log.i(TAG, "showing marker: " + marker.getId());
        }
    }

    public void selectPlace(long placeId) {
        hideCurrentState();
        ShowPlaceAsyncTask task = new ShowPlaceAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), placeId);
        task.execute();
    }

    public void closePlace(){
        hideCurrentState();
        activateStatePlaces();
    }

    private void showPlaceInfo(long id) {
        Log.i(TAG, "marker clicked, id: " + id);

        FragmentManager manager = getChildFragmentManager();

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        FragmentPlaceEssentials newFragment = FragmentPlaceEssentials.newInstance(id);

        transaction.replace(R.id.place_essentials_frame, newFragment, PLACE_ESSENTIALS_TAG);

        transaction.commit();
    }

    private void unselectActiveMarker(){
        if (activeMarker != null) {
            int category = Integer.parseInt(activeMarker.getSnippet());
            activeMarker.setIcon(MapUtils.bitmapDescriptorFromVector(getContext(), PlaceConstants.CATEGORY_MARKERS.get(category)));
            activeMarker = null;
        }
    }

    private void hidePlaceInfo(){
        unselectActiveMarker();

        final FrameLayout fragmentLayout = getView().findViewById(R.id.place_essentials_frame);

        final FragmentManager manager = getChildFragmentManager();
        final Fragment fragment = manager.findFragmentByTag(PLACE_ESSENTIALS_TAG);

        if (fragment == null) {
            return;
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        TranslateAnimation trans=new TranslateAnimation(0, -width, 0,0);
        trans.setDuration(150);
        trans.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }
        });

        fragmentLayout.startAnimation(trans);
    }


    @Override
    public void onResume() {
        super.onResume();
        activeMarker = null;
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        outState.putInt(SAVE_STATE_STATE, currentState);
        Log.i("circuitTest", "current state: " + currentState);
        if (currentState == STATE_CIRCUIT) {
            outState.putLong(SAVE_STATE_CIRCUIT, activeCircuit.getId());
            Log.i("circuitTest", "saving id: " + activeCircuit.getId());
        }
        if (currentState == STATE_SINGLE) {
            outState.putLong(SAVE_STATE_PLACE, activePlace.getId());
        }
    }

    private  void addClusters(List<PlaceCluster> clusters){
        clusterManager.clearItems();
        for (PlaceCluster cluster : clusters) {
            clusterManager.addItem(cluster);
        }

        clusterManager.cluster();
    }

    public void showCircuit(Long id){
        hideCurrentState();
        Log.i("circuitTest", "id: " + id);
        ShowCircuitAsyncTask task = new ShowCircuitAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), id);
        task.execute();
    }

    private void hideCurrentState() {
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
            if (map != null) {
                map.clear();
            }
            activePlace = null;

            RelativeLayout placeLayout = getView().findViewById(R.id.placeLayout);
            placeLayout.setVisibility(View.GONE);
        }
    }

    private void showCircuit(CircuitMapViewModel viewModel) {

        circuitMarkers = new ArrayList<>();
        int color = getResources().getColor(PlaceConstants.CATEGORY_COLORS.get(viewModel.getType()));
        BitmapDescriptor markerIcon = MapUtils.bitmapDescriptorFromVector(FragmentMap.this.getContext(), R.drawable.marker_circuit, color);




        for (Place place : viewModel.getStops()) {
            MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId())).icon(markerIcon).anchor(0.5f, 0.5f);
            Marker marker = map.addMarker(markerOptions);
            circuitMarkers.add(marker);
        }




        List<LatLng> path = viewModel.getPath();

        if (path.size() < 2) {
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng node : path) {
            polylineOptions.add(node);
            boundsBuilder.include(node);
        }

        LatLngBounds bounds = boundsBuilder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int)(width*0.2); // offset from edges of the map in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cameraUpdate);


        RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
        circuitLayout.setVisibility(View.VISIBLE);


        TextView tvCircuitName = getView().findViewById(R.id.circuitName);
        tvCircuitName.setText(viewModel.getName());

        Button button_close = getView().findViewById(R.id.button_closeCircuit);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCircuit();
            }
        });

        ViewPager viewPager = getView().findViewById(R.id.viewpager_circuitStops);
        CircuitMapPagerAdapter adapter = new CircuitMapPagerAdapter(getContext(), viewModel);
        viewPager.setAdapter(adapter);
        viewPager.setVisibility(View.VISIBLE);

        map.addPolyline(polylineOptions);

        activeCircuit = viewModel;
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
}