package com.dialserver;

import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by infotel5 on 10/8/15.
 */
public class DialServerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger
                .init("DialServerApp")               // default PRETTYLOGGER or use just init()
                .setMethodCount(2)            // default 2
                .hideThreadInfo()             // default shown
                .setLogLevel(LogLevel.FULL);  // default LogLevel.FULL
    }
}
