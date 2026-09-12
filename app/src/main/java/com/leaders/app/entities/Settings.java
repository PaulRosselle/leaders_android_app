package com.leaders.app.entities;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    private Settings(){
        // TODO
    }

    @NonNull
    public static Settings getFromJson(JSONObject joSettings) {
        return new Settings(); // TODO
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        return new JSONObject();
    }
}
