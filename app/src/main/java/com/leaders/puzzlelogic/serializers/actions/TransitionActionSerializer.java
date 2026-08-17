package com.leaders.puzzlelogic.serializers.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;

import org.json.JSONException;
import org.json.JSONObject;

public final class TransitionActionSerializer implements IJsonSerializer<TransitionAction> {
    @NonNull
    @Override
    public TransitionAction getFromJson(@NonNull JSONObject jsonObject,
                                        @NonNull SerializationContext srlContext) throws JSONException {
        TransitionType transitionType =
                TransitionType.valueOf(jsonObject.getString("transition_type"));
        TransitionTarget transitionTarget =
                TransitionTarget.valueOf(jsonObject.getString("transition_target"));

        return new TransitionAction(transitionType, transitionTarget);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(TransitionAction object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("transition_type", object.getTransitionType().name());
        jsonObject.put("transition_target", object.getTransitionTarget().name());

        return jsonObject;
    }
}