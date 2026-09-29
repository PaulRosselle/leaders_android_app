package com.leaders.app.export.entities;

import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportTeamType;
import com.leaders.gamelogic.entities.Player;

import org.json.JSONException;
import org.json.JSONObject;

public class ExportPlayer {
    @NonNull
    private final String name;
    @NonNull
    private final ExportTeamType teamType;

    public ExportPlayer(@NonNull String name, @NonNull ExportTeamType teamType) {
        this.name = name;
        this.teamType = teamType;
    }

    public ExportPlayer(@NonNull Player player) {
        this(player.getName(), ExportTeamType.getFromTeamColor(player.getTeamColor()));
    }

    @NonNull
    public Player getPlayer() {
        return new Player(teamType.getAsTeamColor(), name);
    }

    @NonNull
    public static ExportPlayer fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportPlayer(
                jsonObject.getString("name"),
                ExportTeamType.valueOf(jsonObject.getString("team_type"))
        );
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", name);
        jsonObject.put("team_type", teamType.name());

        return jsonObject;
    }
}
