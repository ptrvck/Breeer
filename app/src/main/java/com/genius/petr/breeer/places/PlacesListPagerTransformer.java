package com.genius.petr.breeer.places;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.genius.petr.breeer.R;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class PlacesListPagerTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {
        View background = view.findViewById(R.id.background);
        View list = view.findViewById(R.id.list);

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
        } else if (position <= 1) { // [-1,1]
            background.setAlpha(1.0F - Math.abs(position));
            view.setTranslationX(view.getWidth() * -position);
            list.setTranslationX(view.getWidth()*position);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
