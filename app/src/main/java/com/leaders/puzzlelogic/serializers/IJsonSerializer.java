package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public interface IJsonSerializer<T> {
    @NonNull
    T getFromJson(@NonNull JSONObject jsonObject,
                  @NonNull SerializationContext srlContext) throws JSONException;

    @NonNull
    JSONObject getAsJson(T object) throws JSONException;

    @Nullable
    default T findInJson(@NonNull JSONObject jsonObject,
                         @NonNull SerializationContext srlContext,
                         @NonNull String jsonObjectName) throws JSONException {
        return jsonObject.has(jsonObjectName) ?
                getFromJson(jsonObject.getJSONObject(jsonObjectName), srlContext) : null;
    }

    @NonNull
    default T getFromJsonName(@NonNull JSONObject jsonObject,
                              @NonNull SerializationContext srlContext,
                              @NonNull String jsonObjectName) throws JSONException {
        return getFromJson(jsonObject.getJSONObject(jsonObjectName), srlContext);
    }

}
