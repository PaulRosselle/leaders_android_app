package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public interface IJsonSerializer<T> {
    T getFromJson(@NonNull JSONObject jsonObject) throws JSONException;

    JSONObject getAsJson(T object) throws JSONException;
}
