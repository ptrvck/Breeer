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
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Petr on 24. 2. 2018.
 */

public class FragmentMap extends Fragment {

    private static final String TAG = "mapFragmentLog";

    private MapView mMapView;
    private GoogleMap googleMap;
    private ClusterManager<PlaceCluster> clusterManager;

    private Button button;

    private static final String STATE_MAP_CAMERA = "map camera";

    //todo: zapamatovat si state mapy a pak ho obnovit
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i("Breeer", "OnCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        button = rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> categories = new ArrayList<>();
                categories.add(1);
                categories.add(3);
                categories.add(6);
                AddMarkersAsyncTask task = new AddMarkersAsyncTask(FragmentMap.this, AppDatabase.getDatabase(getContext().getApplicationContext()), categories);
                task.execute();
                clusterManager.clearItems();
                clusterManager.cluster();
            }
        });

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
                // For showing a move to my location button
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

    private  void addAllMarkers(Map<Integer, List<Place>> places){
        clusterManager.clearItems();
        for (Map.Entry<Integer, List<Place>> entry : places.entrySet()) {
            int category = entry.getKey();
            List<Place> placesOfCategory = entry.getValue();

            for (Place place : placesOfCategory) {
                PlaceCluster cluster = new PlaceCluster(new LatLng(place.getLat(), place.getLng()), place.getId(), category);
                clusterManager.addItem(cluster);
            }
        }
        clusterManager.cluster();
    }

    private static class AddMarkersAsyncTask extends AsyncTask<Void, Void, Map<Integer, List<Place>>> {

        private WeakReference<FragmentMap> fragment;
        private final AppDatabase mDb;
        private List<Integer> categories;

        public AddMarkersAsyncTask(FragmentMap fragment, AppDatabase db, List<Integer> categories) {
            this.fragment = new WeakReference<>(fragment);
            this.mDb = db;
            this.categories = categories;
        }

        @Override
        protected Map<Integer, List<Place>> doInBackground(final Void... params) {
            Map<Integer, List<Place>> places = new HashMap<>();

            for (int category : categories) {
                places.put(category, mDb.place().getListByCategory(category));
            }


            return places;
        }

        @Override
        protected void onPostExecute(Map<Integer, List<Place>> places) {
            final FragmentMap fragment = this.fragment.get();
            if (fragment != null) {
                fragment.addAllMarkers(places);
            }
        }
    }
}