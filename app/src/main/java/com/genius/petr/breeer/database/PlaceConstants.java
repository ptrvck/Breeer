package com.genius.petr.breeer.database;

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
    }
}
