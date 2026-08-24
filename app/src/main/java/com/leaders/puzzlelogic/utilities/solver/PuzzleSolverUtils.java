package com.leaders.puzzlelogic.utilities.solver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.CharacterActionResolverFactory;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.GameQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Utility class used to find winning action sequences during an ActionsPhase.
 */
public final class PuzzleSolverUtils {
    private static final int MAX_RESOLVER_INTERACTION_DEPTH = 2;

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
                getPermutation(characters, permutationIndex),
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
            PlayableCharacter playableCharacter = findPlayableCharacterById(playableCharacters, character);

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

        List<CharacterAction> actions = enumerateActions(game, history, playableCharacter.getCharacter());

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
        List<CharacterAction> actions = enumerateActions(game, history, playableCharacter.getCharacter());

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
     * Returns the winning continuations common to both collections.
     *
     * @param first the first collection of continuations
     * @param second the second collection of continuations
     * @return the continuations present in both collections
     */
    @NonNull
    private static List<List<CharacterAction>> intersectSolutions(@NonNull List<List<CharacterAction>> first,
                                                                  @NonNull List<List<CharacterAction>> second) {
        List<List<CharacterAction>> result = new ArrayList<>();

        for (List<CharacterAction> solution : first) {
            if (containsSolution(second, solution)) {
                result.add(new ArrayList<>(solution));
            }
        }

        return result;
    }

    /**
     * Checks whether the given collection contains the specified continuation.
     *
     * @param solutions the collection of continuations to search
     * @param candidate the continuation to look for
     * @return {@code true} if the candidate is present in the collection;
     *         {@code false} otherwise
     */
    private static boolean containsSolution(@NonNull List<List<CharacterAction>> solutions,
                                            @NonNull List<CharacterAction> candidate) {
        for (List<CharacterAction> solution : solutions) {
            if (solution.equals(candidate)) {
                return true;
            }
        }

        return false;
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

    /**
     * Finds a playable character by its character identifier.
     *
     * @param playableCharacters the playable characters to search
     * @param character the character to find
     * @return the corresponding playable character, or {@code null} if none
     *         matches the specified character
     */
    @Nullable
    private static PlayableCharacter findPlayableCharacterById(@NonNull List<PlayableCharacter> playableCharacters,
                                                               @NonNull Character character) {
        for (PlayableCharacter playableCharacter : playableCharacters) {
            if (character.getId() == playableCharacter.getCharacter().getId()) {
                return playableCharacter;
            }
        }

        return null;
    }

    /**
     * Returns all legal actions available to the specified character in the current game state.
     *
     * @param game the current game state
     * @param history the game history used to determine legal actions
     * @param character the character whose actions are enumerated
     * @return the list of legal actions available to the character
     */
    @NonNull
    private static List<CharacterAction> enumerateActions(@NonNull Game game,
                                                          @NonNull GameHistory history,
                                                          @NonNull Character character) {
        CharacterActionResolver resolver =
                CharacterActionResolverFactory.create(game, history, character);

        CharacterActionBuilder builder =
                new CharacterActionBuilder(character, new ArrayList<>(), new ArrayList<>());

        List<CharacterAction> actions = new ArrayList<>();

        enumerateResolverState(resolver, builder, actions, 0);

        return actions;
    }


    /**
     * Enumerates all legal action variants resulting from the interaction
     * sequence exposed by the resolver.
     *
     * @param resolver the resolver determining the next interaction and feedback
     * @param builder the current action being constructed
     * @param actions the collection receiving the completed actions
     */
    private static void enumerateResolverState(@NonNull CharacterActionResolver resolver,
                                               @NonNull CharacterActionBuilder builder,
                                               @NonNull List<CharacterAction> actions,
                                               int depth) {
        if (depth > MAX_RESOLVER_INTERACTION_DEPTH) {
            throw new IllegalStateException(
                    "Resolver interaction flow did not terminate: "
                            + resolver.getClass().getSimpleName()
            );
        }

        InteractionRequest request = resolver.getNextInteraction(builder);

        if (request == null) {
            actions.add(resolver.buildAction(builder));
            return;
        }

        for (InteractionTarget target : request.getLegalTargets()) {
            InteractionResult result =
                    new InteractionResult(
                            target.getCategory().getResultType(),
                            request.getContext(),
                            target
                    );

            CharacterActionBuilder next = new CharacterActionBuilder(builder);

            next.addResult(result);

            InteractionFeedback feedback = resolver.getNextFeedback(next);

            if (feedback != null) {
                next.addFeedback(feedback);
            }

            enumerateResolverState(resolver, next, actions, depth + 1);
        }
    }

    /**
     * Returns the total number of permutations of {@code characters}.
     *
     * @param characters the elements to permute
     * @return the number of permutations, {@code characters.size()!}
     * @throws IllegalArgumentException if the number of permutations exceeds
     *                                  {@link Long#MAX_VALUE}
     */
    public static long getPermutationCount(@NonNull List<Character> characters) {
        return factorial(characters.size());
    }

    /**
     * Returns the permutation of {@code characters} identified by {@code index}
     * in lexicographic order.
     *
     * @param characters the elements to permute
     * @param index the zero-based permutation index, in {@code [0, n!)}
     * @return the permutation corresponding to {@code index}
     * @throws IllegalArgumentException if {@code index} is outside the valid range
     */
    @NonNull
    private static List<Character> getPermutation(@NonNull List<Character> characters,
                                                  long index) {
        int size = characters.size();

        if (index < 0 || index >= factorial(size)) {
            throw new IllegalArgumentException("Invalid permutation index: " + index);
        }

        List<Character> remaining = new ArrayList<>(characters);
        List<Character> permutation = new ArrayList<>(size);

        for (int position = size - 1; position >= 0; position--) {
            long blockSize = factorial(position);

            int selectedIndex = (int) (index / blockSize);
            index %= blockSize;

            permutation.add(remaining.remove(selectedIndex));
        }

        return permutation;
    }

    /**
     * Returns the factorial of {@code value}.
     *
     * @param value a non-negative integer
     * @return {@code value!}
     * @throws IllegalArgumentException if the result exceeds {@link Long#MAX_VALUE}
     */
    private static long factorial(int value) {
        long result = 1;

        for (int i = 2; i <= value; i++) {
            if (Long.MAX_VALUE / i < result) {
                throw new IllegalArgumentException("Factorial exceeds long range: " + value);
            }

            result *= i;
        }

        return result;
    }
}