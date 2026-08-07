package com.leaders.gamelogic.historyentries.segments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;

import java.util.ArrayList;
import java.util.List;

public final class ActionsPhase extends TurnPhase {
    @NonNull
    private final ArrayList<IGameAction> actions;

    public ActionsPhase(@Nullable TransitionAction startAction, @Nullable TransitionAction endAction, @NonNull TeamColor turnTeamColor) {
        super(startAction, endAction, turnTeamColor);
        actions = new ArrayList<>();
    }

    public ActionsPhase(@NonNull ActionsPhase refActionsPhase) {
        this(refActionsPhase.getStartAction(), refActionsPhase.getEndAction(), refActionsPhase.getTurnTeamColor());
        actions.addAll(refActionsPhase.getActions());
    }

    @NonNull
    @Override
    public ArrayList<IGameAction> getActions() {
        return actions;
    }

    /**
     * Retrieves all character actions from the current list of actions.
     * <p>
     * This method filters the actions and returns only those that are instances
     * of {@link CharacterAction}. If an action of another type is encountered,
     * an {@link IllegalStateException} is thrown because only character actions
     * are supported during an actions phase.
     *
     * @return a list containing all {@link CharacterAction} instances from the current actions
     * @throws IllegalStateException if an action that is not a {@link CharacterAction}
     *                              is found in the actions list
     */
    public List<CharacterAction> getCharacterActions() {
        List<CharacterAction> characterActions = new ArrayList<>();
        for (IGameAction action : actions) {
            if (action instanceof CharacterAction) {
                characterActions.add((CharacterAction) action);
            } else {
                throw new IllegalStateException("CharacterAction is the only action type supported with an ActionsPhase\"");
            }
        }
        return characterActions;
    }

    @NonNull
    @Override
    public TransitionTarget getTransitionTarget() {
        return TransitionTarget.ActionsPhase;
    }
}
