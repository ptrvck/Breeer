package com.genius.petr.breeer.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Petr on 21. 2. 2018.
 */

public class PlaceConstants {
    public static final Map<Integer, String> CATEGORY_NAMES;
    static {
        Map<Integer, String> aMap = new HashMap<>();
        aMap.put(0, "Area");
        aMap.put(1, "Misc");
        aMap.put(2, "Food");
        aMap.put(3, "Nature");
        aMap.put(4, "Coffee");
        aMap.put(5, "Beer");
        aMap.put(6, "Bar");
        aMap.put(7, "Music");
        aMap.put(8, "Art");
        CATEGORY_NAMES = Collections.unmodifiableMap(aMap);
    }
}
