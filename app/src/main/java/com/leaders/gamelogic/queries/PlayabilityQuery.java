package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.CharacterPlayableState;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlayabilityQuery {
    private PlayabilityQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Creates a playable state for a character located on the given cell.
     *
     * @param game the current game state used to evaluate character abilities
     * @param characterCell the board cell containing the character
     * @param mandatory whether the character is required to act before the end of the turn
     * @return the playable state of the character
     */
    @NonNull
    private static CharacterPlayableState getState(@NonNull Game game, @NonNull Cell characterCell, boolean mandatory) {
        return new CharacterPlayableState(
                characterCell.getCharacter(), characterCell.getPosition(), mandatory,
                CharacterAbilityQuery.canUseActiveAbility(game, characterCell.getCharacter()));
    }

    /**
     * Returns the playable states of all characters for the current action phase.
     *
     * <p>A character can only be playable during a valid {@link ActionsPhase}.
     * If a character has an immediate action requirement, only that character is
     * returned. Otherwise, all characters currently allowed to act are returned,
     * with their mandatory status indicating whether their action is required
     * before the end of the turn.</p>
     *
     * @param game the current game state
     * @param gameHistory the game history used to determine the current phase and turn information
     * @return the list of playable character states
     * @throws IllegalStateException if the current phase is not an {@link ActionsPhase}
     */
    @NonNull
    public static List<CharacterPlayableState> getCharacterPlayableStates(@NonNull Game game,
                                                                          @NonNull GameHistory gameHistory) {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalStateException("A character cannot be playable outside of a valid actions phase");
        }
        ActionsPhase actionsPhase = (ActionsPhase) currentPhase;

        List<CharacterPlayableState> characterPlayableStates = new ArrayList<>();
        List<Cell> characterCells = BoardQuery.findCharacterCells(game.getBoard(), null, null);
        for (Cell characterCell : characterCells) {
            // If there is a character forced to play immediately, we return them alone
            if (CharacterAbilityQuery.mustActNow(actionsPhase, characterCell.getCharacter())) {
                return Collections.singletonList(getState(game, characterCell, true));
            }
            // If a character is allowed to act, we add them to the list
            if (CharacterAbilityQuery.canAct(actionsPhase, characterCell.getCharacter())) {
                characterPlayableStates.add(getState(game, characterCell, CharacterAbilityQuery.mustAct()));
            }
        }
        return characterPlayableStates;
    }
}
