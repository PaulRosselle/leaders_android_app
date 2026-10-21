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
import java.util.stream.Collectors;

/**
 * Discovers game states in which the Nemesis has a move that prevents the
 * player from winning.
 *
 * <p>The search is split in two independent concerns:</p>
 * <ul>
 *   <li><b>Enumeration</b>: {@code search} walks the reachable states following
 *   the requested character permutation. It only decides <i>which states are
 *   visited</i>.</li>
 *   <li><b>Proof</b>: {@link NemesisBlockerUtils#canBlock} decides whether a Nemesis
 *   action blocks the player. It is exhaustive and independent from any
 *   permutation.</li>
 * </ul>
 *
 * <h2>Shared sets</h2>
 * <ul>
 *   <li>{@code losingStates} (hash only): states from which the player cannot win.
 *   This is a fact about the game, valid for every permutation and every caller.
 *   Must be thread-safe if shared between threads.</li>
 *   <li>{@code exploredStates} (hash + remaining characters): enumeration nodes
 *   already visited. It depends on the permutation and must belong to a single
 *   exploration. It is cleared if the search is interrupted, because nodes are
 *   marked before being fully visited.</li>
 *   <li>{@code nemesisEvaluated} (hash only): Nemesis states whose actions have
 *   been completely evaluated. Entries are only added after a complete
 *   evaluation, so an interruption never leaves a wrong entry.</li>
 * </ul>
 *
 * <p>The supplied {@link Game} and {@link GameHistory} are mutated during the
 * search (and restored). Never use the same instances from another thread.</p>
 */
public final class NemesisMovesUtils {

    private NemesisMovesUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Discovers blocking moves without cross-checking the results.
     *
     * @see #solve(Game, GameHistory, List, long, Map, Set, Set, Set, BooleanSupplier, boolean)
     */
    public static void solve(@NonNull Game game,
                             @NonNull GameHistory history,
                             @NonNull List<Character> characters,
                             long permutationIndex,
                             @NonNull Map<String, CharacterAction> blockingMoves,
                             @NonNull Set<String> exploredStates,
                             @NonNull Set<String> nemesisEvaluated,
                             @NonNull Set<String> losingStates,
                             @NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        solve(game, history, characters, permutationIndex, blockingMoves,
                exploredStates, nemesisEvaluated, losingStates, shouldCancel, false);
    }

    /**
     * Discovers blocking moves while exploring the reachable game states.
     *
     * @param game current game state
     * @param history current game history (current phase must be an ActionsPhase)
     * @param characters player characters defining the enumeration order
     * @param permutationIndex index of the permutation used for the enumeration
     * @param blockingMoves receives, for each Nemesis state, its first blocking action
     * @param exploredStates enumeration nodes already visited (one exploration only)
     * @param nemesisEvaluated Nemesis states already fully evaluated
     * @param losingStates memoization of the exhaustive proof, shareable between searches
     * @param shouldCancel cancellation signal
     * @param verifyResults if true, every reported result is cross-checked with an
     *                      unmemoized brute-force search; throws
     *                      {@link IllegalStateException} on any mismatch. Exponential
     *                      cost: intended for debug builds and tests only.
     * @throws IllegalArgumentException if the current phase is not an ActionsPhase
     * @throws InterruptedException if the search is interrupted or canceled
     */
    public static void solve(@NonNull Game game,
                             @NonNull GameHistory history,
                             @NonNull List<Character> characters,
                             long permutationIndex,
                             @NonNull Map<String, CharacterAction> blockingMoves,
                             @NonNull Set<String> exploredStates,
                             @NonNull Set<String> nemesisEvaluated,
                             @NonNull Set<String> losingStates,
                             @NonNull BooleanSupplier shouldCancel,
                             boolean verifyResults) throws InterruptedException {
        checkCancellation(shouldCancel);

        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisMovesUtils requires an ActionsPhase");
        }

        SearchContext ctx = new SearchContext(
                game, history, (ActionsPhase) phase, blockingMoves,
                exploredStates, nemesisEvaluated, losingStates, shouldCancel, verifyResults
        );

