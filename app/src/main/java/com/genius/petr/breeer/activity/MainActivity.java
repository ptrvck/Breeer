package com.genius.petr.breeer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.genius.petr.breeer.circuits.FragmentCircuits;
import com.genius.petr.breeer.database.FragmentDb;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.map.FragmentMap;
import com.genius.petr.breeer.places.FragmentPlaceCategories;
import com.genius.petr.breeer.places.FragmentPlaceDetail;
import com.genius.petr.breeer.R;
import com.genius.petr.breeer.places.FragmentPlacesViewPager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Field;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BreeerViewPager viewPager;
    private static final String TAG = "mainLog";

    private Fragment placesFragmentToShow = null;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_NAVIGATION = 100;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_PLACES = 101;

    private FusedLocationProviderClient mFusedLocationClient;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FrameLayout fragmentLayout = findViewById(R.id.fragmentLayout);
            fragmentLayout.setVisibility(View.INVISIBLE);
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_circuits:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_places:
                    viewPager.setCurrentItem(2);
                    return true;
                    /*
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(3);
                return true;*/
            }
            return false;
        }
    };

    public void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, fragment).addToBackStack(null).commit();
    }

    public void showFragmentPlaces(Fragment fragment) {

        //there has to be a better solution
        placesFragmentToShow = fragment;

        tryToShowPlaces();
    }

    private void showPlacesFragment(Location location) {
        if (placesFragmentToShow == null) {
            return;
        }

        Bundle args = placesFragmentToShow.getArguments();
        args.putParcelable(FragmentPlaceCategories.ARGUMENT_LOCATION, location);
        showFragment(placesFragmentToShow);
    }



    public void showPlaceDetail(long id) {
        showPlaceDetail(id, true);
    }

    public void showPlaceDetail(long id, boolean showMapButton) {
        FragmentPlaceDetail fragment = FragmentPlaceDetail.newInstance(id);

        if (!showMapButton) {
            Bundle args = fragment.getArguments();
            args.putBoolean(FragmentPlaceDetail.ARGUMENT_SHOW_MAP_BUTTON, showMapButton);
            fragment.setArguments(args);
        }

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, fragment).addToBackStack(null).commit();
    }

    public void showCatogeryOnMap(int category) {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_map);

        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        mapFragment.showCategory(category);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            FrameLayout fragmentLayout = findViewById(R.id.fragmentLayout);
            fragmentLayout.setVisibility(View.INVISIBLE);
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //return to map
            if (viewPager.getCurrentItem() != 0) {
                BottomNavigationView navigation = findViewById(R.id.navigation);
                navigation.setSelectedItemId(R.id.navigation_map);
                return;
            } else {
                BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
                FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
                boolean handled = mapFragment.backPressed();
                if (handled) {
                    return;
                }
            }
        }

        super.onBackPressed();
    }

    //todo: make this solution cleaner
    public void showPlaceOnMap(Place place) {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_map);

        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        mapFragment.selectPlace(place);
    }

    public void showCircuitOnMap(Long id) {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_map);

        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        mapFragment.showCircuit(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String languageToLoad  = getResources().getString(R.string.langENG);
        Locale locale = new Locale(languageToLoad);
        setLocale(locale);

        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);
        BreeerViewPagerAdapter adapter = new BreeerViewPagerAdapter (MainActivity.this.getSupportFragmentManager());
        adapter.addFragment(new FragmentMap(), "Map");
        adapter.addFragment(new FragmentCircuits(), "Circuits");
        adapter.addFragment(new FragmentPlaceCategories(), "Places");
        adapter.addFragment(new FragmentDb(), "Database");
        viewPager.setAdapter(adapter);
    }

    @SuppressWarnings("deprecation")
    private void setLocale(Locale locale){
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
        } else{
            configuration.locale=locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }
    }

    @SuppressLint("RestrictedApi")
    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }



    public void tryToEnableLocation() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                enableMapLocation();
                Log.i(TAG, "location permission granted");
            } else {
                //Request Location Permission
                Log.i(TAG, "requesting location permission");
                checkLocationPermission();
            }
        }
        else {
            enableMapLocation();
        }
    }

    public void tryToNavigateToActivePlace() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("route", "new");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Location location = task.getResult();
                                    navigateToActivePlace(location);

                                } else {
                                    navigateToActivePlace(null);
                                }
                            }
                        });
            } else {
                //Request Location Permission
                Log.i(TAG, "requesting location permission");
                checkLocationPermissionForNavigation();
            }
        }
        else {
            Log.i("route", "old");

            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location location = task.getResult();
                                navigateToActivePlace(location);

                            } else {
                                navigateToActivePlace(null);
                            }
                        }
                    });
        }

    }

    @TargetApi(23)
    private void checkLocationPermissionPlaces() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i(TAG, "show explanation");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION_PLACES);
                            }
                        })
                        .create()
                        .show();


            } else {
                Log.i(TAG, "explanation not needed");
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION_PLACES );
            }
        }
    }

    @TargetApi(23)
    private void checkLocationPermissionForNavigation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i(TAG, "show explanation");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION_NAVIGATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                Log.i(TAG, "explanation not needed");
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION_NAVIGATION );
            }
        }
    }

    @TargetApi(23)
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                Log.i(TAG, "show explanation");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
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

    private void enableMapLocation() {
        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        mapFragment.enableMyLocation();
    }

    private void navigateToActivePlace(Location location) {
        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        if (location == null) {
            mapFragment.navigationFail();
        } else {
            mapFragment.startNavigationToActivePlace(new LatLng(location.getLatitude(), location.getLongitude()));
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
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        enableMapLocation();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_LOCATION_NAVIGATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.getLastLocation()
                                .addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            Location location = task.getResult();
                                            navigateToActivePlace(location);

                                        } else {
                                            navigateToActivePlace(null);
                                        }
                                    }
                                });
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_LOCATION_PLACES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.getLastLocation()
                                .addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            Location location = task.getResult();
                                            showPlacesFragment(location);

                                        } else {
                                            showPlacesFragment(null);
                                        }
                                    }
                                });
                    }

                } else {

                    showPlacesFragment(null);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    private void tryToShowPlaces() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("route", "new");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Location location = task.getResult();
                                    showPlacesFragment(location);

                                } else {
                                    showPlacesFragment(null);
                                }
                            }
                        });
            } else {
                //Request Location Permission
                Log.i(TAG, "requesting location permission");
                checkLocationPermissionPlaces();
            }
        }
        else {
            Log.i("route", "old");

            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location location = task.getResult();
                                showPlacesFragment(location);

                            } else {
                                showPlacesFragment(null);
                            }
                        }
                    });
        }
    }

    public void secondLayerFragmentSelected() {
        FrameLayout fragmentLayout = findViewById(R.id.fragmentLayout);
        fragmentLayout.setVisibility(View.VISIBLE);
    }
}
