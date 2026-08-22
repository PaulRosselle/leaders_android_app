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
    public List<List<CharacterAction>> solve(@NonNull Game game, @NonNull GameHistory history) {
        IPhase phase = GameHistoryQuery.findCurrentPhase(history);
        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("Solver requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        List<List<CharacterAction>> solutions = new ArrayList<>();

        search(game, history, actionsPhase, new ArrayList<>(), solutions);

        return solutions;
    }


    /**
     * Recursively explores possible character actions using depth-first search
     * and backtracking.
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored, in order
     * @param solutions collection of winning sequences
     */
    private static void search(@NonNull Game game, @NonNull GameHistory history,
                               @NonNull ActionsPhase actionsPhase,
                               @NonNull List<Character> remainingCharacters,
                               @NonNull List<List<CharacterAction>> solutions) {
        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, history);

        // A mandatory character always has priority over the requested order
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {

            PlayableCharacter mandatoryCharacter = playableCharacters.get(0);

            Character character = mandatoryCharacter.getCharacter();

            // A mandatory character may be played even without being part of the remainingCharacters
            int characterIdx = remainingCharacters.indexOf(character);
            if (characterIdx != -1) {
                remainingCharacters.remove(characterIdx);
            }

            try {
                exploreCharacterActions(game, history, actionsPhase, remainingCharacters, mandatoryCharacter, solutions);
            } finally {
                if (characterIdx != -1) {
                    remainingCharacters.add(characterIdx, character);
                }
            }

            return;
        }

        // No mandatory character: the requested order is authoritative.
        if (remainingCharacters.isEmpty()) {
            return;
        }

        Character character = remainingCharacters.get(0);

        PlayableCharacter playableCharacter = findPlayableCharacter(playableCharacters, character);
        if (playableCharacter == null) {
            return;
        }
        remainingCharacters.remove(0);

        try {
            // The character is allowed not to use their action.
            if (!playableCharacter.isMandatory()) {
                search(game, history, actionsPhase, remainingCharacters, solutions);
            }
            exploreCharacterActions(game, history, actionsPhase, remainingCharacters, playableCharacter, solutions);
        } finally {
            remainingCharacters.add(0, character);
        }
    }

    /**
     * Explores all legal actions of a character and continues the search.
     *
     * @param game current game state
     * @param history current game history
     * @param actionsPhase current actions phase
     * @param remainingCharacters characters remaining to be explored
     * @param playableCharacter character whose actions are explored
     * @param solutions collection of winning sequences
     */
    private static void exploreCharacterActions(@NonNull Game game,
                                                @NonNull GameHistory history,
                                                @NonNull ActionsPhase actionsPhase,
                                                @NonNull List<Character> remainingCharacters,
                                                @NonNull PlayableCharacter playableCharacter,
                                                @NonNull List<List<CharacterAction>> solutions) {
        Character character = playableCharacter.getCharacter();

        List<CharacterAction> actions = enumerateActions(game, history, character);

        for (CharacterAction action : actions) {
            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                TeamColor playerTeamColor = actionsPhase.getTurnTeamColor();
                TeamColor winnerTeamColor = GameQuery.getWinnerTeam(game, playerTeamColor);
                if (winnerTeamColor != null) {
                    // If the player has been beaten, we end the search on this branch here
                    if (winnerTeamColor == playerTeamColor) {
                        solutions.add(new ArrayList<>(actionsPhase.getCharacterActions()));
                    }
                } else {
                    search(game, history, actionsPhase, remainingCharacters, solutions);
                }

            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }
    }

    /**
     * Finds a playable character by its character identifier.
     *
     * @param playableCharacters characters to search
     * @param character character to find
     * @return the matching playable character, or {@code null} if not found
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
     * @param resolver resolver used to determine the available interactions
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
            case PlayableCharacter: return InteractionResultType.PlayableCharacterChosen;
            case RecruitmentCard:
            case BanishmentCard:
                return InteractionResultType.SelectableCharacterCardChosen;
            case RecruitmentDestination:
            case MovementDestination:
            case ActiveAbilityDestination:
            case ActiveAbilityTargetPosition:
                return InteractionResultType.PositionChosen;
            default: throw new IllegalArgumentException("Cannot find a result type matching target category:" + targetCategory);
        }
    }
}
