package com.genius.petr.breeer.circuits;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.genius.petr.breeer.R;

/**
 * Created by Petr on 2. 4. 2018.
 */

public class CircuitsPagerTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {
        View image = view.findViewById(R.id.backgroundImage);

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
            image.setScaleX(0f);
        } else if (position < 1) { // [-1,1]
            view.setAlpha(1);
            image.setTranslationX(view.getWidth() * -position);

            float factor = Math.max(0, 1.0F - 2*Math.abs(position));
            image.setScaleX(factor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
            image.setScaleX(0f);
        }
    }
}
