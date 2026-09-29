package com.leaders.app.export.actions;

import androidx.annotation.NonNull;

import com.leaders.app.export.enums.ExportActionType;
import com.leaders.app.export.enums.ExportCharacterType;
import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExportBanishmentAction extends ExportAction {
    @Override
    public ExportActionType getActionType() {
        return ExportActionType.BANISHMENT;
    }

    @NonNull
    private final List<ExportCharacterType> characterTypes;

    public ExportBanishmentAction(@NonNull List<ExportCharacterType> characterTypes) {
        this.characterTypes = characterTypes;
    }

    public ExportBanishmentAction(@NonNull BanishmentAction banishmentAction) {
        this(getCharacterTypesFrom(banishmentAction));
    }

    public BanishmentAction getBanismentAction(@NonNull TeamColor turnTeam) {
        return new BanishmentAction(getCharacterCard(), turnTeam);
    }

    private CharacterCard getCharacterCard() {
        if (characterTypes.isEmpty()) {
            throw new IllegalStateException("A character type is required for a ban action export to be valid");
        }
        CharacterCard characterCard = characterTypes.get(0).getAsCharacterType().getCharacterCard();
        for (ExportCharacterType characterType : characterTypes) {
            if (characterType.getAsCharacterType().getCharacterCard() != characterCard) {
                throw new IllegalStateException("A banishment action should handle only one character ban");
            }
        }
        return characterCard;
    }

    @NonNull
    public static ExportBanishmentAction fromJson(@NonNull JSONObject jsonObject) throws JSONException {
        return new ExportBanishmentAction(
                getCharacterTypesFrom(jsonObject.getJSONArray("character_types"))
        );
    }
    @NonNull
    private static List<ExportCharacterType> getCharacterTypesFrom(@NonNull BanishmentAction banishmentAction) {
        List<ExportCharacterType> characterTypes = new ArrayList<>();
        for (CharacterType characterType : CharacterType.getCharacterTypesMatchingCard(banishmentAction.getCharacterCard())) {
            characterTypes.add(ExportCharacterType.getFromCharacterType(characterType));
        }
        return characterTypes;
    }

    @NonNull
    private static List<ExportCharacterType> getCharacterTypesFrom(@NonNull JSONArray jsonArray) throws JSONException {
        List<ExportCharacterType> characterTypes = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            characterTypes.add(ExportCharacterType.valueOf(jsonArray.getString(i)));
        }
        return characterTypes;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = super.toJson();

        JSONArray jsonArray = new JSONArray();
        for (ExportCharacterType characterType : characterTypes) {
            jsonArray.put(characterType.name());
        }
        jsonObject.put("character_types", jsonArray);

        return jsonObject;
    }
}
