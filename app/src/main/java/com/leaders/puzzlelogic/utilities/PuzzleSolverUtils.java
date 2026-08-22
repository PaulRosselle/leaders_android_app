package com.leaders.puzzlelogic.utilities;

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
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.GameQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to find winning action sequences during an ActionsPhase.
 */
public class PuzzleSolverUtils {
    private PuzzleSolverUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Finds all action sequences leading to a victory for the current player.
     *
     * @param game current game state
     * @param history current game history
     * @return all winning action sequences
     * @throws IllegalArgumentException if the current phase is not an ActionsPhase
     */
    @NonNull
    public static List<List<CharacterAction>> solve(@NonNull Game game, @NonNull GameHistory history) {
        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("Solver requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        return search(game, history, actionsPhase, new ArrayList<>());
    }


    /**
     * Recursively explores the game tree using AND/OR semantics.
     *
     * <p>Player characters are OR nodes: one winning action is sufficient.
     * Mandatory opponent characters are AND nodes: every possible action must
     * preserve a winning continuation.</p>
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored, in order
     * @return winning continuations from the current state
     */
    @NonNull
    private static List<List<CharacterAction>> search(@NonNull Game game, @NonNull GameHistory history,
                                                      @NonNull ActionsPhase actionsPhase,
                                                      @NonNull List<Character> remainingCharacters) {
        List<PlayableCharacter> playableCharacters =
                PlayabilityQuery.getPlayableCharacters(game, history);

        // A mandatory character always has priority over the requested order.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {
            PlayableCharacter playableCharacter = playableCharacters.get(0);
            Character character = playableCharacter.getCharacter();

            int characterIdx = remainingCharacters.indexOf(character);
            if (characterIdx != -1) {
                remainingCharacters.remove(characterIdx);
            }

            try {
                // Mandatory characters can be from the other team.
                // Opponent characters have a different "behavior" : they will
                // actively try to block out existing solution if they can
                if (isOpponentCharacter(actionsPhase, character)) {
                    return exploreOpponentCharacter(game, history, actionsPhase,
                            remainingCharacters, playableCharacter);
                }

                return explorePlayerCharacter(game, history, actionsPhase,
                        remainingCharacters, playableCharacter);
            } finally {
                if (characterIdx != -1) {
                    remainingCharacters.add(characterIdx, character);
                }
            }
        }

        // The requested character order is authoritative without a mandatory character.
        if (remainingCharacters.isEmpty()) {
            return new ArrayList<>();
        }

        Character character = remainingCharacters.get(0);

        PlayableCharacter playableCharacter = findPlayableCharacter(playableCharacters, character);
        if (playableCharacter == null) {
            return new ArrayList<>();
        }

        remainingCharacters.remove(0);

        try {
            return explorePlayerCharacter(game, history, actionsPhase,
                    remainingCharacters, playableCharacter);
        } finally {
            remainingCharacters.add(0, character);
        }
    }


    /**
     * Explores a player's character as an OR node.
     *
     * <p>The character may either perform no action, when allowed, or perform
     * any of its legal actions. A single winning branch is sufficient.</p>
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored
     * @param playableCharacter character whose actions are explored
     * @return winning continuations
     */
    @NonNull
    private static List<List<CharacterAction>> explorePlayerCharacter(@NonNull Game game,
                                                                      @NonNull GameHistory history,
                                                                      @NonNull ActionsPhase actionsPhase,
                                                                      @NonNull List<Character> remainingCharacters,
                                                                      @NonNull PlayableCharacter playableCharacter) {
        List<List<CharacterAction>> solutions = new ArrayList<>();

        // A non-mandatory character may choose not to act.
        if (!playableCharacter.isMandatory()) {
            solutions.addAll(search(game, history, actionsPhase, remainingCharacters));
        }

        List<CharacterAction> actions = enumerateActions(game, history, playableCharacter.getCharacter());

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                List<List<CharacterAction>> continuations =
                        evaluateAction(game, history, actionsPhase, remainingCharacters);

                prependAction(action, continuations, solutions);
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        return solutions;
    }


    /**
     * Explores a mandatory opponent character as an AND node.
     *
     * <p>Every possible action of the opponent must leave at least one common
     * winning continuation for the current player.</p>
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored
     * @param playableCharacter mandatory opponent character
     * @return winning continuations common to every opponent action
     */
    @NonNull
    private static List<List<CharacterAction>> exploreOpponentCharacter(@NonNull Game game,
                                                                        @NonNull GameHistory history,
                                                                        @NonNull ActionsPhase actionsPhase,
                                                                        @NonNull List<Character> remainingCharacters,
                                                                        @NonNull PlayableCharacter playableCharacter) {
        List<CharacterAction> actions = enumerateActions(game, history, playableCharacter.getCharacter());

        List<List<CharacterAction>> solutions = null;

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                List<List<CharacterAction>> branchSolutions =
                        evaluateAction(game, history, actionsPhase, remainingCharacters);

                if (solutions == null) {
                    solutions = new ArrayList<>(branchSolutions);
                } else {
                    solutions = intersectSolutions(solutions, branchSolutions);
                }

                // No continuation is valid for every possible opponent action.
                if (solutions.isEmpty()) {
                    return solutions;
                }
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        if (solutions == null) {
            return new ArrayList<>();
        }

        return solutions;
    }


