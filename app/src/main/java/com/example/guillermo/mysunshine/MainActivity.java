package com.example.guillermo.mysunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.guillermo.mysunshine.sync.MySunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback
{
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null)
        {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.

            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            if (savedInstanceState == null)
            {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else
        {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
        MySunshineSyncAdapter.initializeSyncAdapter(this);

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

        // update the location in our second pane using the fragment manager

        if (location != null && !location.equals(mLocation))
        {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

            if (null != forecastFragment)
            {
                forecastFragment.onLocationChanged();
            }

            DetailActivityFragment detailActivityFragment = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);

            if (null != detailActivityFragment)
            {
                detailActivityFragment.onLocationChanged(location);
            }

            mLocation = location;
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

    @Override
    public void onItemSelected(Uri contentUri)
    {
        if (mTwoPane)
        {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                                        .commit();
        }
        else
        {
            Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);
            startActivity(intent);
        }

    }
}
