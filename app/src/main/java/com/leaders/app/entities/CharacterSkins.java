package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

public class CharacterSkins {
    private final Map<CharacterCard, CharacterSkinType> skinTypeMap;

    public CharacterSkins() {
        skinTypeMap = new EnumMap<>(CharacterCard.class);
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull CharacterCard card) {
        CharacterSkinType characterSkin = skinTypeMap.get(card);
        return characterSkin != null ? characterSkin : CharacterSkinType.Default;
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull Character character) {
        return getCharacterSkin(character.getCharacterType());
    }

    @NonNull
    public CharacterSkinType getCharacterSkin(@NonNull CharacterType characterType) {
        return getCharacterSkin(characterType.getCharacterCard());
    }

    public void setCharacterSkin(@NonNull CharacterCard card, @NonNull CharacterSkinType skinType) {
        if (skinType == CharacterSkinType.Default) {
            skinTypeMap.remove(card);
        } else {
            skinTypeMap.put(card, skinType);
        }
    }

    public static CharacterSkins fromJson(@NonNull JSONObject joCharacterSkins) throws JSONException {
        CharacterSkins characterSkins = new CharacterSkins();

        for (CharacterCard card : CharacterCard.values()) {
            if (joCharacterSkins.has(card.name())) {
                characterSkins.skinTypeMap.put(
                        card,
                        CharacterSkinType.valueOf(joCharacterSkins.getString(card.name()))
                );
            }
        }

        return characterSkins;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject joCharacterSkins = new JSONObject();

        for (Map.Entry<CharacterCard, CharacterSkinType> entry : skinTypeMap.entrySet()) {
            joCharacterSkins.put(entry.getKey().name(), entry.getValue());
        }

        return joCharacterSkins;
    }
}