    /**
     * Evaluates the state resulting from an action.
     *
     * <p>A victory for the current player produces an empty continuation.
     * A victory for the opponent invalidates the branch. Otherwise the search
     * continues with the next character.</p>
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored
     * @return winning continuations
     */
    @NonNull
    private static List<List<CharacterAction>> evaluateAction(@NonNull Game game,
                                                              @NonNull GameHistory history,
                                                              @NonNull ActionsPhase actionsPhase,
                                                              @NonNull List<Character> remainingCharacters) {
        List<List<CharacterAction>> solutions = new ArrayList<>();

        TeamColor playerTeamColor = actionsPhase.getTurnTeamColor();
        TeamColor winnerTeamColor = GameQuery.getWinnerTeam(game, playerTeamColor);

        if (winnerTeamColor == playerTeamColor) {
            solutions.add(new ArrayList<>());
            return solutions;
        }

        if (winnerTeamColor != null) {
            return solutions;
        }

        return search(game, history, actionsPhase, remainingCharacters);
    }


    /**
     * Adds an action at the beginning of every continuation.
     *
     * @param action action to prepend
     * @param continuations winning continuations
     * @param solutions destination collection
     */
    private static void prependAction(@NonNull CharacterAction action,
                                      @NonNull List<List<CharacterAction>> continuations,
                                      @NonNull List<List<CharacterAction>> solutions) {
        for (List<CharacterAction> continuation : continuations) {
            List<CharacterAction> solution = new ArrayList<>();

            solution.add(action);
            solution.addAll(continuation);

            solutions.add(solution);
        }
    }


    /**
     * Returns the intersection of two sets of winning continuations.
     *
     * @param first first set of continuations
     * @param second second set of continuations
     * @return continuations present in both sets
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
     * Checks whether a set of solutions contains a given continuation.
     *
     * @param solutions solutions to search
     * @param candidate continuation to find
     * @return {@code true} if the continuation is present
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
     * Returns whether a character belongs to the opposing team.
     *
     * @param actionsPhase current actions phase
     * @param character character to check
     * @return {@code true} if the character belongs to the opposing team
     */
    private static boolean isOpponentCharacter(@NonNull ActionsPhase actionsPhase,
                                               @NonNull Character character) {
        return character.getTeamColor() != actionsPhase.getTurnTeamColor();
    }


    /**
     * Finds a playable character by its character identifier.
     *
     * @param playableCharacters characters to search
     * @param character character to find
     * @return matching playable character, or {@code null} if not found
     */
    @Nullable
    private static PlayableCharacter findPlayableCharacter(@NonNull List<PlayableCharacter> playableCharacters,
                                                           @NonNull Character character) {
        for (PlayableCharacter playableCharacter : playableCharacters) {
            if (character.getId() == playableCharacter.getCharacter().getId()) {
                return playableCharacter;
            }
        }

        return null;
    }


    /**
     * Enumerates all legal actions available to a character.
     *
     * @param game current game state
     * @param history current game history
     * @param character character whose actions are enumerated
     * @return all legal character actions
     */
    @NonNull
    private static List<CharacterAction> enumerateActions(@NonNull Game game,
                                                          @NonNull GameHistory history,
                                                          @NonNull Character character) {
        CharacterActionResolver resolver = CharacterActionResolverFactory.create(game, history, character);

        CharacterActionBuilder builder = new CharacterActionBuilder(character, new ArrayList<>(), new ArrayList<>());

        List<CharacterAction> actions = new ArrayList<>();

        enumerateResolverState(resolver, builder, actions);

        return actions;
    }


    /**
     * Recursively enumerates all possible interaction paths for an action.
     *
     * @param resolver resolver used to determine available interactions
     * @param builder current action builder state
     * @param actions collection of completed actions
     */
    private static void enumerateResolverState(@NonNull CharacterActionResolver resolver,
                                               @NonNull CharacterActionBuilder builder,
                                               @NonNull List<CharacterAction> actions) {
        InteractionRequest request = resolver.getNextInteraction(builder);

        if (request == null) {
            actions.add(resolver.buildAction(builder));
            return;
        }

        for (InteractionTarget target : request.getLegalTargets()) {
            InteractionResult result = new InteractionResult(
                    getResultTypeFromTarget(target.getCategory()),
                    request.getContext(),
                    target
            );

            CharacterActionBuilder next = new CharacterActionBuilder(builder);
            next.addResult(result);

            InteractionFeedback feedback = resolver.getNextFeedback(next);
            if (feedback != null) {
                next.addFeedback(feedback);
            }

            enumerateResolverState(resolver, next, actions);
        }
    }


    /**
     * Returns the interaction result type corresponding to a target category.
     *
     * @param targetCategory target category
     * @return corresponding interaction result type
     * @throws IllegalArgumentException if the category is not supported
     */
    @NonNull
    private static InteractionResultType getResultTypeFromTarget(@NonNull TargetCategory targetCategory) {
        switch (targetCategory) {
            case PlayableCharacter:
                return InteractionResultType.PlayableCharacterChosen;

            case RecruitmentCard:
            case BanishmentCard:
                return InteractionResultType.SelectableCharacterCardChosen;

            case RecruitmentDestination:
            case MovementDestination:
            case ActiveAbilityDestination:
            case ActiveAbilityTargetPosition:
                return InteractionResultType.PositionChosen;

            default:
                throw new IllegalArgumentException("Cannot find a result type matching target category:" + targetCategory);
        }
    }
}