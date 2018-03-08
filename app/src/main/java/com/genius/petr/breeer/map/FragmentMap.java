package com.genius.petr.breeer.map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.CircuitNode;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 24. 2. 2018.
 */

public class FragmentMap extends Fragment {

    private static final String TAG = "mapFragmentLog";

    private MapView mMapView;
    private GoogleMap googleMap;
    private ClusterManager<PlaceCluster> clusterManager;

    private static final String STATE_MAP_CAMERA = "map camera";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i("Breeer", "OnCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        testBullshit(rootView);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                clusterManager = new ClusterManager<>(getContext(), googleMap);
                googleMap.setOnCameraIdleListener(clusterManager);

                if (savedInstanceState != null) {
                    CameraPosition cameraPosition = savedInstanceState.getParcelable(STATE_MAP_CAMERA);
                    if (cameraPosition != null) {
                        //googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }

                AddMarkersAsyncTask task = new AddMarkersAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), PlaceConstants.CATEGORIES);
                task.execute();
                // For showing a move to my location button1
                //googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                //LatLng sydney = new LatLng(-34, 151);
                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
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
                ShowCircuitAsyncTask task = new ShowCircuitAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), 4);
                task.execute();
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

    private void showCircuit(CircuitMapWrapper circuit) {
        clusterManager.clearItems();
        clusterManager.cluster();

        for (Place place : circuit.getStops()) {
            MarkerOptions markerOptions = new MarkerOptions().position(place.getPosition()).title(Long.toString(place.getId()));
            googleMap.addMarker(markerOptions);
        }

        List<LatLng> path = circuit.getPath();

        if (path.size() < 2) {
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng node : path) {
            polylineOptions.add(node);
        }

        googleMap.addPolyline(polylineOptions);
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
            List<Place> stops = mDb.circuit().getStopsOfCircuit(circuitId);

            List<CircuitNode> nodes = mDb.circuit().getNodesOfCircuit(circuitId);
            List<LatLng> path = new ArrayList<>();

            for (CircuitNode node : nodes) {
                path.add(node.getPosition());
            }

            CircuitMapWrapper circuitMapWrapper = new CircuitMapWrapper(stops, path);

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