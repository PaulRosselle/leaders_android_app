package com.leaders.puzzlelogic.utilities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.GameQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.puzzlelogic.utilities.GameSearchUtils;
import com.leaders.puzzlelogic.utilities.GameStateHashUtils;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class NemesisBlockerUtils {
    private NemesisBlockerUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Determines whether the specified Nemesis action can block the opponent from winning.
     *
     * @param game the current game
     * @param history the game history containing the current phase
     * @param nemesisAction the Nemesis action to evaluate
     * @param shouldCancel the cancellation signal checked during the evaluation
     * @return {@code true} if the action prevents the opponent from winning; {@code false} otherwise
     * @throws IllegalArgumentException if the current phase is not an {@link ActionsPhase}
     * @throws InterruptedException if the evaluation is interrupted
     * @throws NullPointerException if any parameter annotated with {@code @NonNull} is {@code null}
     */
    public static boolean canBlock(@NonNull Game game,
                                   @NonNull GameHistory history,
                                   @NonNull CharacterAction nemesisAction,
                                   @NonNull Set<String> losingStates,
                                   @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisBlockerUtils requires an ActionsPhase");
        }
        ActionsPhase actionsPhase = (ActionsPhase) phase;

        return !applyAndCheck(game, history, actionsPhase, nemesisAction, losingStates, shouldCancel);
    }

    /**
     * Checks whether the current operation has been canceled or interrupted.
     *
     * @param shouldCancel the cancellation signal
     * @throws InterruptedException if the current thread is interrupted or cancellation is requested
     * @throws NullPointerException if {@code shouldCancel} is {@code null}
     */
    private static void checkCancellation(@NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        if (Thread.currentThread().isInterrupted() || shouldCancel.getAsBoolean()) {
            throw new InterruptedException("Nemesis blocker search cancelled");
        }
    }


    /**
     * Checks whether the current team can force a win from the given game state.
     *
     * @param game the current game state
     * @param history the game history
     * @param phase the current actions phase
     * @param losingStates the hashes of game states known to be losing
     * @param shouldCancel the condition used to cancel the search
     * @return {@code true} if the current team can force a win, otherwise {@code false}
     * @throws InterruptedException if the search is interrupted
     */
    private static boolean canPlayerWin(Game game, GameHistory history, ActionsPhase phase,
                                        Set<String> losingStates,
                                        BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        TeamColor team = phase.getTurnTeamColor();
        TeamColor winner = GameQuery.getWinnerTeam(game, team);
        if (winner != null) return winner == team;

        String hash = GameStateHashUtils.createHash(game, history);
        if (losingStates.contains(hash)) return false;

        List<PlayableCharacter> playable = PlayabilityQuery.getPlayableCharacters(game, history);
        boolean result;

        if (playable.size() == 1 && playable.get(0).isMandatory()
                && playable.get(0).getCharacter().getTeamColor() != team) {
            // Le Nemesis choisit : le joueur doit gagner contre TOUTES ses actions (AND).
            result = true;
            List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                    game, history, playable.get(0).getCharacter());
            if (actions.isEmpty()) result = false;
            for (CharacterAction a : actions) {
                if (!applyAndCheck(game, history, phase, a, losingStates, shouldCancel)) {
                    result = false;
                    break;
                }
            }
        } else {
            // Le joueur choisit : n'importe quel personnage jouable, n'importe quelle action (OR).
            result = false;
            outer:
            for (PlayableCharacter pc : playable) {
                if (pc.getCharacter().getTeamColor() != team) continue;
                for (CharacterAction a : GameSearchUtils.enumerateActions(game, history, pc.getCharacter())) {
                    if (applyAndCheck(game, history, phase, a, losingStates, shouldCancel)) {
                        result = true;
                        break outer;
                    }
                }
            }
        }

        if (!result) losingStates.add(hash);
        return result;
    }

    /**
     * Checks whether the current team can force a win after the given action.
     *
     * @param game the current game state
     * @param history the game history
     * @param phase the current actions phase
     * @param action the action to evaluate
     * @param losingStates the hashes of game states known to be losing
     * @param shouldCancel the condition used to cancel the search
     * @return {@code true} if the current team can force a win after the action, otherwise {@code false}
     * @throws InterruptedException if the search is interrupted
     */
    private static boolean applyAndCheck(Game game, GameHistory history, ActionsPhase phase,
                                         CharacterAction action, Set<String> losingStates,
                                         BooleanSupplier shouldCancel) throws InterruptedException {
        GameActionHandler handler = GameActionHandlerFactory.create(game, action);
        handler.doAction();
        phase.getActions().add(action);
        try {
            return canPlayerWin(game, history, phase, losingStates, shouldCancel);
        } finally {
            phase.getActions().remove(phase.getActions().size() - 1);
            handler.undoAction();
        }
    }
}
