package com.leaders.puzzlelogic.serializers.historyentries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.actions.RecruitmentActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.TransitionActionSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RecruitmentPhaseSerializer implements IJsonSerializer<RecruitmentPhase> {
    @NonNull
    @Override
    public RecruitmentPhase getFromJson(@NonNull JSONObject jsonObject,
                                        @NonNull SerializationContext srlContext) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        RecruitmentActionSerializer recruitmentActionSerializer =
                new RecruitmentActionSerializer();

        TransitionAction startAction = null;
        if (jsonObject.has("start_action") && !jsonObject.isNull("start_action")) {
            startAction = transitionActionSerializer.getFromJsonName(
                    jsonObject,
                    srlContext,
                    "start_action"
            );
        }

        TransitionAction endAction = null;
        if (jsonObject.has("end_action") && !jsonObject.isNull("end_action")) {
            endAction = transitionActionSerializer.getFromJsonName(
                    jsonObject,
                    srlContext,
                    "end_action"
            );
        }

        TeamColor turnTeamColor =
                TeamColor.valueOf(jsonObject.getString("turn_team_color"));

        RecruitmentPhase phase =
                new RecruitmentPhase(startAction, endAction, turnTeamColor);

        JSONArray jaActions = jsonObject.getJSONArray("actions");
        for (int i = 0; i < jaActions.length(); i++) {
            RecruitmentAction action =
                    recruitmentActionSerializer.getFromJson(
                            jaActions.getJSONObject(i),
                            srlContext
                    );
            phase.getActions().add(action);
        }

        return phase;
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull RecruitmentPhase object) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        RecruitmentActionSerializer recruitmentActionSerializer =
                new RecruitmentActionSerializer();

        JSONObject jsonObject = new JSONObject();

        if (object.getStartAction() != null) {
            jsonObject.put(
                    "start_action",
                    transitionActionSerializer.getAsJson(object.getStartAction())
            );
        }

        if (object.getEndAction() != null) {
            jsonObject.put(
                    "end_action",
                    transitionActionSerializer.getAsJson(object.getEndAction())
            );
        }

        jsonObject.put("turn_team_color", object.getTurnTeamColor().name());

        JSONArray jaActions = new JSONArray();
        for (IGameAction action : object.getActions()) {
            jaActions.put(
                    recruitmentActionSerializer.getAsJson(
                            (RecruitmentAction) action
                    )
            );
        }

        jsonObject.put("actions", jaActions);

        return jsonObject;
    }
}