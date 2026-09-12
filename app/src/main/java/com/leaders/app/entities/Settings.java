package com.leaders.app.entities;

import android.content.Context;

import androidx.annotation.NonNull;

public final class Settings {

    private Settings(){
        // TODO
    }

    public static Settings loadFromJson(@NonNull Context context) {
        return new Settings(); // TODO
    }

    public void saveToJson(@NonNull Context context) {
        // TODO
    }
}
