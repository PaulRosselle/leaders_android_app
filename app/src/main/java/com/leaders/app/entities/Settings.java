package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.AnimationSpeed;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    @NonNull
    private AnimationSpeed animationSpeed;

    private Settings(){
        animationSpeed = AnimationSpeed.Normal;
    }

    @NonNull
    public static Settings getFromJson(JSONObject joSettings) {
        Settings settings = new Settings();

        try {
            if (joSettings.has("animation_speed")) {
                settings.setAnimationSpeed(AnimationSpeed.valueOf(joSettings.getString("animation_speed")));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return settings;
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        JSONObject joSettings = new JSONObject();
        
        joSettings.put("animation_speed", animationSpeed.name());

        return joSettings;
    }

    @NonNull
    public AnimationSpeed getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(@NonNull AnimationSpeed animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
}