        List<Character> permutation = CharactersPermutationUtils.getPermutation(characters, permutationIndex);

        try {
            search(ctx, new ArrayList<>(permutation));
        } catch (InterruptedException e) {
            // Nodes are marked before being fully visited: this set is no longer reliable.
            exploredStates.clear();
            throw e;
        }
    }

    private static void search(@NonNull SearchContext searchContext,
                               @NonNull List<Character> remainingCharacters) throws InterruptedException {
        checkCancellation(searchContext.shouldCancel);

        // The hash represents the state before the next action.
        String stateHash = GameStateHashUtils.createHash(searchContext.game, searchContext.history);

        if (!searchContext.exploredStates.add(createExplorationKey(stateHash, remainingCharacters))) {
            return;
        }

        // Nothing to explore once the game is decided.
        if (GameQuery.getWinnerTeam(searchContext.game, searchContext.actionsPhase.getTurnTeamColor()) != null) {
            return;
        }

        List<PlayableCharacter> playableCharacters =
                PlayabilityQuery.getPlayableCharacters(searchContext.game, searchContext.history);

        // A mandatory character always has priority over the requested order.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {
            exploreMandatoryCharacter(searchContext, remainingCharacters, playableCharacters.get(0), stateHash);
            return;
        }

        exploreNextPlayerCharacter(searchContext, remainingCharacters, playableCharacters);
    }

    private static void exploreMandatoryCharacter(@NonNull SearchContext searchContext,
                                                  @NonNull List<Character> remainingCharacters,
                                                  @NonNull PlayableCharacter playableCharacter,
                                                  @NonNull String stateHash) throws InterruptedException {
        Character character = playableCharacter.getCharacter();
        int characterIndex = remainingCharacters.indexOf(character);

        if (characterIndex != -1) {
            remainingCharacters.remove(characterIndex);
        }

        try {
            if (isNemesisCharacter(searchContext.actionsPhase, character)) {
                exploreNemesisActions(searchContext, remainingCharacters, playableCharacter, stateHash);
            } else {
                explorePlayerActions(searchContext, remainingCharacters, playableCharacter);
            }
        } finally {
            if (characterIndex != -1) {
                remainingCharacters.add(characterIndex, character);
            }
        }
    }

    /**
     * Follows the requested order: the first remaining character that is playable
     * in the current state is explored.
     *
     * <p>Characters that are not playable <i>right now</i> stay in the list, so they
     * remain candidates in the following states (they may become playable after
     * another character acted). Only the character being explored is removed.</p>
     */
    private static void exploreNextPlayerCharacter(@NonNull SearchContext searchContext,
                                                   @NonNull List<Character> remainingCharacters,
                                                   @NonNull List<PlayableCharacter> playableCharacters) throws InterruptedException {
        for (int i = 0; i < remainingCharacters.size(); i++) {
            Character character = remainingCharacters.get(i);

            PlayableCharacter playableCharacter = GameSearchUtils.findPlayableCharacterById(playableCharacters, character);

            if (playableCharacter == null) {
                continue;
            }

            remainingCharacters.remove(i);
            try {
                explorePlayerActions(searchContext, remainingCharacters, playableCharacter);
            } finally {
                remainingCharacters.add(i, character);
            }
            return;
        }
    }

    /**
     * Explores the actions of a player character, including the option to skip
     * an optional character.
     */
    private static void explorePlayerActions(@NonNull SearchContext ctx,
                                             @NonNull List<Character> remainingCharacters,
                                             @NonNull PlayableCharacter playableCharacter) throws InterruptedException {
        if (!playableCharacter.isMandatory()) {
            search(ctx, remainingCharacters);
        }

        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                ctx.game, ctx.history, playableCharacter.getCharacter()
        );

        for (CharacterAction action : actions) {
            checkCancellation(ctx.shouldCancel);
            applyThenSearch(ctx, action, remainingCharacters);
        }
    }

    /**
     * Evaluates the current Nemesis state (once), then explores every Nemesis
     * action to discover the following states.
     */
    private static void exploreNemesisActions(@NonNull SearchContext searchContext,
                                              @NonNull List<Character> remainingCharacters,
                                              @NonNull PlayableCharacter playableCharacter,
                                              @NonNull String stateHash) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                searchContext.game, searchContext.history, playableCharacter.getCharacter()
        );

        if (!searchContext.nemesisEvaluated.contains(stateHash)) {
            evaluateNemesisState(searchContext, stateHash, actions);
        }

        for (CharacterAction action : actions) {
            checkCancellation(searchContext.shouldCancel);
            applyThenSearch(searchContext, action, remainingCharacters);
        }
    }

    private static void applyThenSearch(@NonNull SearchContext searchContext,
                                        @NonNull CharacterAction action,
                                        @NonNull List<Character> remainingCharacters) throws InterruptedException {
        GameActionHandler handler = GameActionHandlerFactory.create(searchContext.game, action);

        handler.doAction();
        searchContext.actionsPhase.getActions().add(action);

        try {
            search(searchContext, remainingCharacters);
        } finally {
            searchContext.actionsPhase.getActions().remove(searchContext.actionsPhase.getActions().size() - 1);
            handler.undoAction();
        }
    }


    /**
     * Looks for a blocking action in the current Nemesis state.
     *
     * <p>A blocking action is only reported when another action of the same state
     * leaves the player a winning continuation. Otherwise, the state is either lost
     * or won for the player whatever the Nemesis does, so there is nothing
     * interesting to report.</p>
     */
    private static void evaluateNemesisState(@NonNull SearchContext searchContext,
                                             @NonNull String stateHash,
                                             @NonNull List<CharacterAction> actions) throws InterruptedException {
        CharacterAction firstBlockingAction = null;
        CharacterAction firstWinningAlternative = null;

        for (CharacterAction action : actions) {
            checkCancellation(searchContext.shouldCancel);

            if (NemesisBlockerUtils.canBlock(
                    searchContext.game, searchContext.history, action,
                    searchContext.losingStates, searchContext.shouldCancel)) {
                if (firstBlockingAction == null) {
                    firstBlockingAction = action;
                }
            } else if (firstWinningAlternative == null) {
                firstWinningAlternative = action;
            }

            if (firstBlockingAction != null && firstWinningAlternative != null) {
                break;
            }
        }

        if (firstBlockingAction != null && firstWinningAlternative != null) {
            if (searchContext.verifyResults) {
                verifyBlockingMove(searchContext, stateHash, firstBlockingAction, firstWinningAlternative);
            }
            searchContext.blockingMoves.putIfAbsent(stateHash, firstBlockingAction);
        }

        // Only marked after a complete evaluation: an interruption leaves no entry.
        searchContext.nemesisEvaluated.add(stateHash);
    }

    /**
     * Cross-checks a result against a brute-force search that shares nothing with
     * {@link NemesisBlockerUtils} (no {@code losingStates}, no state hash).
     * A failure points to an incomplete {@code createHash}, a wrong
     * {@code getPlayableCharacters}, or a bug in the proof.
     */
    private static void verifyBlockingMove(@NonNull SearchContext searchContext,
                                           @NonNull String stateHash,
                                           @NonNull CharacterAction blockingAction,
                                           @NonNull CharacterAction winningAlternative) throws InterruptedException {
        if (applyThenBruteForce(searchContext, blockingAction)) {
            throw new IllegalStateException(
                    "Invalid blocking move: the player can still win after " + blockingAction
                            + " (state " + stateHash + ")");
        }
        if (!applyThenBruteForce(searchContext, winningAlternative)) {
            throw new IllegalStateException(
                    "Invalid alternative: no player victory found after " + winningAlternative
                            + " although the proof reported one (state " + stateHash + ")");
        }
    }

    private static boolean applyThenBruteForce(@NonNull SearchContext searchContext,
                                               @NonNull CharacterAction action) throws InterruptedException {
        GameActionHandler handler = GameActionHandlerFactory.create(searchContext.game, action);

        handler.doAction();
        searchContext.actionsPhase.getActions().add(action);

        try {
            return bruteForcePlayerCanWin(searchContext);
        } finally {
            searchContext.actionsPhase.getActions().remove(searchContext.actionsPhase.getActions().size() - 1);
            handler.undoAction();
        }
    }

    /**
     * Unmemoized AND/OR search: the Nemesis picks (AND), the player picks any
     * playable character and any action (OR).
     */
    private static boolean bruteForcePlayerCanWin(@NonNull SearchContext searchContext) throws InterruptedException {
        checkCancellation(searchContext.shouldCancel);

        TeamColor playerTeam = searchContext.actionsPhase.getTurnTeamColor();
        TeamColor winner = GameQuery.getWinnerTeam(searchContext.game, playerTeam);
        if (winner != null) {
            return winner == playerTeam;
        }

        List<PlayableCharacter> playable = PlayabilityQuery.getPlayableCharacters(searchContext.game, searchContext.history);

        if (playable.size() == 1 && playable.get(0).isMandatory()
                && isNemesisCharacter(searchContext.actionsPhase, playable.get(0).getCharacter())) {
            List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                    searchContext.game, searchContext.history, playable.get(0).getCharacter()
            );
            if (actions.isEmpty()) {
                return false;
            }
            for (CharacterAction action : actions) {
                if (!applyThenBruteForce(searchContext, action)) {
                    return false;
                }
            }
            return true;
        }

        for (PlayableCharacter playableCharacter : playable) {
            if (isNemesisCharacter(searchContext.actionsPhase, playableCharacter.getCharacter())) {
                continue;
            }
            for (CharacterAction action : GameSearchUtils.enumerateActions(
                    searchContext.game, searchContext.history, playableCharacter.getCharacter())) {
                if (applyThenBruteForce(searchContext, action)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNemesisCharacter(@NonNull ActionsPhase actionsPhase,
                                              @NonNull Character character) {
        return character.getTeamColor() != actionsPhase.getTurnTeamColor();
    }

    private static void checkCancellation(@NonNull BooleanSupplier shouldCancel) throws InterruptedException {
        if (Thread.currentThread().isInterrupted() || shouldCancel.getAsBoolean()) {
            throw new InterruptedException("Nemesis moves search cancelled");
        }
    }

    @NonNull
    private static String createExplorationKey(@NonNull String stateHash,
                                               @NonNull List<Character> remainingCharacters) {
        return stateHash + "|" +
                remainingCharacters.stream()
                        .map(character -> character.getId().toString())
                        .collect(Collectors.joining(","));
    }

    /**
     * Immutable bundle of the arguments that stay the same during a whole search.
     * Only used to keep the private method signatures short.
     */
    private static final class SearchContext {
        final Game game;
        final GameHistory history;
        final ActionsPhase actionsPhase;
        final Map<String, CharacterAction> blockingMoves;
        final Set<String> exploredStates;
        final Set<String> nemesisEvaluated;
        final Set<String> losingStates;
        final BooleanSupplier shouldCancel;
        final boolean verifyResults;

        SearchContext(@NonNull Game game,
                      @NonNull GameHistory history,
                      @NonNull ActionsPhase actionsPhase,
                      @NonNull Map<String, CharacterAction> blockingMoves,
                      @NonNull Set<String> exploredStates,
                      @NonNull Set<String> nemesisEvaluated,
                      @NonNull Set<String> losingStates,
                      @NonNull BooleanSupplier shouldCancel,
                      boolean verifyResults) {
            this.game = game;
            this.history = history;
            this.actionsPhase = actionsPhase;
            this.blockingMoves = blockingMoves;
            this.exploredStates = exploredStates;
            this.nemesisEvaluated = nemesisEvaluated;
            this.losingStates = losingStates;
            this.shouldCancel = shouldCancel;
            this.verifyResults = verifyResults;
        }
    }
}