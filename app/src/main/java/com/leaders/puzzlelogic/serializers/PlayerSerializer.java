package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONException;
import org.json.JSONObject;

public final class PlayerSerializer implements IJsonSerializer<Player> {
    @NonNull
    @Override
    public Player getFromJson(@NonNull JSONObject jsonObject,
                              @NonNull SerializationContext srlContext) throws JSONException {
        TeamColor teamColor = TeamColor.valueOf(jsonObject.getString("team_color"));
        String name = jsonObject.getString("name");

        return new Player(teamColor, name);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull Player object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("team_color", object.getTeamColor().name());
        jsonObject.put("name", object.getName());

        return jsonObject;
    }
}