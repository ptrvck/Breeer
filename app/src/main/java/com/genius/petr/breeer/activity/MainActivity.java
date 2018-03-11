package com.genius.petr.breeer.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.genius.petr.breeer.database.FragmentDb;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.map.FragmentMap;
import com.genius.petr.breeer.places.FragmentPlaceCategories;
import com.genius.petr.breeer.places.FragmentPlaceDetail;
import com.genius.petr.breeer.places.FragmentPlaces;
import com.genius.petr.breeer.R;
import com.google.android.gms.maps.MapFragment;

public class MainActivity extends AppCompatActivity {

    private BreeerViewPager viewPager;
    private static final String TAG = "mainLog";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        }
    };

    public void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, fragment).addToBackStack(null).commit();
    }

    public void showPlaceDetail(long id) {
        Log.i(TAG, "showing place: " + id);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, FragmentPlaceDetail.newInstance(id)).addToBackStack(null).commit();
        //viewPager.setVisibility(View.GONE);
    }

    //todo: make this solution cleaner
    public void showPlaceOnMap(Place place) {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);

        BreeerViewPagerAdapter adapter = (BreeerViewPagerAdapter)viewPager.getAdapter();
        FragmentMap mapFragment = (FragmentMap)adapter.getFragment(0);
        mapFragment.selectPlace(place);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPagingEnabled(false);
        BreeerViewPagerAdapter adapter = new BreeerViewPagerAdapter (MainActivity.this.getSupportFragmentManager());
        adapter.addFragment(new FragmentMap(), "Map");
        adapter.addFragment(new FragmentPlaceCategories(), "Places");
        adapter.addFragment(new FragmentDb(), "Database");
        viewPager.setAdapter(adapter);
    }
}
