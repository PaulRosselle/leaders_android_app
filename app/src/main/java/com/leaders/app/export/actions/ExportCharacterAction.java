package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportActionType;
import com.leaders.app.export.enums.ExportCharacterActionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExportCharacterAction extends ExportAction {
    @Override
    public ExportActionType getActionType() {
        return ExportActionType.CHARACTER;
    }

    @NonNull
    public final ExportCharacterActionType characterActionType;

    @NonNull
    public final List<ExportCharacterMotion> characterMotions;

    public ExportCharacterAction(@NonNull ExportCharacterActionType characterActionType, @NonNull List<ExportCharacterMotion> characterMotions) {
        this.characterActionType = characterActionType;
        this.characterMotions = characterMotions;
    }

    @NonNull
    public static ExportCharacterAction fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportCharacterAction(
                ExportCharacterActionType.valueOf(jsonObject.getString("character_action_type")),
                getCharacterMotionsFrom(jsonObject.getJSONArray("character_motions"))
        );
    }

    private static List<ExportCharacterMotion> getCharacterMotionsFrom(@NonNull JSONArray jsonArray) throws JSONException {
        List<ExportCharacterMotion> characterStates = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            characterStates.add(ExportCharacterMotion.fromJson(jsonArray.getJSONObject(i)));
        }

        return characterStates;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = super.toJson();

        jsonObject.put("character_action_type", characterActionType.name());
        JSONArray jsonArray = new JSONArray();
        for (ExportCharacterMotion characterMotion : characterMotions) {
            jsonArray.put(characterMotion.toJson());
        }
        jsonObject.put("character_motions", jsonArray);

        return jsonObject;
    }
}
