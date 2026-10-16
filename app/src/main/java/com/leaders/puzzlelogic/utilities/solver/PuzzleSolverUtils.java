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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Utility class used to find winning action sequences during an ActionsPhase.
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
     * @throws IllegalArgumentException if the current phase is not an ActionsPhase
     * @throws InterruptedException if the current thread is interrupted
     */
    public static void solve(@NonNull Game game,
                             @NonNull GameHistory history,
                             @NonNull List<Character> characters,
                             long permutationIndex,
                             @NonNull BlockingQueue<List<CharacterAction>> solutions) throws InterruptedException {
        IPhase phase = GameHistoryQuery.findCurrentPhase(history);

        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("Solver requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        search(game, history, actionsPhase,
                CharactersPermutationUtils.getPermutation(characters, permutationIndex),
                new QueueSolutionCollector(solutions)
        );
    }

    /**
     * Recursively explores the game tree using AND/OR semantics.
     *
     * <p>The destination of discovered solutions is provided by the collector,
     * allowing the same search algorithm to be used both for normal exploration
     * and for local exploration below an AND node.</p>
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored, in order
     * @param collector destination for discovered solutions
     * @throws InterruptedException if the current thread is interrupted
     */
    private static void search(@NonNull Game game,
                               @NonNull GameHistory history,
                               @NonNull ActionsPhase actionsPhase,
                               @NonNull List<Character> remainingCharacters,
                               @NonNull ISolutionCollector collector) throws InterruptedException {
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
                    exploreOpponentCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector);
                } else {
                    explorePlayerCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector);
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
                    explorePlayerCharacterActions(game, history, actionsPhase,
                            remainingCharacters, playableCharacter, collector);
                } finally {
                    remainingCharacters.add(0, character);
                }
            }
        }
    }

    /**
     * Explores the possible actions of a player's character and collects each
     * successful continuation.
     *
     * <p>If the character is not mandatory, the option to take no action is also
     * explored. The game state is restored after each explored action.</p>
     *
     * @param game the game being explored
     * @param history the game history used to evaluate possible continuations
     * @param actionsPhase the current actions phase
     * @param remainingCharacters the characters remaining to be explored
     * @param playableCharacter the character whose actions are being explored
     * @param collector the collector receiving successful continuations
     * @throws InterruptedException if the exploration is interrupted
     */
    private static void explorePlayerCharacterActions(@NonNull Game game,
                                                      @NonNull GameHistory history,
                                                      @NonNull ActionsPhase actionsPhase,
                                                      @NonNull List<Character> remainingCharacters,
                                                      @NonNull PlayableCharacter playableCharacter,
                                                      @NonNull ISolutionCollector collector) throws InterruptedException {
        if (!playableCharacter.isMandatory()) {
            search(game, history, actionsPhase, remainingCharacters, collector);
        }

        List<CharacterAction> actions = GameSearchUtils.enumerateActions(game, history, playableCharacter.getCharacter());

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                evaluateAction(game, history, actionsPhase, remainingCharacters, collector);
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }
    }

    /**
     * Explores all possible actions of a mandatory opponent character.
     *
     * <p>Every possible action of the opponent must leave at least one winning
     * continuation for the current player. If one opponent action does not allow
     * any solution, the whole branch is invalidated. Otherwise, all solutions
     * found across the opponent actions are collected.</p>
     *
     * @param game the game being explored
     * @param history the game history used to evaluate possible continuations
     * @param actionsPhase the current actions phase
     * @param remainingCharacters the characters remaining to be explored
     * @param playableCharacter the opponent character whose actions are being explored
     * @param collector the collector receiving the valid solutions
     * @throws InterruptedException if the exploration is interrupted
     */
    private static void exploreOpponentCharacterActions(@NonNull Game game,
                                                        @NonNull GameHistory history,
                                                        @NonNull ActionsPhase actionsPhase,
                                                        @NonNull List<Character> remainingCharacters,
                                                        @NonNull PlayableCharacter playableCharacter,
                                                        @NonNull ISolutionCollector collector) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(game, history, playableCharacter.getCharacter());

        List<List<CharacterAction>> solutions = new ArrayList<>();

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                ListSolutionCollector branchCollector = new ListSolutionCollector();

                search(game, history, actionsPhase, remainingCharacters, branchCollector);

                // If this opponent action prevents every possible solution, the AND node is invalid
                if (branchCollector.getSolutions().isEmpty()) {
                    return;
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
    }

    /**
     * Evaluates the game state after an action and collects the resulting action
     * sequence if it leads to a victory for the current team.
     *
     * <p>If the opposing team has won, the current branch is discarded. If the
     * game is not yet decided, exploration continues with the remaining
     * characters.</p>
     *
     * @param game the game state resulting from the action
     * @param history the game history used during the exploration
     * @param actionsPhase the current actions phase
     * @param remainingCharacters the characters remaining to be explored
     * @param collector the collector receiving successful action sequences
     * @throws InterruptedException if the exploration is interrupted
     */
    private static void evaluateAction(@NonNull Game game,
                                       @NonNull GameHistory history,
                                       @NonNull ActionsPhase actionsPhase,
                                       @NonNull List<Character> remainingCharacters,
                                       @NonNull ISolutionCollector collector) throws InterruptedException {
        TeamColor playerTeamColor = actionsPhase.getTurnTeamColor();

        TeamColor winnerTeamColor = GameQuery.getWinnerTeam(game, playerTeamColor);

        if (winnerTeamColor == playerTeamColor) {
            collector.add(new ArrayList<>(actionsPhase.getCharacterActions()));
            return;
        }

        if (winnerTeamColor != null) {
            return;
        }

        search(game, history, actionsPhase, remainingCharacters, collector);
    }

    /**
     * Determines whether the specified character belongs to the opposing team
     * for the current actions phase.
     *
     * @param actionsPhase the current actions phase
     * @param character the character whose team is checked
     * @return {@code true} if the character belongs to the opposing team;
     *         {@code false} otherwise
     */
    private static boolean isOpponentCharacter(@NonNull ActionsPhase actionsPhase,
                                               @NonNull Character character) {
        return character.getTeamColor() != actionsPhase.getTurnTeamColor();
    }
}