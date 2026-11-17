package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.FirstPlayerTeam;
import com.leaders.gamelogic.enums.GameMode;

import org.json.JSONException;
import org.json.JSONObject;

public class DuelSetup {
    @NonNull
    private PlayerSetup playerSetup1;
    @NonNull
    private PlayerSetup playerSetup2;
    @NonNull
    private GameMode gameMode;
    @NonNull
    private FirstPlayerTeam firstPlayerTeam;
    @NonNull
    private CharactersSetup charactersSetup;

    public DuelSetup(@NonNull PlayerSetup playerSetup1,
                     @NonNull PlayerSetup playerSetup2,
                     @NonNull GameMode gameMode,
                     @NonNull FirstPlayerTeam firstPlayerTeam,
                     @NonNull CharactersSetup charactersSetup) {
        this.playerSetup1 = playerSetup1;
        this.playerSetup2 = playerSetup2;
        this.gameMode = gameMode;
        this.firstPlayerTeam = firstPlayerTeam;
        this.charactersSetup = charactersSetup;
    }

    @NonNull
    public PlayerSetup getPlayerSetup1() {
        return playerSetup1;
    }

    public void setPlayerSetup1(@NonNull PlayerSetup playerSetup1) {
        this.playerSetup1 = playerSetup1;
    }

    @NonNull
    public PlayerSetup getPlayerSetup2() {
        return playerSetup2;
    }

    public void setPlayerSetup2(@NonNull PlayerSetup playerSetup2) {
        this.playerSetup2 = playerSetup2;
    }

    @NonNull
    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(@NonNull GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @NonNull
    public FirstPlayerTeam getFirstPlayerTeam() {
        return firstPlayerTeam;
    }

    public void setFirstPlayerTeam(@NonNull FirstPlayerTeam firstPlayerTeam) {
        this.firstPlayerTeam = firstPlayerTeam;
    }

    @NonNull
    public CharactersSetup getCharactersSetup() {
        return charactersSetup;
    }

    public void setCharactersSetup(@NonNull CharactersSetup charactersSetup) {
        this.charactersSetup = charactersSetup;
    }

    @NonNull
    public static DuelSetup fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new DuelSetup(
                PlayerSetup.fromJson(jsonObject.getJSONObject("player_setup_1")),
                PlayerSetup.fromJson(jsonObject.getJSONObject("player_setup_2")),
                GameMode.valueOf(jsonObject.getString("game_mode")),
                FirstPlayerTeam.valueOf(jsonObject.getString("first_player_team")),
                CharactersSetup.fromJson(jsonObject.getJSONObject("characters_setup"))
        );
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        JSONObject joDuelSetup = new JSONObject();

        joDuelSetup.put("player_setup_1", playerSetup1.getAsJson());
        joDuelSetup.put("player_setup_2", playerSetup2.getAsJson());
        joDuelSetup.put("game_mode", gameMode.name());
        joDuelSetup.put("first_player_team", firstPlayerTeam.name());
        joDuelSetup.put("characters_setup", charactersSetup.getAsJson());

        return joDuelSetup;
    }
}
