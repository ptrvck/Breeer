package com.genius.petr.breeer.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.genius.petr.breeer.circuits.FragmentCircuits;
import com.genius.petr.breeer.database.FragmentDb;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.map.FragmentMap;
import com.genius.petr.breeer.places.FragmentPlaceCategories;
import com.genius.petr.breeer.places.FragmentPlaceDetail;
import com.genius.petr.breeer.R;

import java.lang.reflect.Field;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BreeerViewPager viewPager;
    private static final String TAG = "mainLog";

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
                case R.id.navigation_settings:
                    viewPager.setCurrentItem(3);
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
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, FragmentPlaceDetail.newInstance(id)).addToBackStack(null).commit();
        //viewPager.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            FrameLayout fragmentLayout = findViewById(R.id.fragmentLayout);
            fragmentLayout.setVisibility(View.INVISIBLE);
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

        String languageToLoad  = getResources().getString(R.string.langENG);
        Locale locale = new Locale(languageToLoad);
        setLocale(locale);

        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
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

    public void secondLayerFragmentSelected() {
        FrameLayout fragmentLayout = findViewById(R.id.fragmentLayout);
        fragmentLayout.setVisibility(View.VISIBLE);
    }
}
