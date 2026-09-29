package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ExportCharacterChange {
    @NonNull
    private final ExportCharacterState beforeState;
    @NonNull
    private final ExportCharacterState afterState;

    public ExportCharacterChange(@NonNull ExportCharacterState beforeState, @NonNull ExportCharacterState afterState) {
        this.beforeState = beforeState;
        this.afterState = afterState;
    }

    @NonNull
    public static ExportCharacterChange fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportCharacterChange(
                ExportCharacterState.fromJson(jsonObject.getJSONObject("before_state")),
                ExportCharacterState.fromJson(jsonObject.getJSONObject("after_state"))
        );
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("before_state", beforeState.toJson());
        jsonObject.put("after_state", afterState.toJson());

        return jsonObject;
    }
}
