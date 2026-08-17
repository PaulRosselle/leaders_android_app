package com.leaders.app.entities;

import android.app.Application;

import com.leaders.app.entities.crash.CrashLoggingHandler;

public final class LeadersApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashLoggingHandler(this));
    }
}