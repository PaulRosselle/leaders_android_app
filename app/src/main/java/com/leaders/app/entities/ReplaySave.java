package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;

public class ReplaySave {
    @NonNull
    private String name;
    @NonNull
    private LocalDate date;
    @NonNull
    private final GameMode gameMode;
    @NonNull
    private final List<Player> players;
    @NonNull
    protected JSONObject datas;

    private ReplaySave(@NonNull String name, @NonNull LocalDate date, @NonNull GameMode gameMode,
                       @NonNull List<Player> players, @NonNull JSONObject datas) {
        this.name = name;
        this.date = date;
        this.gameMode = gameMode;
        this.players = players;
        this.datas = datas;
    }

    public ReplaySave(@NonNull String name, @NonNull LocalDate date, @NonNull GameHistory gameHistory) {
        this.date = date;
        this.name = name;
        this.gameMode = gameHistory.getConfig().getGameMode();
        this.players = gameHistory.getConfig().getPlayers();
        try {
            this.datas = (new GameHistorySerializer()).getAsJson(gameHistory);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static ReplaySave getFromJson(@NonNull JSONObject joReplaySave) {
        try {
            JSONObject datas = joReplaySave.getJSONObject("datas");
            GameHistory gameHistory = (new GameHistorySerializer()).getFromJson(datas, new SerializationContext());

            return new ReplaySave(
                    joReplaySave.getString("name"),
                    LocalDate.parse(joReplaySave.getString("date")),
                    gameHistory.getConfig().getGameMode(),
                    gameHistory.getConfig().getPlayers(),
                    datas
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    @NonNull
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzleSave = new JSONObject();
        joPuzzleSave.put("name", getName());
        joPuzzleSave.put("date", getDate().toString());
        joPuzzleSave.put("datas", datas);
        return joPuzzleSave;
    }

    public final GameHistory getReplayGameHistory() {
        try {
            return (new GameHistorySerializer()).getFromJson(datas, new SerializationContext());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    @NonNull
    public GameMode getGameMode() {
        return gameMode;
    }

    @NonNull
    public List<Player> getPlayers() {
        return players;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }
}
