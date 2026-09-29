package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportActionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExportRecruitmentAction extends ExportAction {
    @Override
    public ExportActionType getActionType() {
        return ExportActionType.RECRUITMENT;
    }

    @NonNull
    private final List<ExportCharacterState> characterStates;

    public ExportRecruitmentAction(@NonNull List<ExportCharacterState> characterStates) {
        this.characterStates = characterStates;
    }

    @NonNull
    public static ExportRecruitmentAction fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportRecruitmentAction(
                getCharacterStatesFrom(jsonObject.getJSONArray("character_states"))
        );
    }

    private static List<ExportCharacterState> getCharacterStatesFrom(@NonNull JSONArray jsonArray) throws JSONException {
        List<ExportCharacterState> characterStates = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            characterStates.add(ExportCharacterState.fromJson(jsonArray.getJSONObject(i)));
        }

        return characterStates;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = super.toJson();

        JSONArray jsonArray = new JSONArray();
        for (ExportCharacterState characterState : characterStates) {
            jsonArray.put(characterState.toJson());
        }
        jsonObject.put("character_states", jsonArray);

        return jsonObject;
    }
}
