package com.example.guillermo.mysunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.example.guillermo.mysunshine.data.WeatherContract;

/**
 * Created by Guillermo on 14-Mar-16.
 * Exposes a list of weather forecasts from
 * {@link android.database.Cursor} to {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter
{
    public ForecastAdapter(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
    }

    //Prepare the weather high/low for presentation
    private String formatHighLows(double high, double low)
    {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    private String convertCursorRowToUXFormat(Cursor cursor)
    {
        //get row indices from cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(cursor.getDouble(idx_max_temp), cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) +
                                    " - " +
                                  cursor.getString(idx_short_desc) +
                                    " - " +
                                  highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView textView = (TextView)view;
        textView.setText(convertCursorRowToUXFormat(cursor));
    }
}

