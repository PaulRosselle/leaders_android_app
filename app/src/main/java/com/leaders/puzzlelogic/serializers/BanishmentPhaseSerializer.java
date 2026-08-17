package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class BanishmentPhaseSerializer implements IJsonSerializer<BanishmentPhase> {
    @NonNull
    @Override
    public BanishmentPhase getFromJson(@NonNull JSONObject jsonObject,
                                       @NonNull SerializationContext srlContext) throws JSONException {
        TransitionActionSerializer transitionActionSerializer = new TransitionActionSerializer();
        BanishmentActionSerializer banishmentActionSerializer = new BanishmentActionSerializer();

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

        TeamColor teamColor = TeamColor.valueOf(jsonObject.getString("team_color"));

        BanishmentPhase phase = new BanishmentPhase(startAction, endAction, teamColor);

        JSONArray jaActions = jsonObject.getJSONArray("actions");
        for (int i = 0; i < jaActions.length(); i++) {
            BanishmentAction action =
                    banishmentActionSerializer.getFromJson(
                            jaActions.getJSONObject(i),
                            srlContext
                    );
            phase.getActions().add(action);
        }

        return phase;
    }

    @NonNull
    @Override
    public JSONObject getAsJson(BanishmentPhase object) throws JSONException {
        TransitionActionSerializer transitionActionSerializer = new TransitionActionSerializer();
        BanishmentActionSerializer banishmentActionSerializer = new BanishmentActionSerializer();

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

        jsonObject.put("team_color", object.getTeamColor().name());

        JSONArray jaActions = new JSONArray();
        for (IGameAction action : object.getActions()) {
            jaActions.put(banishmentActionSerializer.getAsJson((BanishmentAction) action));
        }

        jsonObject.put("actions", jaActions);

        return jsonObject;
    }
}