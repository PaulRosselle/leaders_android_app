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
    private boolean duelScreenAutoRotation;
    private CharacterSkins characterSkins;
    private boolean isFirstLaunch;

    public Settings(){
        userName = "";
        animationSpeed = AnimationSpeed.Normal;
        highlightColor = HighlightColor.Default;
        animatePlayableItems = true;
        duelScreenAutoRotation = false;
        isFirstLaunch = true;
        characterSkins = new CharacterSkins();
    }

    @NonNull
    public static Settings getFromJson(@NonNull JSONObject joSettings) {
        Settings settings = new Settings();

        settings.userName = readString(joSettings, "user_name", settings.userName);
        settings.animationSpeed = readEnum(joSettings, "animation_speed", AnimationSpeed.class, settings.animationSpeed);
        settings.highlightColor = readEnum(joSettings, "highlight_color", HighlightColor.class, settings.highlightColor);
        settings.animatePlayableItems = readBoolean(joSettings, "animate_playable_items", settings.animatePlayableItems);
        settings.duelScreenAutoRotation = readBoolean(joSettings, "duel_screen_auto_rotation", settings.duelScreenAutoRotation);
        settings.isFirstLaunch = readBoolean(joSettings, "is_first_launch", settings.isFirstLaunch);

        Object skins = joSettings.opt("character_skins");
        if (skins instanceof JSONObject) {
            try {
                settings.characterSkins = CharacterSkins.fromJson((JSONObject) skins);
            } catch (Exception e) {
                // Skins invalides : on garde les skins par défaut
            }
        }

        return settings;
    }

    @NonNull
    private static String readString(@NonNull JSONObject jsonObject,
                                     @NonNull String key,
                                     @NonNull String defaultValue) {
        Object value = jsonObject.opt(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private static boolean readBoolean(@NonNull JSONObject jsonObject,
                                       @NonNull String key,
                                       boolean defaultValue) {
        Object value = jsonObject.opt(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    @NonNull
    private static <E extends Enum<E>> E readEnum(@NonNull JSONObject jo,
                                                  @NonNull String key,
                                                  @NonNull Class<E> enumClass,
                                                  @NonNull E fallback) {
        Object value = jo.opt(key);
        if (!(value instanceof String)) {
            return fallback;
        }

        try {
            return Enum.valueOf(enumClass, (String) value);
        } catch (IllegalArgumentException e) {
            return fallback; // valeur supprimée ou renommée dans une version ultérieure
        }
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        JSONObject joSettings = new JSONObject();

        joSettings.put("user_name", getUserName());
        joSettings.put("animation_speed", getAnimationSpeed().name());
        joSettings.put("highlight_color", getHighlightColor().name());
        joSettings.put("animate_playable_items", animatePlayableItems());
        joSettings.put("duel_screen_auto_rotation", duelScreenAutoRotation());
        joSettings.put("is_first_launch", isFirstLaunch());
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

    public boolean duelScreenAutoRotation() {
        return duelScreenAutoRotation;
    }

    public void setDuelScreenAutoRotation(boolean duelScreenAutoRotation) {
        this.duelScreenAutoRotation = duelScreenAutoRotation;
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    public void setIsFirstLaunch(boolean isFirstLaunch) {
        this.isFirstLaunch = isFirstLaunch;
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
