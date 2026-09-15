package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONException;
import org.json.JSONObject;

public class Contributor {
    @NonNull
    private final TeamColor teamColor;
    @NonNull
    private final CharacterType characterType;
    @NonNull
    private final String name;
    private final boolean hasContributedToAlpha;
    private final boolean hasContributedToBeta;

    private Contributor(@NonNull TeamColor teamColor,
                       @NonNull CharacterType characterType,
                       @NonNull String name,
                       boolean hasContributedToAlpha,
                       boolean hasContributedToBeta) {
        this.teamColor = teamColor;
        this.characterType = characterType;
        this.name = name;
        this.hasContributedToAlpha = hasContributedToAlpha;
        this.hasContributedToBeta = hasContributedToBeta;
    }

    @NonNull
    public JSONObject getAsJson() {
        JSONObject joContributor = new JSONObject();

        try {
            joContributor.put("team_color", teamColor.name());
            joContributor.put("character_type", characterType.name());
            joContributor.put("name", name);
            joContributor.put("has_contributed_to_alpha", hasContributedToAlpha);
            joContributor.put("has_contributed_to_beta", hasContributedToBeta);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return joContributor;
    }

    public static Contributor getFromJson(@NonNull JSONObject joContributor) throws JSONException {
        return new Contributor(
                TeamColor.valueOf(joContributor.getString("team_color")),
                CharacterType.valueOf(joContributor.getString("character_type")),
                joContributor.getString("name"),
                joContributor.getBoolean("has_contributed_to_alpha"),
                joContributor.getBoolean("has_contributed_to_beta")
        );
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public CharacterType getCharacterType() {
        return characterType;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean hasContributedToAlpha() {
        return hasContributedToAlpha;
    }

    public boolean hasContributedToBeta() {
        return hasContributedToBeta;
    }
}
