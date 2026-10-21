package com.leaders.puzzlelogic.utilities.solver;

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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * Utility class used to find winning action sequences during an ActionsPhase.
 *
 * <h2>Memoization</h2>
 * <p>{@code deadEndStates} contains the exploration nodes (state hash + remaining
 * characters) from which no solution exists. This is a pure fact about the search
 * tree: it is valid for every permutation and can safely be shared between threads
 * (use a concurrent set). Entries are only added once a node has been completely
 * explored, so an interruption never leaves a wrong entry.</p>
 *
 * <p>Only dead ends are memoized: nodes that do lead to solutions are always
 * re-explored, so the set of solutions found is exactly the same as without
 * memoization.</p>
 */
public final class PuzzleSolverUtils {
    public static final int MAX_PLAYER_CHARACTER_COUNT = 10;

    private PuzzleSolverUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Finds winning action sequences and publishes them as they are discovered.
     *
     * @param game current game state
     * @param history current game history
     * @param solutions queue receiving winning action sequences
     * @param deadEndStates memoization of the exploration nodes without any solution.
     *                      Can be shared between several searches on the same puzzle
     *                      (must then be thread-safe, e.g. {@code ConcurrentHashMap.newKeySet()}).
     * @throws IllegalArgumentException if the current phase is not an ActionsPhase
     * @throws InterruptedException if the current thread is interrupted
     */
    public static void solve(@NonNull Game game,
                             @NonNull GameHistory history,
                             @NonNull List<Character> characters,
                             long permutationIndex,
                             @NonNull BlockingQueue<List<CharacterAction>> solutions,
                             @NonNull Set<String> deadEndStates) throws InterruptedException {
        IPhase phase = GameHistoryQuery.findCurrentPhase(history);

        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("Solver requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        search(game, history, actionsPhase,
                CharactersPermutationUtils.getPermutation(characters, permutationIndex),
                new QueueSolutionCollector(solutions),
                deadEndStates
        );
    }

    /**
     * Recursively explores the game tree using AND/OR semantics.
     *
     * <p>The destination of discovered solutions is provided by the collector,
     * allowing the same search algorithm to be used both for normal exploration
     * and for local exploration below an AND node.</p>
     *
     * @return {@code true} if at least one solution was published to the collector
     *         from this node, {@code false} if the node is a dead end
     * @throws InterruptedException if the current thread is interrupted
     */
    private static boolean search(@NonNull Game game,
                                  @NonNull GameHistory history,
                                  @NonNull ActionsPhase actionsPhase,
                                  @NonNull List<Character> remainingCharacters,
                                  @NonNull ISolutionCollector collector,
                                  @NonNull Set<String> deadEndStates) throws InterruptedException {
        // The hash represents the state before the next action.
        String nodeKey = createNodeKey(GameStateHashUtils.createHash(game, history), remainingCharacters);

        if (deadEndStates.contains(nodeKey)) {
            return false;
        }

        boolean solutionFound = false;

        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, history);

        // MANDATORY CHARACTER : always has priority over the requested order.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {
            PlayableCharacter playableCharacter = playableCharacters.get(0);
            Character character = playableCharacter.getCharacter();

            int characterIdx = remainingCharacters.indexOf(character);
            if (characterIdx != -1) {
                remainingCharacters.remove(characterIdx);
            }

