package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.HighlightColor;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    @NonNull
    private AnimationSpeed animationSpeed;
    @NonNull
    private HighlightColor highlightColor;

    private Settings(){
        animationSpeed = AnimationSpeed.Normal;
        highlightColor = HighlightColor.Default;
    }

    @NonNull
    public static Settings getFromJson(JSONObject joSettings) {
        Settings settings = new Settings();

        try {
            if (joSettings.has("animation_speed")) {
                settings.setAnimationSpeed(AnimationSpeed.valueOf(joSettings.getString("animation_speed")));
            }
            if (joSettings.has("highlight_color")) {
                settings.setHighlightColor(HighlightColor.valueOf(joSettings.getString("highlight_color")));
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
        joSettings.put("highlight_color", highlightColor.name());

        return joSettings;
    }

    @NonNull
    public AnimationSpeed getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(@NonNull AnimationSpeed animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    @NonNull
    public HighlightColor getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(@NonNull HighlightColor highlightColor) {
        this.highlightColor = highlightColor;
    }
}
