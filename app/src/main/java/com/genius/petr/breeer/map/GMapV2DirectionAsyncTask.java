package com.genius.petr.breeer.map;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GMapV2DirectionAsyncTask extends AsyncTask<String, Void, Document> {

    private final static String TAG = GMapV2DirectionAsyncTask.class.getSimpleName();
    private Handler handler;
    private LatLng start, end;
    private String mode;

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
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'Get' request to URL : " +    url+"--"+responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response : -- " + response.toString());
            String msg = response.toString();

            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(msg);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}