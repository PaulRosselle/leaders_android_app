package com.leaders.puzzlelogic.serializers.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.CharacterSerializer;
import com.leaders.puzzlelogic.serializers.entities.PositionSerializer;

import org.json.JSONException;
import org.json.JSONObject;

public final class CharacterActionTargetSerializer implements IJsonSerializer<CharacterActionTarget> {
    @NonNull
    @Override
    public CharacterActionTarget getFromJson(@NonNull JSONObject jsonObject,
                                             @NonNull SerializationContext srlContext) throws JSONException {
        CharacterSerializer characterSerializer = new CharacterSerializer();
        PositionSerializer positionSerializer = new PositionSerializer();

        Character character = characterSerializer.getFromJsonName(jsonObject, srlContext, "character");
        Position originPos = positionSerializer.findInJson(jsonObject, srlContext, "origin_pos");
        Position destPos = positionSerializer.findInJson(jsonObject, srlContext, "dest_pos");
        return new CharacterActionTarget(character, originPos, destPos);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(CharacterActionTarget object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        CharacterSerializer characterSerializer = new CharacterSerializer();
        PositionSerializer positionSerializer = new PositionSerializer();

        jsonObject.put("character", characterSerializer.getAsJson(object.getCharacter()));
        if (object.getOriginPos() != null) {
            jsonObject.put("origin_pos", positionSerializer.getAsJson(object.getOriginPos()));
        }
        if (object.getDestPos() != null) {
            jsonObject.put("dest_pos", positionSerializer.getAsJson(object.getDestPos()));
        }

        return jsonObject;
    }
}
