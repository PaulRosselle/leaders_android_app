package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class GameHistorySerializer implements IJsonSerializer<GameHistory> {
    @NonNull
    @Override
    public GameHistory getFromJson(@NonNull JSONObject jsonObject,
                                   @NonNull SerializationContext srlContext) throws JSONException {
        GameConfigSerializer gameConfigSerializer = new GameConfigSerializer();
        TurnSerializer turnSerializer = new TurnSerializer();
        BanishmentPhaseSerializer banishmentPhaseSerializer = new BanishmentPhaseSerializer();

        GameConfig config = gameConfigSerializer.getFromJsonName(jsonObject, srlContext, "config");

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        JSONArray jaEntries = jsonObject.getJSONArray("entries");

        for (int i = 0; i < jaEntries.length(); i++) {
            JSONObject entryJson = jaEntries.getJSONObject(i);
            String entryType = entryJson.getString("type");

            switch (entryType) {
                case "turn": entries.add(turnSerializer.getFromJson(entryJson, srlContext));break;
                case "banishment_phase": entries.add(banishmentPhaseSerializer.getFromJson(entryJson, srlContext));break;
                default: throw new JSONException("Unsupported history entry type: " + entryType);
            }
        }

        return new GameHistory(config, entries);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull GameHistory object) throws JSONException {
        GameConfigSerializer gameConfigSerializer = new GameConfigSerializer();
        TurnSerializer turnSerializer = new TurnSerializer();
        BanishmentPhaseSerializer banishmentPhaseSerializer = new BanishmentPhaseSerializer();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("config", gameConfigSerializer.getAsJson(object.getConfig()));

        JSONArray jaEntries = new JSONArray();

        for (IHistoryEntry entry : object.getEntries()) {
            JSONObject entryJson;

            if (entry instanceof Turn) {
                entryJson = turnSerializer.getAsJson((Turn) entry);
                entryJson.put("type", "turn");
            } else if (entry instanceof BanishmentPhase) {
                entryJson = banishmentPhaseSerializer.getAsJson((BanishmentPhase) entry);
                entryJson.put("type", "banishment_phase");
            } else {
                throw new JSONException("Unsupported history entry: " + entry.getClass());
            }

            jaEntries.put(entryJson);
        }

        jsonObject.put("entries", jaEntries);

        return jsonObject;
    }
}