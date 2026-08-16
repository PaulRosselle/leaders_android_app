package com.leaders.app.entities.crash;

import android.app.Application;

public final class LeadersApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashLoggingHandler(this));
    }
}