package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.HighlightColor;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    @NonNull
    private String userName;
    @NonNull
    private AnimationSpeed animationSpeed;
    @NonNull
    private HighlightColor highlightColor;
    private boolean animatePlayableItems;
    private CharacterSkins characterSkins;

    private Settings(){
        userName = "";
        animationSpeed = AnimationSpeed.Normal;
        highlightColor = HighlightColor.Default;
        animatePlayableItems = true;
        characterSkins = new CharacterSkins();
    }

    @NonNull
    public static Settings getFromJson(JSONObject joSettings) {
        Settings settings = new Settings();

        try {
            if (joSettings.has("user_name")) {
                settings.setUserName(joSettings.getString("user_name"));
            }
            if (joSettings.has("animation_speed")) {
                settings.setAnimationSpeed(AnimationSpeed.valueOf(joSettings.getString("animation_speed")));
            }
            if (joSettings.has("highlight_color")) {
                settings.setHighlightColor(HighlightColor.valueOf(joSettings.getString("highlight_color")));
            }
            if (joSettings.has("animate_playable_items")) {
                settings.setAnimatePlayableItems(joSettings.getBoolean("animate_playable_items"));
            }
            if (joSettings.has("character_skins")) {
                settings.characterSkins = CharacterSkins.fromJson(joSettings.getJSONObject("character_skins"));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return settings;
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        JSONObject joSettings = new JSONObject();

        joSettings.put("user_name", getUserName());
        joSettings.put("animation_speed", getAnimationSpeed().name());
        joSettings.put("highlight_color", getHighlightColor().name());
        joSettings.put("animate_playable_items", animatePlayableItems());
        joSettings.put("character_skins", characterSkins.toJson());

        return joSettings;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public void setUserName(@NonNull String userName) {
        this.userName = userName;
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

    public boolean animatePlayableItems() {
        return animatePlayableItems;
    }

    public void setAnimatePlayableItems(boolean animatePlayableItems) {
        this.animatePlayableItems = animatePlayableItems;
    }

    @NonNull
    public CharacterSkins getCharacterSkins() {
        return characterSkins;
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull CharacterCard card) {
        return characterSkins.getCharacterSkin(card);
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull Character character) {
        return characterSkins.getCharacterSkin(character.getCharacterType());
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull CharacterType characterType) {
        return characterSkins.getCharacterSkin(characterType.getCharacterCard());
    }

    public void setCharacterSkin(@NonNull CharacterCard card, @NonNull CharacterSkinType skinType) {
        characterSkins.setCharacterSkin(card, skinType);
    }
}
