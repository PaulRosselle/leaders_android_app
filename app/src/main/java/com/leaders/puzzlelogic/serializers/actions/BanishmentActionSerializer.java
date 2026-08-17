package com.leaders.puzzlelogic.serializers.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;

import org.json.JSONException;
import org.json.JSONObject;

public final class BanishmentActionSerializer implements IJsonSerializer<BanishmentAction> {
    @NonNull
    @Override
    public BanishmentAction getFromJson(@NonNull JSONObject jsonObject,
                                        @NonNull SerializationContext srlContext) throws JSONException {
        CharacterCard characterCard =
                CharacterCard.valueOf(jsonObject.getString("character_card"));
        TeamColor teamColor =
                TeamColor.valueOf(jsonObject.getString("team_color"));

        return new BanishmentAction(characterCard, teamColor);
    }

    @NonNull
    @Override
    public JSONObject getAsJson(BanishmentAction object) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("character_card", object.getCharacterCard().name());
        jsonObject.put("team_color", object.getTeamColor().name());

        return jsonObject;
    }
}