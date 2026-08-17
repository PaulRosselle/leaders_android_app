package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class RecruitmentActionSerializer implements IJsonSerializer<RecruitmentAction> {
    @NonNull
    @Override
    public RecruitmentAction getFromJson(@NonNull JSONObject jsonObject,
                                         @NonNull SerializationContext srlContext) throws JSONException {
        RecruitmentActionMotionSerializer motionSerializer =
                new RecruitmentActionMotionSerializer();

        List<RecruitmentActionMotion> motions = new ArrayList<>();

        if (jsonObject.has("motions")) {
            JSONArray jaMotions = jsonObject.getJSONArray("motions");

            for (int i = 0; i < jaMotions.length(); i++) {
                motions.add(
                        motionSerializer.getFromJson(
                                jaMotions.getJSONObject(i),
                                srlContext
                        )
                );
            }
        }

        return new RecruitmentAction(motions);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull RecruitmentAction object) throws JSONException {
        RecruitmentActionMotionSerializer motionSerializer =
                new RecruitmentActionMotionSerializer();

        JSONArray jaMotions = new JSONArray();

        for (RecruitmentActionMotion motion : object.getMotions()) {
            jaMotions.put(motionSerializer.getAsJson(motion));
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("motions", jaMotions);

        return jsonObject;
    }
}