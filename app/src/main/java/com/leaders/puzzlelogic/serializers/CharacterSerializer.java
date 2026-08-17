package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.UUID;

public class CharacterSerializer {
    @NonNull
    public Character getFromJson(@NonNull JSONObject jsonObject,
                                 @NonNull SerializationContext serializationContext) throws JSONException {
        UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
        Character mappedCharacter = serializationContext.getCharactersMap().get(uuid);
        if (mappedCharacter != null) {
            return mappedCharacter;
        }

        Character newCharacter = Character.create(
                CharacterType.valueOf(jsonObject.getString("character_type")),
                TeamColor.valueOf(jsonObject.getString("team_color"))
        );
        serializationContext.getCharactersMap().put(uuid, newCharacter);
        return newCharacter;
    }

    public JSONObject getAsJson(@NonNull Character character) throws JSONException {
        JSONObject joCharacter = new JSONObject();
        joCharacter.put("uuid", character.getId());
        joCharacter.put("character_type", character.getCharacterType().name());
        joCharacter.put("team_color", character.getTeamColor().name());
        return joCharacter;
    }
}
