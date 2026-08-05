package com.leaders.gamelogic.factories;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.handlers.BanishmentActionHandler;
import com.leaders.gamelogic.handlers.CharacterActionHandler;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.handlers.RecruitmentActionHandler;
import com.leaders.gamelogic.handlers.TransitionActionHandler;
import com.leaders.gamelogic.handlers.WarningActionHandler;

public final class GameActionHandlerFactory {
    private GameActionHandlerFactory(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    @NonNull
    public static GameActionHandler create(@NonNull Game game, @NonNull IGameAction gameAction) {
        switch (gameAction.getActionType()) {
            case Transition: return new TransitionActionHandler(game, (TransitionAction) gameAction);
            case CharacterAction: return new CharacterActionHandler(game, (CharacterAction) gameAction);
            case Recruitment: return new RecruitmentActionHandler(game, (RecruitmentAction) gameAction);
            case Banishment: return new BanishmentActionHandler(game, (BanishmentAction) gameAction);
            case Warning: return new WarningActionHandler(game, (WarningAction) gameAction);
            default: throw new IllegalArgumentException("No handler found for action type " + gameAction.getActionType());
        }
    }
}
