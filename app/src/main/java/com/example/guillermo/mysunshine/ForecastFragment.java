package com.example.guillermo.mysunshine;

import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("Caracas,ve");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //String array of dummy content for listView
        String[] forecastArray = {"Today - Sunny - 88/63",
                                  "Tomorrow - Foggy - 70/40",
                                  "Wednesday - Meatballs - 80/56",
                                  "Thursday - Thunderstorm - 85/60",
                                  "Friday - Cloudy - 72/63",
                                  "Saturday - Sunny - 85/60",
                                  "Sunday - Hail - 88/63"};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        //Array adapter constructor
        forecastArrayAdapter = new ArrayAdapter<String>(getActivity(),                   //current context
                                                        R.layout.list_item_forecast,     //id of list item layout
                                                        R.id.txtView_list_item_forecast, //id of the textView to populate
                                                        weekForecast);                   //forecast data

        View rootView = inflater.inflate(R.layout.content_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(forecastArrayAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params)
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
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";
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

                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJSONStr);
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
            return null;
        }
    }
}
