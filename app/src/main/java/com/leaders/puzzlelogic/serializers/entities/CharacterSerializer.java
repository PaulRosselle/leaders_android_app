package com.leaders.puzzlelogic.serializers.entities;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;

import java.util.UUID;

public final class CharacterSerializer implements IJsonSerializer<Character> {
    @NonNull
    public Character getFromJson(@NonNull JSONObject jsonObject,
                                 @NonNull SerializationContext srlContext) throws JSONException {
        UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
        Character mappedCharacter = srlContext.getCharactersMap().get(uuid);
        if (mappedCharacter != null) {
            return mappedCharacter;
        }

        Character newCharacter = Character.create(
                CharacterType.valueOf(jsonObject.getString("character_type")),
                TeamColor.valueOf(jsonObject.getString("team_color"))
        );
        srlContext.getCharactersMap().put(uuid, newCharacter);
        return newCharacter;
    }

    @NonNull
    public JSONObject getAsJson(@NonNull Character character) throws JSONException {
        JSONObject joCharacter = new JSONObject();
        joCharacter.put("uuid", character.getId());
        joCharacter.put("character_type", character.getCharacterType().name());
        joCharacter.put("team_color", character.getTeamColor().name());
        return joCharacter;
    }
}
