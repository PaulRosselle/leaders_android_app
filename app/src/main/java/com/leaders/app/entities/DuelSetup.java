package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.FirstPlayerTeam;
import com.leaders.app.enums.LeaderType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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

    public DuelSetup() {
        List<LeaderType> leaderTypes = new ArrayList<>(Arrays.asList(LeaderType.values()));
        Collections.shuffle(leaderTypes);

        playerSetup1 = new PlayerSetup("", TeamColor.Black, leaderTypes.remove(0));
        playerSetup2 = new PlayerSetup("", TeamColor.White, leaderTypes.remove(0));
        gameMode = GameMode.Discovery;
        firstPlayerTeam = FirstPlayerTeam.Random;
        charactersSetup = new CharactersSetup(new HashSet<>(), new HashSet<>());
    }

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
    public JSONObject toJson() throws JSONException {
        JSONObject joDuelSetup = new JSONObject();

        joDuelSetup.put("player_setup_1", playerSetup1.toJson());
        joDuelSetup.put("player_setup_2", playerSetup2.toJson());
        joDuelSetup.put("game_mode", gameMode.name());
        joDuelSetup.put("first_player_team", firstPlayerTeam.name());
        joDuelSetup.put("characters_setup", charactersSetup.toJson());

        return joDuelSetup;
    }
}
