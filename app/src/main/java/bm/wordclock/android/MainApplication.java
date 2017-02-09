package bm.wordclock.android;

import android.app.Application;

import bm.wordclock.Helper.SocketConnectionHandler;

/**
 * Created by phenze on 08.02.17.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SocketConnectionHandler.createInstance(getApplicationContext());
    }
}