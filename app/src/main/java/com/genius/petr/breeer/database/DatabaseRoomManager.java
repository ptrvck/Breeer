package com.genius.petr.breeer.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.genius.petr.breeer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by Petr on 26. 4. 2017.
 */

public class DatabaseRoomManager {
    private AppDatabase database;
    final static String TAG = "DB_BREER";

    public DatabaseRoomManager(Context context){
        database = AppDatabase.getDatabase(context);
    }

    public void updateDatabase(final Context context, final DatabaseUpdateListener listener) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = context.getString(R.string.server_url) + context.getString(R.string.script_url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //this is called on UI thread
                        Log.i(TAG,"Response is: "+ response.toString().substring(0, 500));
                        //updateSQLite(response, context);
                        UpdateDbAsync task = new UpdateDbAsync(database, response, listener);
                        task.execute();
                                            }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        Log.e(TAG, "That didn't work");

                        if (listener != null) {
                            listener.onDatabaseUpdateFinished(false);
                        }

                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }


    private static Place addPlace(final AppDatabase db, JSONObject json) throws JSONException {

        int id = json.getInt(JsonConstants.PLACE_ID);
        String name = json.getString(JsonConstants.PLACE_NAME);
        int type = json.getInt(JsonConstants.PLACE_TYPE);
        double lat = json.getDouble(JsonConstants.PLACE_LAT);
        double lng = json.getDouble(JsonConstants.PLACE_LNG);
        String description = json.getString(JsonConstants.PLACE_DESCRIPTION);
        String phone = json.getString(JsonConstants.PLACE_PHONE);
        String web = json.getString(JsonConstants.PLACE_WEB);
        String address = json.getString(JsonConstants.PLACE_ADDRESS);

        Place place = new Place();
        place.id = id;
        place.name = name;
        place.category = type;
        place.lat = lat;
        place.lng = lng;
        place.description = description;
        place.phone = phone;
        place.web = web;
        place.address = address;

        db.place().insert(place);

        return place;
    }

    private static CircuitBase addCircuit(final AppDatabase db, JSONObject json) throws JSONException {

        int id = json.getInt(JsonConstants.CIRCUIT_NODE_ID);
        String name = json.getString(JsonConstants.CIRCUIT_NAME);
        String description = json.getString(JsonConstants.CIRCUIT_DESCRIPTION);
        int type = json.getInt(JsonConstants.CIRCUIT_TYPE);

        CircuitBase circuit = new CircuitBase();
        circuit.id = id;
        circuit.name = name;
        circuit.type = type;
        circuit.description = description;

        db.circuit().insertCircuitBase(circuit);

        return circuit;
    }

    private static CircuitNode addCircuitNode(final AppDatabase db, JSONObject json) throws JSONException {

        long id = json.getLong(JsonConstants.CIRCUIT_ID);
        long circuitId = json.getLong(JsonConstants.CIRCUIT_NODE_CIRCUIT_ID);
        double lat = json.getDouble(JsonConstants.CIRCUIT_NODE_LAT);
        double lng = json.getDouble(JsonConstants.CIRCUIT_NODE_LNG);
        int number = json.getInt(JsonConstants.CIRCUIT_NODE_NUMBER);

        CircuitNode circuitNode = new CircuitNode();
        circuitNode.id = id;
        circuitNode.circuitId = circuitId;
        circuitNode.lat = lat;
        circuitNode.lng = lng;
        circuitNode.number = number;

        db.circuit().insertNode(circuitNode);

        return circuitNode;
    }

    private static CircuitStop addCircuitStop(final AppDatabase db, JSONObject json) throws JSONException {

        long id = json.getLong(JsonConstants.CIRCUIT_STOP_ID);
        long circuitId = json.getLong(JsonConstants.CIRCUIT_STOP_CIRCUIT_ID);
        long placeId = json.getLong(JsonConstants.CIRCUIT_STOP_PLACE_ID);
        int number = json.getInt(JsonConstants.CIRCUIT_STOP_NUMBER);

        CircuitStop circuitStop = new CircuitStop();
        circuitStop.id = id;
        circuitStop.circuitId = circuitId;
        circuitStop.placeId = placeId;
        circuitStop.number = number;

        db.circuit().insertStop(circuitStop);

        return circuitStop;
    }

    private static void addPlaces(JSONArray placesJSON, final AppDatabase db) throws JSONException {

        for (int i = 0; i < placesJSON.length(); i++) {
            JSONObject json = placesJSON.getJSONObject(i);
            addPlace(db, json);
        }
    }

    private static void addCircuits(JSONArray circuitsJSON, final AppDatabase db) throws JSONException {

        for (int i = 0; i < circuitsJSON.length(); i++) {
            JSONObject json = circuitsJSON.getJSONObject(i);
            addCircuit(db, json);
        }
    }

    private static void addCircuitNodes(JSONArray nodesJSON, final AppDatabase db) throws JSONException {

        for (int i = 0; i < nodesJSON.length(); i++) {
            JSONObject json = nodesJSON.getJSONObject(i);
            addCircuitNode(db, json);
        }
    }

    private static void addCircuitStops(JSONArray stopsJSON, final AppDatabase db) throws JSONException {

        for (int i = 0; i < stopsJSON.length(); i++) {
            JSONObject json = stopsJSON.getJSONObject(i);
            addCircuitStop(db, json);
        }
    }

    private static boolean updateDb(AppDatabase db, JSONObject response) {
        try {
            int success = response.getInt(JsonConstants.SUCCESS);
            if (success != 1) {
                return false;
            }

            if (response.has(JsonConstants.PLACES)) {
                JSONArray placesJSON = response.getJSONArray(JsonConstants.PLACES);
                addPlaces(placesJSON, db);
            }
            if (response.has(JsonConstants.CIRCUITS)) {
                JSONArray circuitsJSON = response.getJSONArray(JsonConstants.CIRCUITS);
                addCircuits(circuitsJSON, db);
            }
            if (response.has(JsonConstants.CIRCUIT_NODES)) {
                JSONArray nodesJSON = response.getJSONArray(JsonConstants.CIRCUIT_NODES);
                addCircuitNodes(nodesJSON, db);
            }
            if (response.has(JsonConstants.CIRCUIT_STOPS)) {
                JSONArray stopsJSON = response.getJSONArray(JsonConstants.CIRCUIT_STOPS);
                addCircuitStops(stopsJSON, db);
            }

        } catch (JSONException e) {
            return false;
        }

        List<Place> places = db.place().selectAllSynchronous();
        Log.d(TAG, "Places Count: " + places.size());

        List<Circuit> circuits = db.circuit().selectAllSynchronous();
        Log.d(TAG, "Circuits Count: " + circuits.size());
        for (Circuit circuit : circuits) {
            //Log.d(TAG, "Circuit " + circuit.circuitBase.name + " nodes count: " + circuit.nodes.size());
            //List<Place> stops = db.circuit().getStopsOfCircuit(circuit.circuitBase.id);
            List<Integer> stops = db.circuit().getStopsIdsOfCircuit(circuit.circuitBase.id);
            Log.d(TAG, "Circuit " + circuit.circuitBase.name + " stops count: " + stops.size());
            for (int i : stops) {
                Log.d(TAG, "---id: " + i);
            }
        }

        return true;
    }

    private static class UpdateDbAsync extends AsyncTask<Void, Void, Boolean> {

        //todo: can this be final?
        private final AppDatabase mDb;
        private final JSONObject mResponse;
        private final DatabaseUpdateListener mListener;

        UpdateDbAsync(AppDatabase db, JSONObject response, DatabaseUpdateListener listener) {
            mDb = db;
            mResponse = response;
            mListener = listener;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            return updateDb(mDb, mResponse);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mListener!=null) {
                mListener.onDatabaseUpdateFinished(success);
            }
        }

    }
}
