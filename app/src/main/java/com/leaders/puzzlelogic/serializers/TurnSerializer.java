package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;

import org.json.JSONException;
import org.json.JSONObject;

public final class TurnSerializer implements IJsonSerializer<Turn> {
    @NonNull
    @Override
    public Turn getFromJson(@NonNull JSONObject jsonObject,
                            @NonNull SerializationContext srlContext) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        TurnStartPhaseSerializer turnStartPhaseSerializer =
                new TurnStartPhaseSerializer();
        ActionsPhaseSerializer actionsPhaseSerializer =
                new ActionsPhaseSerializer();
        RecruitmentPhaseSerializer recruitmentPhaseSerializer =
                new RecruitmentPhaseSerializer();
        TurnEndPhaseSerializer turnEndPhaseSerializer =
                new TurnEndPhaseSerializer();

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

        TeamColor teamColor =
                TeamColor.valueOf(jsonObject.getString("team_color"));

        TurnStartPhase turnStartPhase =
                turnStartPhaseSerializer.getFromJsonName(
                        jsonObject,
                        srlContext,
                        "turn_start_phase"
                );

        ActionsPhase actionsPhase =
                actionsPhaseSerializer.getFromJsonName(
                        jsonObject,
                        srlContext,
                        "actions_phase"
                );

        RecruitmentPhase recruitmentPhase =
                recruitmentPhaseSerializer.getFromJsonName(
                        jsonObject,
                        srlContext,
                        "recruitment_phase"
                );

        TurnEndPhase turnEndPhase =
                turnEndPhaseSerializer.getFromJsonName(
                        jsonObject,
                        srlContext,
                        "turn_end_phase"
                );

        return new Turn(
                startAction,
                endAction,
                teamColor,
                turnStartPhase,
                actionsPhase,
                recruitmentPhase,
                turnEndPhase
        );
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull Turn object) throws JSONException {
        TransitionActionSerializer transitionActionSerializer =
                new TransitionActionSerializer();
        TurnStartPhaseSerializer turnStartPhaseSerializer =
                new TurnStartPhaseSerializer();
        ActionsPhaseSerializer actionsPhaseSerializer =
                new ActionsPhaseSerializer();
        RecruitmentPhaseSerializer recruitmentPhaseSerializer =
                new RecruitmentPhaseSerializer();
        TurnEndPhaseSerializer turnEndPhaseSerializer =
                new TurnEndPhaseSerializer();

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

        TurnPhase[] subPhases = object.getSubPhasesInOrder();

        jsonObject.put(
                "turn_start_phase",
                turnStartPhaseSerializer.getAsJson((TurnStartPhase) subPhases[0])
        );

        jsonObject.put(
                "actions_phase",
                actionsPhaseSerializer.getAsJson((ActionsPhase) subPhases[1])
        );

        jsonObject.put(
                "recruitment_phase",
                recruitmentPhaseSerializer.getAsJson((RecruitmentPhase) subPhases[2])
        );

        jsonObject.put(
                "turn_end_phase",
                turnEndPhaseSerializer.getAsJson((TurnEndPhase) subPhases[3])
        );

        return jsonObject;
    }
}