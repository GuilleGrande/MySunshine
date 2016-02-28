package com.example.guillermo.mysunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Guillermo on 11-Feb-16.
 */
public class ForecastFragment extends Fragment
{
    private ArrayAdapter<String> forecastArrayAdapter;

    public ForecastFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Array adapter constructor
        forecastArrayAdapter = new ArrayAdapter<String>(getActivity(),                   //current context
                                                        R.layout.list_item_forecast,     //id of list item layout
                                                        R.id.txtView_list_item_forecast, //id of the textView to populate
                                                        new ArrayList<String>());        //forecast data

        View rootView = inflater.inflate(R.layout.content_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(forecastArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String intentStrForecast = forecastArrayAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, intentStrForecast);
                startActivity(intent);
                Toast.makeText(getActivity(), intentStrForecast, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time)
        {
            // Because the API returns a unix timestamp (measured in seconds), it must be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        //Prepare weather high/lows for presentation.
        private String formatHighLows(double high, double low)
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPreferences.getString
                                                (getString(R.string.pref_units_key)
                                                ,getString(R.string.label_pref_units_metric));

            //Converts units depending on user preference
            if (unitType.equals(getString(R.string.pref_units_imperial)))
            {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            }
            else  if (!unitType.equals(getString(R.string.pref_units_metric)))
            {
                Log.d(LOG_TAG, "Unit type not found: " + unitType);
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        //Take the String representing the complete forecast in JSON Format and pull out the data we need.
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException
        {
            //JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "main";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            //Get local current date and time
            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());

            //Declare String array to store results
            // Each position of the array represent a day forecast
            String[] resultStrs = new String[numDays];

            for (int i = 0; i < weatherArray.length(); i++)
            {
                //For now the format will be: day - description - high/low
                String day;
                String description;
                String highAndLow;

                //Get JSON object for day i
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //Get day i
                long dateTime = calendar.getTimeInMillis();
                day = getReadableDateString(dateTime);
                calendar.add(Calendar.DATE, 1);

                //Get description for day i
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                //Get max and min temperature for day i
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                //Build String to display high and low temperature
                highAndLow = formatHighLows(high, low);

                //Build String result with previously said format
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params)
        {
            //Verify that there's params to use
            if (params.length == 0)
            {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Will contain JSON response
            String forecastJSONStr = null;

            //Parameters for the OpenWeatherMap search query
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try
            {
                //Declare the URL parameters for the OpenWeatherMap query
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                //Construct the Uri call
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                //Build final URL
                URL url = new URL(builtUri.toString());

                //Log any errors
                Log.v(LOG_TAG, "Built URI: " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                forecastJSONStr = buffer.toString();
            }
            catch (IOException e)
            {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in parsing.
                return null;
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null )
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException e)
                    {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            try
            {
                return getWeatherDataFromJson(forecastJSONStr, numDays);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result)
        {
            if (result != null)
            {
                forecastArrayAdapter.clear();
                for (String dayForecastStr : result)
                {
                    forecastArrayAdapter.addAll(dayForecastStr);
                }
            }
        }
    }

    /**
     * Method used to update weather date by calling the FetchWeatherTask.class
     */
    public void updateWeather()
    {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preferences.getString(getString(R.string.pref_loaction_key), getString(R.string.pref_loaction_default));
        weatherTask.execute(location);
    }
}
