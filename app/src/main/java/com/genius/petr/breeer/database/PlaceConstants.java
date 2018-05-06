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
    public static final Map<Integer, Integer> CATEGORY_ICONS;
    public static final Map<Integer, Integer> CATEGORY_ICONS_BLACK;
    public static final Map<Integer, Integer> CATEGORY_MARKERS;
    public static final Map<Integer, Integer> CATEGORY_MARKERS_ACTIVE;
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

        Map<Integer, Integer> iconIds = new HashMap<>();
        iconIds.put(0, R.drawable.cat_area);
        iconIds.put(1, R.drawable.cat_misc);
        iconIds.put(2, R.drawable.cat_food);
        iconIds.put(3, R.drawable.cat_nature);
        iconIds.put(4, R.drawable.cat_coffee);
        iconIds.put(5, R.drawable.cat_beer);
        iconIds.put(6, R.drawable.cat_bar);
        iconIds.put(7, R.drawable.cat_music);
        iconIds.put(8, R.drawable.cat_art);
        CATEGORY_ICONS = Collections.unmodifiableMap(iconIds);

        Map<Integer, Integer> blackIconIds = new HashMap<>();
        blackIconIds.put(0, R.drawable.cat_area_d);
        blackIconIds.put(1, R.drawable.cat_misc_d);
        blackIconIds.put(2, R.drawable.cat_food_d);
        blackIconIds.put(3, R.drawable.cat_nature_d);
        blackIconIds.put(4, R.drawable.cat_coffee_d);
        blackIconIds.put(5, R.drawable.cat_beer_d);
        blackIconIds.put(6, R.drawable.cat_bar_d);
        blackIconIds.put(7, R.drawable.cat_music_d);
        blackIconIds.put(8, R.drawable.cat_art_d);
        CATEGORY_ICONS_BLACK = Collections.unmodifiableMap(blackIconIds);

        Map<Integer, Integer> markersIds = new HashMap<>();
        markersIds.put(0, R.drawable.marker_area);
        markersIds.put(1, R.drawable.marker_misc);
        markersIds.put(2, R.drawable.marker_food);
        markersIds.put(3, R.drawable.marker_nature);
        markersIds.put(4, R.drawable.marker_coffee);
        markersIds.put(5, R.drawable.marker_beer);
        markersIds.put(6, R.drawable.marker_bar);
        markersIds.put(7, R.drawable.marker_music);
        markersIds.put(8, R.drawable.marker_art);
        CATEGORY_MARKERS = Collections.unmodifiableMap(markersIds);

        Map<Integer, Integer> markersActiveIds = new HashMap<>();
        markersActiveIds.put(0, R.drawable.marker_area_active);
        markersActiveIds.put(1, R.drawable.marker_misc_active);
        markersActiveIds.put(2, R.drawable.marker_food_active);
        markersActiveIds.put(3, R.drawable.marker_nature_active);
        markersActiveIds.put(4, R.drawable.marker_coffee_active);
        markersActiveIds.put(5, R.drawable.marker_beer_active);
        markersActiveIds.put(6, R.drawable.marker_bar_active);
        markersActiveIds.put(7, R.drawable.marker_music_active);
        markersActiveIds.put(8, R.drawable.marker_art_active);
        CATEGORY_MARKERS_ACTIVE = Collections.unmodifiableMap(markersActiveIds);


    }
}
