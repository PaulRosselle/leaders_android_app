package com.leaders.app.entities;

import android.app.Application;

import com.leaders.app.entities.crash.CrashLoggingHandler;
import com.leaders.app.utilities.JsonUtils;

public final class LeadersApplication extends Application {
    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashLoggingHandler(this));

        settings = JsonUtils.loadSettings(this);
    }

    public Settings getSettings() {
        return settings;
    }
}