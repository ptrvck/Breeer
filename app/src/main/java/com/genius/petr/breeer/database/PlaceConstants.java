package com.genius.petr.breeer.database;

import com.genius.petr.breeer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Petr on 21. 2. 2018.
 */

public class PlaceConstants {
    public static final Map<Integer, String> CATEGORY_NAMES;
    public static final Map<Integer, Integer> CATEGORY_COLORS;
    public static final List<Integer> CATEGORIES;
    static {
        List<Integer> categories = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            categories.add(i);
        }

        CATEGORIES = Collections.unmodifiableList(categories);

        Map<Integer, String> categoryNames = new HashMap<>();
        categoryNames.put(0, "Area");
        categoryNames.put(1, "Misc");
        categoryNames.put(2, "Food");
        categoryNames.put(3, "Nature");
        categoryNames.put(4, "Coffee");
        categoryNames.put(5, "Beer");
        categoryNames.put(6, "Bar");
        categoryNames.put(7, "Music");
        categoryNames.put(8, "Art");
        CATEGORY_NAMES = Collections.unmodifiableMap(categoryNames);

        Map<Integer, Integer> colorIds = new HashMap<>();
        colorIds.put(0, R.color.colorAreaAccent);
        colorIds.put(1, R.color.colorMiscAccent);
        colorIds.put(2, R.color.colorFoodAccent);
        colorIds.put(3, R.color.colorNatureAccent);
        colorIds.put(4, R.color.colorCoffeeAccent);
        colorIds.put(5, R.color.colorBeerAccent);
        colorIds.put(6, R.color.colorBarAccent);
        colorIds.put(7, R.color.colorMusicAccent);
        colorIds.put(8, R.color.colorArtAccent);
        CATEGORY_COLORS = Collections.unmodifiableMap(colorIds);
    }
}
