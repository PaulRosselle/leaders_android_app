package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.RecruitmentMotionType;

import org.json.JSONException;
import org.json.JSONObject;

public final class RecruitmentActionMotionSerializer implements IJsonSerializer<RecruitmentActionMotion> {
    @NonNull
    @Override
    public RecruitmentActionMotion getFromJson(@NonNull JSONObject jsonObject,
                                               @NonNull SerializationContext srlContext) throws JSONException {
        CharacterSerializer characterSerializer = new CharacterSerializer();
        PositionSerializer positionSerializer = new PositionSerializer();

        RecruitmentMotionType motionType =
                RecruitmentMotionType.valueOf(jsonObject.getString("motion_type"));
        Character character =
                characterSerializer.getFromJsonName(jsonObject, srlContext, "character");
        Position position =
                positionSerializer.getFromJsonName(jsonObject, srlContext, "position");

        return new RecruitmentActionMotion(motionType, character, position);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(RecruitmentActionMotion object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        CharacterSerializer characterSerializer = new CharacterSerializer();
        PositionSerializer positionSerializer = new PositionSerializer();

        jsonObject.put("motion_type", object.getMotionType().name());
        jsonObject.put("character", characterSerializer.getAsJson(object.getCharacter()));
        jsonObject.put("position", positionSerializer.getAsJson(object.getPosition()));

        return jsonObject;
    }
}