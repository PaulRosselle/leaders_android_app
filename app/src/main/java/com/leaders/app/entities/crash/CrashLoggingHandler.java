package com.leaders.app.entities.crash;

import android.app.ApplicationErrorReport;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.utilities.JsonUtils;

public final class CrashLoggingHandler implements Thread.UncaughtExceptionHandler {
    @Nullable
    private final Thread.UncaughtExceptionHandler defaultHandler;
    @NonNull
    private final Context appContext;

    public CrashLoggingHandler(@NonNull Context context) {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // Keep only the application context to avoid retaining a shorter-lived context.
        appContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            saveCrashLog(throwable);
        } catch (Throwable ignored) {
            // Crash logging must never interfere with the default handler.
        } finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private void saveCrashLog(@NonNull Throwable throwable) {
        ApplicationErrorReport.CrashInfo crashInfo = new ApplicationErrorReport.CrashInfo(throwable);
        CrashLog crashLog = new CrashLog(
                Build.VERSION.SDK_INT,
                appContext.getString(R.string.app_version),
                crashInfo.exceptionClassName,
                crashInfo.exceptionMessage,
                crashInfo.stackTrace
        );

        JsonUtils.saveCrashLog(appContext, crashLog);
    }
}
