package com.genius.petr.breeer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.DatabaseRoomManager;
import com.genius.petr.breeer.database.DatabaseUpdateListener;
import com.genius.petr.breeer.database.NetworkStateReceiver;


public class WelcomeActivity extends AppCompatActivity implements DatabaseUpdateListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private static final String TAG = "welcomeActivity";
    private NetworkStateReceiver networkStateReceiver;

    private Button loadButton;
    private TextView noInternetNote;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        loadButton = findViewById(R.id.button_load);
        noInternetNote = findViewById(R.id.tv_no_internet);
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorLightGray), PorterDuff.Mode.SRC_IN );



        loadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatabaseRoomManager db = new DatabaseRoomManager(WelcomeActivity.this);
                db.updateDatabase(WelcomeActivity.this, WelcomeActivity.this);
                loadButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
/*
        if (!haveNetworkConnection()) {
            noInternetNote.setVisibility(View.VISIBLE);
            loadButton.setEnabled(false);
        }
*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    /*
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean hasInternetAccess(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }
*/
    @Override
    public void onDatabaseUpdateFinished(Boolean success) {
        if (success) {
            Intent intent = new Intent(this, MainActivity.class);
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.preferences_dbLoaded), true);
            editor.commit();
            startActivity(intent);
            finish();
        } else {
            loadButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Connection to database failed. Trying again might solve all your problems...", Toast.LENGTH_LONG).show();
            Log.i(TAG, "db update failed");
        }
    }

    @Override
    public void networkAvailable() {
        noInternetNote.setVisibility(View.GONE);
        loadButton.setEnabled(true);
    }

    @Override
    public void networkUnavailable() {
        noInternetNote.setVisibility(View.VISIBLE);
        loadButton.setEnabled(false);
    }
}
