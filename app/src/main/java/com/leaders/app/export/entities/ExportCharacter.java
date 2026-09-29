package com.leaders.app.export.entities;

import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportCharacterType;
import com.leaders.app.export.enums.ExportTeamType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONException;
import org.json.JSONObject;

public class ExportCharacter {
    @NonNull
    private final ExportCharacterType characterType;
    @NonNull
    private final ExportTeamType teamType;

    public ExportCharacter(@NonNull Character character) {
        this(
                ExportCharacterType.getFromCharacterType(character.getCharacterType()),
                ExportTeamType.getFromTeamColor(character.getTeamColor())
        );
    }

    public ExportCharacter(@NonNull ExportCharacterType characterType, @NonNull ExportTeamType teamType) {
        this.characterType = characterType;
        this.teamType = teamType;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamType.getAsTeamColor();
    }

    @NonNull
    public CharacterType getCharacterType() {
        return characterType.getAsCharacterType();
    }

    @NonNull
    public static ExportCharacter fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportCharacter(
                ExportCharacterType.valueOf(jsonObject.getString("character_type")),
                ExportTeamType.valueOf(jsonObject.getString("team_type"))
        );
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("character_type", characterType.name());
        jsonObject.put("team_type", teamType.name());

        return jsonObject;
    }
}
