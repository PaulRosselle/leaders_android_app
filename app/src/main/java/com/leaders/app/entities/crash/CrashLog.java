package com.leaders.app.entities.crash;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class CrashLog {
    private final int sdkLevel;
    @NonNull
    private final String appVersion;
    @NonNull
    private final String exceptionName;
    @NonNull
    private final String exceptionMessage;
    @NonNull
    private final String stackTrace;

    public CrashLog(int sdkLevel, @NonNull String appVersion, @NonNull String exceptionName,
                    @NonNull String exceptionMessage, @NonNull String stackTrace) {
        this.sdkLevel = sdkLevel;
        this.appVersion = appVersion;
        this.exceptionName = exceptionName;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
    }

    public CrashLog(@NonNull JSONObject joCrashLog) throws JSONException {
        this(joCrashLog.getInt("sdk_level"),
                joCrashLog.getString("app_version"),
                joCrashLog.getString("exception_name"),
                joCrashLog.getString("exception_message"),
                joCrashLog.getString("stack_trace"));
    }

    public JSONObject getAsJsonObj() throws JSONException {
        JSONObject joCrashLog = new JSONObject();
        joCrashLog.put("sdk_level", sdkLevel);
        joCrashLog.put("app_version", appVersion);
        joCrashLog.put("exception_name", exceptionName);
        joCrashLog.put("exception_message", exceptionMessage);
        joCrashLog.put("stack_trace", stackTrace);
        return joCrashLog;
    }

    public String getFullMessage() {
        return String.format("SDK LEVEL = %s\nAPP VERSION = %s\n-----\n%s\n\n%s\n-----\n\n%s",
                sdkLevel, appVersion, exceptionName, exceptionMessage, stackTrace);
    }
}
