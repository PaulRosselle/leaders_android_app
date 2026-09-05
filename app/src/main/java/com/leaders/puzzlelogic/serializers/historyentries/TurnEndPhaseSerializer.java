package com.leaders.puzzlelogic.serializers.historyentries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.actions.BanishmentActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.CharacterActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.RecruitmentActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.TransitionActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.WarningActionSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class TurnEndPhaseSerializer implements IJsonSerializer<TurnEndPhase> {
    @NonNull
    @Override
    public TurnEndPhase getFromJson(@NonNull JSONObject jsonObject,
                                    @NonNull SerializationContext srlContext) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        CharacterActionSerializer characterActionSerializer =
                new CharacterActionSerializer();

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

        TurnEndPhase phase =
                new TurnEndPhase(startAction, endAction, turnTeamColor);

        JSONArray jaActions = jsonObject.getJSONArray("actions");
        for (int i = 0; i < jaActions.length(); i++) {
            JSONObject actionJson = jaActions.getJSONObject(i);
            GameActionType actionType = GameActionType.valueOf(actionJson.getString("type"));
            IGameAction action;
            switch (actionType) {
                case CharacterAction:
                    action = new CharacterActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                case Warning:
                    action = new WarningActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                default: throw new JSONException("Unsupported game action type: " + actionType);
            }
            phase.getActions().add(action);
        }

        return phase;
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull TurnEndPhase object) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        CharacterActionSerializer characterActionSerializer =
                new CharacterActionSerializer();

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
            JSONObject actionJson;
            switch (action.getActionType()) {
                case CharacterAction: actionJson = new CharacterActionSerializer().getAsJson((CharacterAction) action);
                    break;
                case Warning: actionJson = new WarningActionSerializer().getAsJson((WarningAction) action);
                    break;
                default: throw new JSONException("Unsupported game action type: " + action.getActionType());
            }
            actionJson.put("type", action.getActionType().name());
            jaActions.put(actionJson);
        }

        jsonObject.put("actions", jaActions);

        return jsonObject;
    }
}