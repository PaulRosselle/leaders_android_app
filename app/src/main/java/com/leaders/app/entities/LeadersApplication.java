package com.leaders.app.entities;

import android.app.Application;

import com.leaders.app.entities.crash.CrashLoggingHandler;

public final class LeadersApplication extends Application {
    private static Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashLoggingHandler(this));


        settings = Settings.loadFromJson(this);
    }

    public static Settings getSettings() {
        return settings;
    }
}