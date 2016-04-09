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
import android.widget.ImageView;
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
                                                            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                                                            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                                                            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                                                            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                                                            WeatherContract.WeatherEntry.COLUMN_DEGREES,
                                                            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                                                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING};

    // These constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mDetail_icon;
    private TextView mDetail_day_txtView;
    private TextView mDetail_date_txtView;
    private TextView mDetail_forecast_txtView;
    private TextView mDetail_high_txtView;
    private TextView mDetail_low_txtView;
    private TextView mDetail_humidity_txtView;
    private TextView mDetail_pressure_txtView;
    private TextView mDetail_wind_txtView;

    public DetailActivityFragment()
    {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mDetail_icon = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDetail_day_txtView = (TextView) rootView.findViewById(R.id.detail_day_txtView);
        mDetail_date_txtView = (TextView) rootView.findViewById(R.id.detail_date_txtView);
        mDetail_forecast_txtView = (TextView) rootView.findViewById(R.id.detail_forecast_txtView);
        mDetail_high_txtView = (TextView) rootView.findViewById(R.id.detail_high_txtView);
        mDetail_low_txtView = (TextView) rootView.findViewById(R.id.detail_low_txtView);
        mDetail_humidity_txtView = (TextView) rootView.findViewById(R.id.detail_humidity_txtView);
        mDetail_pressure_txtView = (TextView) rootView.findViewById(R.id.detail_pressure_txtView);
        mDetail_wind_txtView = (TextView) rootView.findViewById(R.id.detail_wind_txtView);
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

        if (data != null && data.moveToFirst())
        {
            //Read weather condition ID from cursor
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            //Use image
            mDetail_icon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            //Read date from cursor and update views for day of the week and date
            long date = data.getLong(COL_WEATHER_DATE);
            String dayTxt = Utility.getDayName(getActivity(), date);
            String dateTxt = Utility.getFormattedMonthDay(getActivity(), date);
            mDetail_day_txtView.setText(dayTxt);
            mDetail_date_txtView.setText(dateTxt);

            // Read description from cursor and update view
            String forecastTxt = data.getString(COL_WEATHER_DESC);
            mDetail_forecast_txtView.setText(forecastTxt);

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highTempTxt = Utility.formatTemperature(getActivity(), high, isMetric);
            mDetail_high_txtView.setText(highTempTxt);

            // Read low temperature from cursor and update view
            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowTempTxt = Utility.formatTemperature(getActivity(), low, isMetric);
            mDetail_low_txtView.setText(lowTempTxt);

            // Read humidity from cursor and update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            mDetail_humidity_txtView.setText(getActivity().getString(R.string.format_humidity, humidity));

            // Read wind speed and direction from cursor and update view
            float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirection = data.getFloat(COL_WEATHER_DEGREES);
            mDetail_wind_txtView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));

            // Read pressure from cursor and update view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            mDetail_pressure_txtView.setText(getActivity().getString(R.string.format_pressure, pressure));

            mForecast = String.format("%s - %s - %s/%s", dateTxt, forecastTxt, high, low);

            if (shareActionProvider != null)
            {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
