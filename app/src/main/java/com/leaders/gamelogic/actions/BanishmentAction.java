package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.TeamColor;

public final class BanishmentAction implements IGameAction {
    @Override
    public GameActionType getActionType() {
        return GameActionType.Banishment;
    }

    @NonNull
    private final CharacterCard characterCard;
    @NonNull
    private final TeamColor teamColor;

    public BanishmentAction(@NonNull CharacterCard characterCard, @NonNull TeamColor teamColor) {
        this.characterCard = characterCard;
        this.teamColor = teamColor;
    }

    @NonNull
    public CharacterCard getCharacterCard() {
        return characterCard;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }
}
