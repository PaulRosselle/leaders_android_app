package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import com.leaders.app.export.entities.ExportCharacter;
import com.leaders.app.export.entities.ExportPosition;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONException;
import org.json.JSONObject;

public class ExportCharacterState {
    @NonNull
    private final ExportCharacter character;
    @NonNull
    private final ExportPosition position;

    public ExportCharacterState(@NonNull ExportCharacter character, @NonNull ExportPosition position) {
        this.character = character;
        this.position = position;
    }

    @NonNull
    public CharacterType getCharacterType() {
        return character.getCharacterType();
    }

    @NonNull
    public TeamColor getTeamColor() {
        return character.getTeamColor();
    }

    @NonNull
    public Position getPosition() {
        return position.getPosition();
    }

    @NonNull
    public static ExportCharacterState fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportCharacterState(
                ExportCharacter.fromJson(jsonObject.getJSONObject("character")),
                ExportPosition.fromJsonString(jsonObject.getString("position"))
        );
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("character", character.toJson());
        jsonObject.put("position", position.toJsonString());

        return jsonObject;
    }
}
