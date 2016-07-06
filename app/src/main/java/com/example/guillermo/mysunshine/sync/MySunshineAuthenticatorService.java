package com.example.guillermo.mysunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Guillermo on 06-Jul-16.
 *
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class MySunshineAuthenticatorService extends Service
{
    // Instance field to store authenticator object
    private MySunshineAuthenticator mAuthenticator;

    @Override
    public void onCreate()
    {
        // Create new authenticator object
        mAuthenticator = new MySunshineAuthenticator(this);
    }

    /**
     * When the system binds to this Service to make the RPC call return the authenticator's IBinder.
     * @param intent
     * @return IBinder
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return mAuthenticator.getIBinder();
    }
}
