package com.example.guillermo.mysunshine;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.guillermo.mysunshine.data.WeatherContract;

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final String FORECAST_SHARE_HASHTAG = " #MySunshineApp";
    private String mForecast;
    private ShareActionProvider shareActionProvider;

    //Loader id constant
    private static final int DETAIL_LOADER = 0;
    //Projection of the columns we need from the data base
    private static final String[] FORECAST_COLUMNS = {WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                                                            WeatherContract.WeatherEntry.COLUMN_DATE,
                                                            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                                                            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                                                            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP};

    // These constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;

    public DetailActivityFragment()
    {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //Inflate menu bar
        inflater.inflate(R.menu.detail_activity_fragment, menu);

        //Retrieve share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        //Get provider and hold onto it to set/change the share intent
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null)
        {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();

        if (intent == null)
        {
            //If there is no projection data in the intent there is no point in returning a loader.
            return null;
        }

        // Now we create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        return new CursorLoader(getActivity(),
                                intent.getData(),
                                FORECAST_COLUMNS,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.v(LOG_TAG, "In onLoadFinished");

        if (!data.moveToFirst())
        {
            return;
        }

        String dateStr = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String highTemp = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String lowTemp = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        mForecast = String.format("%s - %s - %s/%s", dateStr, weatherDescription, highTemp, lowTemp);

        TextView detailTxtView = (TextView) getView().findViewById(R.id.detail_txtView);
        detailTxtView.setText(mForecast);

        if (shareActionProvider != null)
        {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
