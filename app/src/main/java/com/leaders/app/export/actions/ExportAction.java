package com.leaders.app.export.actions;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportActionType;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ExportAction {
    public abstract ExportActionType getActionType();


    @NonNull
    @CallSuper
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("action_type", getActionType());

        return jsonObject;
    }
}
