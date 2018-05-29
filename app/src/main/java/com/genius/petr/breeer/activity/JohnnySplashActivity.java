package com.genius.petr.breeer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.genius.petr.breeer.R;

public class JohnnySplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);
        boolean defaultValue = false;
        boolean dbLoaded = sharedPref.getBoolean(getString(R.string.preferences_dbLoaded), defaultValue);

        Intent intent;
        if(dbLoaded) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, WelcomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
