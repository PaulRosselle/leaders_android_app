package com.leaders.puzzlelogic.serializers.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;

import org.json.JSONException;
import org.json.JSONObject;

public final class WarningActionSerializer implements IJsonSerializer<WarningAction> {
    @NonNull
    @Override
    public WarningAction getFromJson(@NonNull JSONObject jsonObject,
                                     @NonNull SerializationContext srlContext) throws JSONException {
        WarningType warningType = WarningType.valueOf(jsonObject.getString("warning_type"));
        TeamColor teamColor = TeamColor.valueOf(jsonObject.getString("team_color"));
        int countChange = jsonObject.getInt("count_change");

        return new WarningAction(warningType, teamColor, countChange);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(WarningAction object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("warning_type", object.getWarningType().name());
        jsonObject.put("team_color", object.getTeamColor().name());
        jsonObject.put("count_change", object.getCountChange());

        return jsonObject;
    }
}