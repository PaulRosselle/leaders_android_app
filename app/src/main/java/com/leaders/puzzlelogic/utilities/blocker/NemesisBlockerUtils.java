package com.leaders.puzzlelogic.utilities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
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
import com.leaders.puzzlelogic.utilities.CharactersPermutationUtils;
import com.leaders.puzzlelogic.utilities.GameSearchUtils;

import java.util.List;
import java.util.function.BooleanSupplier;

public class NemesisBlockerUtils {
    private NemesisBlockerUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Determines whether the specified Nemesis action can block the opponent from winning.
     *
     * @param game the current game
     * @param history the game history containing the current phase
     * @param nemesisAction the Nemesis action to evaluate
     * @param permutation the character ordering used to evaluate the opponent's winning opportunities
     * @param shouldCancel the cancellation signal checked during the evaluation
     * @return {@code true} if the action prevents the opponent from winning; {@code false} otherwise
     * @throws IllegalArgumentException if the current phase is not an {@link ActionsPhase}
     * @throws InterruptedException if the evaluation is interrupted
     * @throws NullPointerException if any parameter annotated with {@code @NonNull} is {@code null}
     */
    public static boolean canBlock(@NonNull Game game,
                                   @NonNull GameHistory history,
                                   @NonNull CharacterAction nemesisAction,
                                   @NonNull List<Character> permutation,
                                   @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisBlockerUtils requires an ActionsPhase");
        }
        ActionsPhase actionsPhase = (ActionsPhase) phase;

        GameActionHandler handler = GameActionHandlerFactory.create(game, nemesisAction);

        handler.doAction();
        actionsPhase.getActions().add(nemesisAction);

        try {
            return !canOpponentWin(game, history, actionsPhase, permutation, shouldCancel);
        } finally {
            actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
            handler.undoAction();
        }
    }

    /**
     * Determines whether the specified Nemesis action can prevent the opponent from winning
     * for the given character permutation.
     *
     * @param game the current game
     * @param history the game history containing the current phase
     * @param nemesisAction the Nemesis action to evaluate
     * @param opponentCharacters the characters whose permutation defines the opponent's playing order
     * @param permutationIndex the index of the permutation to evaluate
     * @param shouldCancel the cancellation signal checked during the evaluation
     * @return {@code true} if the action prevents the opponent from winning; {@code false} otherwise
     * @throws IllegalArgumentException if the current phase is not an {@link ActionsPhase}
     * @throws InterruptedException if the evaluation is interrupted
     * @throws NullPointerException if any parameter annotated with {@code @NonNull} is {@code null}
     */
    public static boolean canBlock(@NonNull Game game,
                                   @NonNull GameHistory history,
                                   @NonNull CharacterAction nemesisAction,
                                   @NonNull List<Character> opponentCharacters,
                                   long permutationIndex,
                                   @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        List<Character> permutation = CharactersPermutationUtils.getPermutation(
                opponentCharacters, permutationIndex
        );
        return canBlock(game, history, nemesisAction, permutation, shouldCancel);
    }

    /**
     * Checks whether the current operation has been cancelled or interrupted.
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
     * Determines whether the opponent can win from the current game state by exploring
     * the available character actions in the specified order.
     *
     * @param game the current game
     * @param history the game history containing the current phase
     * @param actionsPhase the current actions phase
     * @param remainingCharacters the characters remaining to be considered, in evaluation order;
     *                           this list is restored to its original state after the evaluation
     * @param shouldCancel the cancellation signal checked during the search
     * @return {@code true} if the opponent can win; {@code false} otherwise
     * @throws InterruptedException if the search is interrupted or cancellation is requested
     * @throws NullPointerException if any parameter annotated with {@code @NonNull} is {@code null}
     */
    private static boolean canOpponentWin(@NonNull Game game,
                                          @NonNull GameHistory history,
                                          @NonNull ActionsPhase actionsPhase,
                                          @NonNull List<Character> remainingCharacters,
                                          @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        TeamColor currentTeam = actionsPhase.getTurnTeamColor();
        TeamColor opponentTeam = currentTeam.getOpposite();

        // Before exploring new actions, we check if the game isn't already won
        TeamColor winnerTeam = GameQuery.getWinnerTeam(game, currentTeam);
        if (winnerTeam != null) {
            return winnerTeam == opponentTeam;
        }

        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, history);

        // MANDATORY CHARACTER : always has priority over the requested order.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {
            PlayableCharacter playableCharacter = playableCharacters.get(0);
            Character character = playableCharacter.getCharacter();

            int characterIndex = remainingCharacters.indexOf(character);

            if (characterIndex != -1) {
                remainingCharacters.remove(characterIndex);
            }

            try {
                return exploreOpponentCharacterActions(
                        game, history, actionsPhase, remainingCharacters, playableCharacter, shouldCancel
                );
            } finally {
                if (characterIndex != -1) {
                    remainingCharacters.add(characterIndex, character);
                }
            }
        }

        // PLAYABLE CHARACTER : we play the remaining characters in order.
        if (remainingCharacters.isEmpty()) {
            return false;
        }

        Character character = remainingCharacters.get(0);

        PlayableCharacter playableCharacter = GameSearchUtils.findPlayableCharacterById(
                playableCharacters, character
        );

        // This character is not playable in the current state.
        // Skip it in this permutation and continue with the next character.
        if (playableCharacter == null) {
            remainingCharacters.remove(0);
            try {
                return canOpponentWin(game, history, actionsPhase, remainingCharacters, shouldCancel);
            } finally {
                remainingCharacters.add(0, character);
            }
        }

        remainingCharacters.remove(0);

        try {
            // If playing this character is not mandatory, we can try to skip its action
            if (!playableCharacter.isMandatory() &&
                    canOpponentWin(game, history, actionsPhase, remainingCharacters, shouldCancel)) {
                return true;
            }

            return exploreOpponentCharacterActions(
                    game, history, actionsPhase, remainingCharacters, playableCharacter, shouldCancel
            );
        } finally {
            remainingCharacters.add(0, character);
        }
    }

    /**
     * Determines whether any available action for the specified character allows the opponent to win.
     *
     * @param game the current game
     * @param history the game history containing the current phase
     * @param actionsPhase the current actions phase
     * @param remainingCharacters the characters remaining to be considered, in evaluation order
     * @param playableCharacter the character whose available actions are evaluated
     * @param shouldCancel the cancellation signal checked during the search
     * @return {@code true} if at least one available action allows the opponent to win;
     *         {@code false} otherwise
     * @throws InterruptedException if the search is interrupted or cancellation is requested
     * @throws NullPointerException if any parameter annotated with {@code @NonNull} is {@code null}
     */
    private static boolean exploreOpponentCharacterActions(@NonNull Game game,
                                                           @NonNull GameHistory history,
                                                           @NonNull ActionsPhase actionsPhase,
                                                           @NonNull List<Character> remainingCharacters,
                                                           @NonNull PlayableCharacter playableCharacter,
                                                           @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                game, history, playableCharacter.getCharacter()
        );

        for (CharacterAction action : actions) {
            checkCancellation(shouldCancel);

            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                if (canOpponentWin(game, history, actionsPhase, remainingCharacters, shouldCancel)) {
                    return true;
                }
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        return false;
    }
}
