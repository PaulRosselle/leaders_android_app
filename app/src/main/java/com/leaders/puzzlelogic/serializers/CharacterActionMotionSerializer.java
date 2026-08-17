package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.enums.CharacterMotionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class CharacterActionMotionSerializer implements IJsonSerializer<CharacterActionMotion> {
    @NonNull
    @Override
    public CharacterActionMotion getFromJson(@NonNull JSONObject jsonObject, @NonNull SerializationContext srlContext) throws JSONException {
        CharacterActionTargetSerializer characterActionTargetSerializer = new CharacterActionTargetSerializer();

        CharacterMotionType motionType = CharacterMotionType.valueOf(jsonObject.getString("motion_type"));
        List<CharacterActionTarget> targets = new ArrayList<>();
        if (jsonObject.has("targets")) {
            JSONArray jaTargets = jsonObject.getJSONArray("targets");
            for (int i = 0; i < jaTargets.length(); i++) {
                targets.add(characterActionTargetSerializer.getFromJson(jaTargets.getJSONObject(i), srlContext));
            }
        }

        return new CharacterActionMotion(motionType, targets);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(CharacterActionMotion object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        CharacterActionTargetSerializer characterActionTargetSerializer = new CharacterActionTargetSerializer();

        jsonObject.put("motion_type", object.getMotionType().name());
        if (!object.getTargets().isEmpty()) {
            JSONArray jaTargets = new JSONArray();
            for (CharacterActionTarget target : object.getTargets()) {
                jaTargets.put(characterActionTargetSerializer.getAsJson(target));
            }
            jsonObject.put("targets", jaTargets);
        }

        return jsonObject;
    }
}
