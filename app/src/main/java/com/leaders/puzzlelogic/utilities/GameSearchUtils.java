package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.factories.CharacterActionResolverFactory;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;

import java.util.ArrayList;
import java.util.List;

public class GameSearchUtils {
    private static final int MAX_RESOLVER_INTERACTION_DEPTH = 2;

    private GameSearchUtils(){
        throw new AssertionError("Cannot instantiate utility class");
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
    public static List<CharacterAction> enumerateActions(@NonNull Game game,
                                                         @NonNull GameHistory history,
                                                         @NonNull Character character) {
        CharacterActionResolver resolver = CharacterActionResolverFactory.create(game, history, character);

        CharacterActionBuilder builder = new CharacterActionBuilder(character);

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
            throw new IllegalStateException("Resolver interaction flow did not terminate: " + resolver.getClass().getSimpleName());
        }

        InteractionRequest request = resolver.getNextInteraction(builder);

        if (request == null) {
            actions.add(resolver.buildAction(builder));
            return;
        }

        for (InteractionTarget target : request.getLegalTargets()) {
            InteractionResult result = new InteractionResult(
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
     * Finds a playable character by its character identifier.
     *
     * @param playableCharacters the playable characters to search
     * @param character the character to find
     * @return the corresponding playable character, or {@code null} if none
     *         matches the specified character
     */
    @Nullable
    public static PlayableCharacter findPlayableCharacterById(@NonNull List<PlayableCharacter> playableCharacters,
                                                              @NonNull Character character) {
        for (PlayableCharacter playableCharacter : playableCharacters) {
            if (character.getId() == playableCharacter.getCharacter().getId()) {
                return playableCharacter;
            }
        }

        return null;
    }
}
