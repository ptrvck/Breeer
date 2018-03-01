package com.genius.petr.breeer.database;

/**
 * Created by Petr on 11. 2. 2018.
 */

public class JsonConstants {
    public static final String SUCCESS = "success";

    public static final String PLACES = "markers";
    public static final String CIRCUITS = "circuits";
    public static final String CIRCUIT_NODES = "circuit_nodes";
    public static final String CIRCUIT_STOPS = "circuit_stops";

    public static final String PLACE_ID = "id";
    public static final String PLACE_NAME = "name";
    public static final String PLACE_TYPE = "type";
    public static final String PLACE_LAT = "lat";
    public static final String PLACE_LNG = "lng";
    public static final String PLACE_DESCRIPTION = "description";
    public static final String PLACE_PHONE = "phone";
    public static final String PLACE_WEB = "web";
    public static final String PLACE_ADDRESS = "address";
    public static final String PLACE_OPENING_HOURS = "opening_hours";

    public static final String CIRCUIT_ID = "id";
    public static final String CIRCUIT_NAME = "name";
    public static final String CIRCUIT_DESCRIPTION = "description";
    public static final String CIRCUIT_TYPE = "type";

    public static final String CIRCUIT_NODE_ID = "id";
    public static final String CIRCUIT_NODE_CIRCUIT_ID = "circuit_id";
    public static final String CIRCUIT_NODE_LAT = "lat";
    public static final String CIRCUIT_NODE_LNG = "lng";
    public static final String CIRCUIT_NODE_NUMBER = "number";

    public static final String CIRCUIT_STOP_ID = "id";
    public static final String CIRCUIT_STOP_CIRCUIT_ID = "circuit_id";
    public static final String CIRCUIT_STOP_PLACE_ID = "marker_id";
    public static final String CIRCUIT_STOP_NUMBER = "position";
}
