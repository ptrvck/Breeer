package com.genius.petr.breeer.map;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.DatabaseRoomManager;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class GMapV2DirectionAsyncTask extends AsyncTask<String, Void, Document> {

    private final static String TAG = GMapV2DirectionAsyncTask.class.getSimpleName();
    private Handler handler;
    private LatLng  start, end;
    private String mode;
    private Context context;

    public GMapV2DirectionAsyncTask(Handler handler, LatLng start, LatLng end, String mode) {
        this.start = start;
        this.end = end;
        this.mode = mode;
        this.handler = handler;
    }

    @Override
    protected Document doInBackground(String... params) {

        String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&sensor=false&units=metric&mode=" + mode;
        Log.d("url", url);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //this is called on UI thread
                        Log.i(TAG,"Response is: "+ response.toString().substring(0, 500));
                        //updateSQLite(response, context);
                        DatabaseRoomManager.UpdateDbAsync task = new DatabaseRoomManager.UpdateDbAsync(database, response, listener);
                        task.execute();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "That didn't work");
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpClient.execute(httpPost, localContext);
            InputStream in = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getDirectionFromDirectionApiServer(String url){
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(serverRequest);
    }

    @Override
    protected void onPostExecute(Document result) {
        if (result != null) {
            Log.d(TAG, "---- GMapV2DirectionAsyncTask OK ----");
            Message message = new Message();
            message.obj = result;
            handler.dispatchMessage(message);
        } else {
            Log.d(TAG, "---- GMapV2DirectionAsyncTask ERROR ----");
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
