package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExportCharacterMotion {
    @NonNull
    private final List<ExportCharacterChange> characterChanges;

    public ExportCharacterMotion(@NonNull List<ExportCharacterChange> characterChanges) {
        this.characterChanges = characterChanges;
    }

    @NonNull
    public static ExportCharacterMotion fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportCharacterMotion(
                getCharacterChangesFrom(jsonObject.getJSONArray("character_changes"))
        );
    }

    @NonNull
    private static List<ExportCharacterChange> getCharacterChangesFrom(@NonNull JSONArray jsonArray) throws JSONException {
        List<ExportCharacterChange> characterChanges = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            characterChanges.add(ExportCharacterChange.fromJson(jsonArray.getJSONObject(i)));
        }

        return characterChanges;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (ExportCharacterChange characterChange : characterChanges) {
            jsonArray.put(characterChange.toJson());
        }
        jsonObject.put("character_changes", jsonArray);

        return jsonObject;
    }
}
