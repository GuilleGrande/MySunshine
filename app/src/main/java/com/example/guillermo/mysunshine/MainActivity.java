package com.example.guillermo.mysunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String currentLocation;
    private final String FORECASTFRAGMENT_TAG = "FF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentLocation = Utility.getPreferredLocation(this);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
                    .commit();
        }

        Log.d(LOG_TAG, "CREATE");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(LOG_TAG, "DESTROY");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "START");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "STOP");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "PAUSE");
    }

    @Override
    protected void onResume()
    {
        Log.d(LOG_TAG, "RESUME");
        super.onResume();

        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.equals(currentLocation))
        {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);

            if (forecastFragment != null)
            {
                forecastFragment.onLocationChanged();
            }
            currentLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map)
        {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method fetchs user's preferred location to open
     * in an external map app
     */
    public void openPreferredLocationInMap()
    {
        //Get user's preferred location and store it a String variable
        String location = Utility.getPreferredLocation(this);

        // Build URI scheme with previous String to launch an intent
        Uri geoLocation = Uri
                .parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();

        //Build intent to launch external map app
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);


        //Resolve intent data to launch app
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(intent);
        }
        else
        {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed");
        }

    }
}
