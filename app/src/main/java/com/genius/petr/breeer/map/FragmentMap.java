package com.genius.petr.breeer.map;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
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
import com.genius.petr.breeer.circuits.CircuitMapPagerAdapter;
import com.genius.petr.breeer.circuits.CircuitMapViewModel;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.CircuitBase;
import com.genius.petr.breeer.database.CircuitNode;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.genius.petr.breeer.places.FragmentPlaceEssentials;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Petr on 24. 2. 2018.
 */

public class FragmentMap
        extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final String TAG = "mapFragmentLog";
    private static final String PLACE_ESSENTIALS_TAG = "placeEssentialsFragment";

    private MapView mMapView;
    private GoogleMap map;
    private ClusterManager<PlaceCluster> clusterManager;

    private LinearLayout filtersLayout;
    private GridLayout filtersGrid;
    private CheckBox filtersCheckbox;

    private List<Marker> circuitMarkers;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker mCurrLocationMarker;
    private Location mLastLocation;

    private static final int STATE_PLACES = 0;
    private static final int STATE_CIRCUIT = 1;
    private static final int STATE_SINGLE = 2;
    private int currentState = STATE_PLACES;


    private static final String STATE_MAP_CAMERA = "map camera";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i("Breeer", "OnCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        testBullshit(rootView);
        setupFilters(rootView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }


    private void setupFilters(View rootView) {
        filtersLayout = rootView.findViewById(R.id.filtersLayout);
        filtersGrid = filtersLayout.findViewById(R.id.filtersGrid);
        filtersCheckbox = filtersLayout.findViewById(R.id.checkBoxFilters);


        ViewTreeObserver viewTreeObserver = filtersGrid.getViewTreeObserver();
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
                }
            });
        }


        for(int i=0; i < filtersGrid.getChildCount(); i++) {
            View child = filtersGrid.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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
        clusterManager = new ClusterManager<>(getContext(), googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMapClickListener(this);

        AddMarkersAsyncTask task = new AddMarkersAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), PlaceConstants.CATEGORIES);
        task.execute();

        googleMap.setOnMarkerClickListener(this);

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
                mLastLocation = location;

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

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();

        //cluster
        if (title == null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    marker.getPosition(), (float) Math.floor(map
                            .getCameraPosition().zoom + 1)), 300,
                    null);
            return true;
        }

        long id = Long.parseLong(title);
        showPlaceInfo(id);

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hidePlaceInfo();
    }

    public void selectPlace(Place place) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getPosition(), 16));

        Collection<Marker> markers = clusterManager.getMarkerCollection().getMarkers();
        for (Marker marker : markers) {
            //id is stored in marker title
            if (marker.getTitle().equals(Long.toString(place.getId()))) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getPosition(), 14));
            }
        }
        //MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId()));
        //googleMap.addMarker(markerOptions);
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

    private void hidePlaceInfo(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        final FrameLayout fragmentLayout = getView().findViewById(R.id.place_essentials_frame);

        final FragmentManager manager = getChildFragmentManager();
        final Fragment fragment = manager.findFragmentByTag(PLACE_ESSENTIALS_TAG);

        if (fragment == null) {
            return;
        }

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

    private void testBullshit(View rootView) {
        Button button1 = rootView.findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> categories = new ArrayList<>();
                categories.add(1);
                categories.add(3);
                categories.add(6);
                AddMarkersAsyncTask task = new AddMarkersAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), categories);
                task.execute();
            }
        });

        Button button2 = rootView.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }

    private  void addClusters(List<PlaceCluster> clusters){
        clusterManager.clearItems();
        for (PlaceCluster cluster : clusters) {
            clusterManager.addItem(cluster);
        }

        clusterManager.cluster();
    }

    public void showCircuit(Long id){
        ShowCircuitAsyncTask task = new ShowCircuitAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), id);
        task.execute();
        hideCurrentState();
    }

    private void hideCurrentState() {
        if (currentState == STATE_PLACES) {
            clusterManager.clearItems();
            clusterManager.cluster();
            map.clear();

            filtersLayout.setVisibility(View.GONE);
        }

        if (currentState == STATE_CIRCUIT) {
            map.clear();
            RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
            circuitLayout.setVisibility(View.GONE);
        }
    }

    private void showCircuit(CircuitMapWrapper circuit) {

        circuitMarkers = new ArrayList<>();
        for (Place place : circuit.getStops()) {
            MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId()));
            Marker marker = map.addMarker(markerOptions);
            circuitMarkers.add(marker);
        }

        List<LatLng> path = circuit.getPath();

        if (path.size() < 2) {
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng node : path) {
            polylineOptions.add(node);
        }


        RelativeLayout circuitLayout = getView().findViewById(R.id.circuitLayout);
        circuitLayout.setVisibility(View.VISIBLE);


        TextView tvCircuitName = getView().findViewById(R.id.circuitName);
        tvCircuitName.setText(circuit.getName());

        Button button_close = getView().findViewById(R.id.button_closeCircuit);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCircuit();
            }
        });

        ViewPager viewPager = getView().findViewById(R.id.viewpager_circuitStops);
        CircuitMapViewModel viewModel = new CircuitMapViewModel(circuit.getStops());
        CircuitMapPagerAdapter adapter = new CircuitMapPagerAdapter(getContext(), viewModel);
        viewPager.setAdapter(adapter);
        viewPager.setVisibility(View.VISIBLE);

        map.addPolyline(polylineOptions);

        currentState = STATE_CIRCUIT;
    }

    public void closeCircuit(){
        hideCurrentState();
        activateStatePlaces();
    }

    public void activateStatePlaces(){
        refreshMarkers();
        filtersGrid.setVisibility(View.VISIBLE);
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

    private static class ShowCircuitAsyncTask extends AsyncTask<Void, Void, CircuitMapWrapper> {

        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private long circuitId;

        public ShowCircuitAsyncTask(FragmentMap fragment, AppDatabase db, long id) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.circuitId = id;
        }

        @Override
        protected CircuitMapWrapper doInBackground(final Void... params) {
            CircuitBase circuit = mDb.circuit().selectById(circuitId);

            List<Place> stops = mDb.circuit().getStopsOfCircuit(circuitId);

            List<CircuitNode> nodes = mDb.circuit().getNodesOfCircuit(circuitId);
            List<LatLng> path = new ArrayList<>();

            for (CircuitNode node : nodes) {
                path.add(node.getPosition());
            }

            CircuitMapWrapper circuitMapWrapper = new CircuitMapWrapper(circuit.getName(), stops, path);

            return circuitMapWrapper;
        }

        @Override
        protected void onPostExecute(CircuitMapWrapper circuitMapWrapper) {
            final FragmentMap fragment = this.fragment.get();
            if (fragment != null) {
                fragment.showCircuit(circuitMapWrapper);
            }
        }
    }
}