package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.entities.Game;

public final class BanishmentActionHandler extends ActionHandler {
    @NonNull
    private final BanishmentAction banishmentAction;

    public BanishmentActionHandler(@NonNull Game game, @NonNull BanishmentAction banishmentAction) {
        super(game);
        this.banishmentAction = banishmentAction;
    }

    @Override
    public void doAction() {
        game.getRecruitableCards().remove(banishmentAction.getCharacterCard());
        game.getBanishedCards().add(banishmentAction.getCharacterCard());
    }

    @Override
    public void undoAction() {
        game.getBanishedCards().add(banishmentAction.getCharacterCard());
        game.getRecruitableCards().remove(banishmentAction.getCharacterCard());
    }
}
