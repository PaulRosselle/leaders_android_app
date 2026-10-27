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
import java.util.function.BooleanSupplier;

public final class NemesisBlockerUtils {
    private NemesisBlockerUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Checks whether the given Nemesis action can be blocked by the current team.
     *
     * @param game the current game state
     * @param history the game history
     * @param nemesisAction the Nemesis action to evaluate
     * @param searchCache the cache used to store search results
     * @param shouldCancel the condition used to cancel the search
     * @return {@code true} if the action can be blocked, otherwise {@code false}
     * @throws IllegalArgumentException if the current phase is not an {@code ActionsPhase}
     * @throws InterruptedException if the search is interrupted
     */
    public static boolean canBlock(@NonNull Game game,
                                   @NonNull GameHistory history,
                                   @NonNull CharacterAction nemesisAction,
                                   @NonNull SearchCache searchCache,
                                   @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisBlockerUtils requires an ActionsPhase");
        }
        ActionsPhase actionsPhase = (ActionsPhase) phase;

        return !applyAndCheck(game, history, actionsPhase, nemesisAction, searchCache, shouldCancel);
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
     * @param searchCache the cache used to store search results
     * @param shouldCancel the condition used to cancel the search
     * @return {@code true} if the current team can force a win, otherwise {@code false}
     * @throws InterruptedException if the search is interrupted
     */
    private static boolean canPlayerWin(@NonNull Game game,
                                        @NonNull GameHistory history,
                                        @NonNull ActionsPhase phase,
                                        @NonNull SearchCache searchCache,
                                        @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        TeamColor team = phase.getTurnTeamColor();
        TeamColor winner = GameQuery.getWinnerTeam(game, team);
        if (winner != null) {
            return winner == team; // Ends the search, no need to hash
        }

        String hash = GameStateHashUtils.createHash(game, history);
        Boolean cached = searchCache.get(hash);
        if (cached != null) {
            return cached;
        }

        List<PlayableCharacter> playable = PlayabilityQuery.getPlayableCharacters(game, history);
        boolean result;

        if (playable.size() == 1 && playable.get(0).isMandatory()
                && playable.get(0).getCharacter().getTeamColor() != team) {
            // Nemesis actions : the player must win against all her actions (AND).
            List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                    game, history, playable.get(0).getCharacter());
            result = !actions.isEmpty();
            for (CharacterAction action : actions) {
                if (!applyAndCheck(game, history, phase, action, searchCache, shouldCancel)) {
                    result = false;
                    break;
                }
            }
        } else {
            // Ally actions : any action can lead to the player victory (OR).
            result = false;
            outer:
            for (PlayableCharacter pc : playable) {
                if (pc.getCharacter().getTeamColor() != team) continue;
                for (CharacterAction action : GameSearchUtils.enumerateActions(game, history, pc.getCharacter())) {
                    if (applyAndCheck(game, history, phase, action, searchCache, shouldCancel)) {
                        result = true;
                        break outer;
                    }
                }
            }
        }

        searchCache.put(hash, result);
        return result;
    }

    /**
     * Checks whether the current team can force a win after the given action.
     *
     * @param game the current game state
     * @param history the game history
     * @param phase the current actions phase
     * @param action the action to evaluate
     * @param searchCache the cache used to store search results
     * @param shouldCancel the condition used to cancel the search
     * @return {@code true} if the current team can force a win after the action, otherwise {@code false}
     * @throws InterruptedException if the search is interrupted
     */
    private static boolean applyAndCheck(@NonNull Game game,
                                         @NonNull GameHistory history,
                                         @NonNull ActionsPhase phase,
                                         @NonNull CharacterAction action,
                                         @NonNull SearchCache searchCache,
                                         @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        GameActionHandler handler = GameActionHandlerFactory.create(game, action);
        handler.doAction();
        phase.getActions().add(action);
        try {
            return canPlayerWin(game, history, phase, searchCache, shouldCancel);
        } finally {
            phase.getActions().remove(phase.getActions().size() - 1);
            handler.undoAction();
        }
    }
}