            try {
                if (isOpponentCharacter(actionsPhase, character)) {
                    solutionFound = exploreOpponentCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector, deadEndStates);
                } else {
                    solutionFound = explorePlayerCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector, deadEndStates);
                }
            } finally {
                if (characterIdx != -1) {
                    remainingCharacters.add(characterIdx, character);
                }
            }

            // PLAYABLE CHARACTER : we play the remaining characters in order.
        } else if (!remainingCharacters.isEmpty()) {
            Character character = remainingCharacters.get(0);
            PlayableCharacter playableCharacter = GameSearchUtils.findPlayableCharacterById(
                    playableCharacters, character
            );

            if (playableCharacter != null) {
                remainingCharacters.remove(0);

                try {
                    solutionFound = explorePlayerCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector, deadEndStates);
                } finally {
                    remainingCharacters.add(0, character);
                }
            }
        }

        // Only reached after a complete exploration: an interruption leaves no entry.
        if (!solutionFound) {
            deadEndStates.add(nodeKey);
        }

        return solutionFound;
    }

    /**
     * Explores the possible actions of a player's character and collects each
     * successful continuation.
     *
     * <p>If the character is not mandatory, the option to take no action is also
     * explored. The game state is restored after each explored action.</p>
     *
     * @return {@code true} if at least one solution was published
     * @throws InterruptedException if the exploration is interrupted
     */
    private static boolean explorePlayerCharacterActions(@NonNull Game game,
                                                         @NonNull GameHistory history,
                                                         @NonNull ActionsPhase actionsPhase,
                                                         @NonNull List<Character> remainingCharacters,
                                                         @NonNull PlayableCharacter playableCharacter,
                                                         @NonNull ISolutionCollector collector,
                                                         @NonNull Set<String> deadEndStates) throws InterruptedException {
        boolean solutionFound = false;

        if (!playableCharacter.isMandatory()) {
            solutionFound |= search(game, history, actionsPhase, remainingCharacters, collector, deadEndStates);
        }

        List<CharacterAction> actions = GameSearchUtils.enumerateActions(game, history, playableCharacter.getCharacter());

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                // Not short-circuited: every action must be explored to collect all solutions.
                solutionFound |= evaluateAction(game, history, actionsPhase, remainingCharacters, collector, deadEndStates);
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        return solutionFound;
    }

    /**
     * Explores all possible actions of a mandatory opponent character.
     *
     * <p>Every possible action of the opponent must leave at least one winning
     * continuation for the current player. If one opponent action does not allow
     * any solution, the whole branch is invalidated. Otherwise, all solutions
     * found across the opponent actions are collected.</p>
     *
     * @return {@code true} if the AND node is valid and solutions were published
     * @throws InterruptedException if the exploration is interrupted
     */
    private static boolean exploreOpponentCharacterActions(@NonNull Game game,
                                                           @NonNull GameHistory history,
                                                           @NonNull ActionsPhase actionsPhase,
                                                           @NonNull List<Character> remainingCharacters,
                                                           @NonNull PlayableCharacter playableCharacter,
                                                           @NonNull ISolutionCollector collector,
                                                           @NonNull Set<String> deadEndStates) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(game, history, playableCharacter.getCharacter());

        List<List<CharacterAction>> solutions = new ArrayList<>();

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                ListSolutionCollector branchCollector = new ListSolutionCollector();

                boolean solutionFound = search(game, history, actionsPhase,
                        remainingCharacters, branchCollector, deadEndStates);

                // If this opponent action prevents every possible solution, the AND node is invalid
                if (!solutionFound) {
                    return false;
                }

                // This opponent action allows at least one solution
                solutions.addAll(branchCollector.getSolutions());

            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        for (List<CharacterAction> solution : solutions) {
            collector.add(solution);
        }

        return !solutions.isEmpty();
    }

    /**
     * Evaluates the game state after an action and collects the resulting action
     * sequence if it leads to a victory for the current team.
     *
     * <p>If the opposing team has won, the current branch is discarded. If the
     * game is not yet decided, exploration continues with the remaining
     * characters.</p>
     *
     * @return {@code true} if at least one solution was published
     * @throws InterruptedException if the exploration is interrupted
     */
    private static boolean evaluateAction(@NonNull Game game,
                                          @NonNull GameHistory history,
                                          @NonNull ActionsPhase actionsPhase,
                                          @NonNull List<Character> remainingCharacters,
                                          @NonNull ISolutionCollector collector,
                                          @NonNull Set<String> deadEndStates) throws InterruptedException {
        TeamColor playerTeamColor = actionsPhase.getTurnTeamColor();

        TeamColor winnerTeamColor = GameQuery.getWinnerTeam(game, playerTeamColor);

        if (winnerTeamColor == playerTeamColor) {
            collector.add(new ArrayList<>(actionsPhase.getCharacterActions()));
            return true;
        }

        if (winnerTeamColor != null) {
            return false;
        }

        return search(game, history, actionsPhase, remainingCharacters, collector, deadEndStates);
    }

    /**
     * Determines whether the specified character belongs to the opposing team
     * for the current actions phase.
     */
    private static boolean isOpponentCharacter(@NonNull ActionsPhase actionsPhase,
                                               @NonNull Character character) {
        return character.getTeamColor() != actionsPhase.getTurnTeamColor();
    }

    /**
     * Builds the memoization key of an exploration node: the exploration below a
     * node only depends on the game state and on the characters left to play, in order.
     */
    @NonNull
    private static String createNodeKey(@NonNull String stateHash,
                                        @NonNull List<Character> remainingCharacters) {
        return stateHash + "|" +
                remainingCharacters.stream()
                        .map(character -> character.getId().toString())
                        .collect(Collectors.joining(","));
    }
}