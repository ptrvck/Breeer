package com.genius.petr.breeer.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.genius.petr.breeer.database.FragmentDb;
import com.genius.petr.breeer.map.FragmentMap;
import com.genius.petr.breeer.places.FragmentPlaceDetail;
import com.genius.petr.breeer.places.FragmentPlaces;
import com.genius.petr.breeer.R;

public class MainActivity extends AppCompatActivity {

    private BreeerViewPager viewPager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
/*
    private void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragmentLayout, fragment).commit();
    }
*/
    public void showPlaceDetail(long id) {
        //FragmentManager manager = getSupportFragmentManager();
        //manager.beginTransaction().replace(R.id.fragmentLayout, FragmentPlaceDetail.newInstance(id)).addToBackStack(null).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPagingEnabled(false);
        BreeerViewPagerAdapter adapter = new BreeerViewPagerAdapter (MainActivity.this.getSupportFragmentManager());
        adapter.addFragment(new FragmentMap(), "Map");
        adapter.addFragment(new FragmentPlaces(), "Places");
        adapter.addFragment(new FragmentDb(), "Database");
        viewPager.setAdapter(adapter);
    }
}
