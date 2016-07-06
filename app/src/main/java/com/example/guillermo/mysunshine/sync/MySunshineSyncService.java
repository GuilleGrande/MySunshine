package com.example.guillermo.mysunshine.sync;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Guillermo on 06-Jul-16.
 */
public class MySunshineSyncService extends Service
{
    private static final Object mMySyncAdapterLock = new Object();
    private static MySunshineSyncAdapter mMySunshineSyncAdapter = null;

    @Override
    public void onCreate()
    {
        Log.d("MySunshineSyncService", "onCreate - MySunshineSyncService");

        synchronized (mMySyncAdapterLock)
        {
            if (mMySunshineSyncAdapter == null)
            {
                mMySunshineSyncAdapter = new MySunshineSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMySunshineSyncAdapter.getSyncAdapterBinder();
    }
}
