package com.leaders.puzzlelogic.serializers.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.CharacterSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CharacterActionSerializer implements IJsonSerializer<CharacterAction> {
    @NonNull
    @Override
    public CharacterAction getFromJson(@NonNull JSONObject jsonObject,
                                       @NonNull SerializationContext srlContext) throws JSONException {
        CharacterSerializer characterSerializer = new CharacterSerializer();
        CharacterActionMotionSerializer characterActionMotionSerializer = new CharacterActionMotionSerializer();

        Character srcCharacter = characterSerializer.getFromJsonName(
                jsonObject,
                srlContext,
                "src_character"
        );

        List<CharacterActionMotion> motions = new ArrayList<>();
        if (jsonObject.has("motions")) {
            JSONArray jaMotions = jsonObject.getJSONArray("motions");
            for (int i = 0; i < jaMotions.length(); i++) {
                motions.add(
                        characterActionMotionSerializer.getFromJson(
                                jaMotions.getJSONObject(i),
                                srlContext
                        )
                );
            }
        }

        return new CharacterAction(srcCharacter, motions);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(CharacterAction object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        CharacterSerializer characterSerializer = new CharacterSerializer();
        CharacterActionMotionSerializer characterActionMotionSerializer =
                new CharacterActionMotionSerializer();

        jsonObject.put(
                "src_character",
                characterSerializer.getAsJson(object.getSrcCharacter())
        );

        if (!object.getMotions().isEmpty()) {
            JSONArray jaMotions = new JSONArray();
            for (CharacterActionMotion motion : object.getMotions()) {
                jaMotions.put(characterActionMotionSerializer.getAsJson(motion));
            }
            jsonObject.put("motions", jaMotions);
        }

        return jsonObject;
    }
}