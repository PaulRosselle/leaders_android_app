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
import com.leaders.puzzlelogic.utilities.GameStateHashUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Discovers game states in which the Nemesis has a move
 * that prevents the player from winning.
 * <p>
 * Each state is evaluated at most once per exploredStates set.
 * Only states with a blocking move are stored in blockingMoves.
 */
public final class NemesisMovesUtils {

    private NemesisMovesUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Discovers blocking moves while exploring the reachable game states.
     *
     * @param game current game state
     * @param history current game history
     * @param characters player characters in permutation order
     * @param permutationIndex index of the permutation to use
     * @param blockingMoves map associating each blocking state hash
     *                      with its first discovered blocking action
     * @param exploredStates hashes of states already evaluated
     * @param shouldCancel cancellation signal
     * @throws IllegalArgumentException if the current phase is not an ActionsPhase
     * @throws InterruptedException if the search is interrupted or canceled
     */
    public static void solve(@NonNull Game game,
                             @NonNull GameHistory history,
                             @NonNull List<Character> characters,
                             long permutationIndex,
                             @NonNull Map<String, CharacterAction> blockingMoves,
                             @NonNull Set<String> exploredStates,
                             @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisMovesUtils requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        List<Character> permutation = CharactersPermutationUtils.getPermutation(characters, permutationIndex);

        search(game, history, actionsPhase, permutation, new ArrayList<>(permutation),
                blockingMoves, exploredStates, shouldCancel
        );
    }

    /**
     * Explores reachable states recursively.
     */
    private static void search(@NonNull Game game,
                               @NonNull GameHistory history,
                               @NonNull ActionsPhase actionsPhase,
                               @NonNull List<Character> permutation,
                               @NonNull List<Character> remainingCharacters,
                               @NonNull Map<String, CharacterAction> blockingMoves,
                               @NonNull Set<String> exploredStates,
                               @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        checkCancellation(shouldCancel);

        // The hash represents the state before the next action.
        String stateHash = GameStateHashUtils.createHash(game, history);

        // This state has already been fully evaluated.
        if (!exploredStates.add(stateHash)) {
            return;
        }

        // No further actions can be explored after the game is decided.
        TeamColor playerTeam = actionsPhase.getTurnTeamColor();

        if (GameQuery.getWinnerTeam(game, playerTeam) != null) {
            return;
        }

        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, history);

        // Mandatory characters always have priority.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {

            PlayableCharacter playableCharacter = playableCharacters.get(0);
            Character character = playableCharacter.getCharacter();
            int characterIndex = remainingCharacters.indexOf(character);

            if (characterIndex != -1) {
                remainingCharacters.remove(characterIndex);
            }

            try {
                if (isNemesisCharacter(actionsPhase, character)) {
                    exploreNemesisActions(game, history, actionsPhase,
                            permutation, remainingCharacters, playableCharacter,
                            stateHash, blockingMoves, exploredStates, shouldCancel
                    );
                } else {
                    explorePlayerActions(game, history, actionsPhase,
                            permutation, remainingCharacters, playableCharacter,
                            blockingMoves, exploredStates, shouldCancel
                    );
                }
            } finally {
                if (characterIndex != -1) {
                    remainingCharacters.add(characterIndex, character);
                }
            }

            return;
        }

        // Follow the requested player-character order.
        // Characters that cannot play are skipped.
        List<Character> skippedCharacters = new ArrayList<>();

        try {
            while (!remainingCharacters.isEmpty()) {
                Character character = remainingCharacters.get(0);

                PlayableCharacter playableCharacter = GameSearchUtils.findPlayableCharacterById(
                        playableCharacters, character
                );

                if (playableCharacter != null) {
                    remainingCharacters.remove(0);

                    try {
                        explorePlayerActions(game, history, actionsPhase,
                                permutation, remainingCharacters, playableCharacter,
                                blockingMoves, exploredStates, shouldCancel
                        );
                    } finally {
                        remainingCharacters.add(0, character);
                    }

                    return;
                }

                skippedCharacters.add(remainingCharacters.remove(0));
            }
        } finally {
            for (int i = skippedCharacters.size() - 1; i >= 0; i--) {
                remainingCharacters.add(0, skippedCharacters.get(i));
            }
        }
    }

    /**
     * Explores player actions, including the option to skip an optional
     * character's action.
     */
    private static void explorePlayerActions(@NonNull Game game,
                                             @NonNull GameHistory history,
                                             @NonNull ActionsPhase actionsPhase,
                                             @NonNull List<Character> permutation,
                                             @NonNull List<Character> remainingCharacters,
                                             @NonNull PlayableCharacter playableCharacter,
                                             @NonNull Map<String, CharacterAction> blockingMoves,
                                             @NonNull Set<String> exploredStates,
                                             @NonNull BooleanSupplier shouldCancel) throws InterruptedException {

        if (!playableCharacter.isMandatory()) {
            search(game, history, actionsPhase, permutation,
                    remainingCharacters, blockingMoves, exploredStates, shouldCancel
            );
        }

        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                game, history, playableCharacter.getCharacter()
        );

        for (CharacterAction action : actions) {
            checkCancellation(shouldCancel);

            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                search(game, history, actionsPhase, permutation,
                        remainingCharacters, blockingMoves, exploredStates, shouldCancel
                );
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }
    }

    /**
     * Evaluates the blocking actions of a mandatory Nemesis character.
     * <p>
     * The first action that prevents every winning continuation for the
     * player is stored. All legal actions are still explored afterward
     * to discover other reachable states.
     */
    private static void exploreNemesisActions(@NonNull Game game,
                                              @NonNull GameHistory history,
                                              @NonNull ActionsPhase actionsPhase,
                                              @NonNull List<Character> permutation,
                                              @NonNull List<Character> remainingCharacters,
                                              @NonNull PlayableCharacter playableCharacter,
                                              @NonNull String stateHash,
                                              @NonNull Map<String, CharacterAction> blockingMoves,
                                              @NonNull Set<String> exploredStates,
                                              @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                game, history, playableCharacter.getCharacter()
        );

        // Find the first blocking action for this state.
        if (!blockingMoves.containsKey(stateHash)) {
            for (CharacterAction action : actions) {
                checkCancellation(shouldCancel);

                if (NemesisBlockerUtils.canBlock(game, history, action, permutation, shouldCancel)) {
                    blockingMoves.putIfAbsent(stateHash, action);
                    break;
                }
            }
        }

        // Explore every legal action to discover subsequent states.
        for (CharacterAction action : actions) {
            checkCancellation(shouldCancel);

            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                search(game, history, actionsPhase, permutation,
                        remainingCharacters, blockingMoves, exploredStates, shouldCancel
                );
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }
    }

    private static boolean isNemesisCharacter(@NonNull ActionsPhase actionsPhase, @NonNull Character character) {
        return character.getTeamColor() != actionsPhase.getTurnTeamColor();
    }

    private static void checkCancellation(@NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        if (Thread.currentThread().isInterrupted() || shouldCancel.getAsBoolean()) {
            throw new InterruptedException("Nemesis moves search cancelled");
        }
    }
}

