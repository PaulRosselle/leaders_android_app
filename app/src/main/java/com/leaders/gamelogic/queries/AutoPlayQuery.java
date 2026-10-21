package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import java.util.Objects;

public final class AutoPlayQuery {
    private AutoPlayQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Determines whether a character can be selected automatically for the given request.
     *
     * @param gameMode the current game mode
     * @param request the interaction request
     * @return {@code true} if the request expects a playable character in puzzle mode, has exactly one legal target,
     *         and that target is an automatically playable character; {@code false} otherwise
     */
    public static boolean canAutoSelectCharacter(@NonNull GameMode gameMode,
                                                 @NonNull InteractionRequest request) {
        return gameMode == GameMode.Puzzle &&
                request.getRequestType() == InteractionType.PlayableCharacterExpected &&
                request.getLegalTargets().size() == 1 &&
                isAutoPlayableCharacter(getPlayableCharacterFromTarget(request.getLegalTargets().get(0)));
    }

    /**
     * Determines whether the character can be played automatically for the given request.
     *
     * @param gameMode the current game mode
     * @param request the interaction request
     * @return {@code true} if the game is in puzzle mode, the request expects a position, and the character is
     *         eligible for automatic play; {@code false} otherwise
     */
    public static boolean canAutoPlayCharacter(@NonNull GameMode gameMode,
                                               @NonNull InteractionRequest request) {
        return gameMode == GameMode.Puzzle &&
                request.getRequestType() == InteractionType.PositionExpected &&
                isAutoCharacter(request.getContext().getSourceCharacter());
    }

    /**
     * Determines whether the interaction result represents an automatically playable character.
     *
     * @param gameMode the current game mode
     * @param result the interaction result to evaluate
     * @return {@code true} if the game is in puzzle mode and the result selects an automatically playable character
     *         or a position for an automatically playable character; {@code false} otherwise
     */
    public static boolean canAutoPlayCharacter(@NonNull GameMode gameMode,
                                               @NonNull InteractionResult result) {
        if (gameMode != GameMode.Puzzle) {
            return false;
        }

        switch (result.getResultType()) {
            case PlayableCharacterChosen:
                return isAutoPlayableCharacter(getPlayableCharacterFromTarget(result.getChosenTarget()));
            case PositionChosen:
                return isAutoCharacter(result.getContext().getSourceCharacter());
            default:
                return false;
        }
    }

    /**
     * Determines whether the action is performed automatically.
     *
     * @param gameMode the current game mode
     * @param action the game action to evaluate
     * @return {@code true} if the game is in puzzle mode and the action originates from an automatically played
     *         character; {@code false} otherwise
     */
    public static boolean isAutoAction(@NonNull GameMode gameMode, @NonNull IGameAction action) {
        if (gameMode != GameMode.Puzzle || !(action instanceof CharacterAction)) {
            return false;
        }
        CharacterAction characterAction = (CharacterAction) action;
        return isAutoCharacter(characterAction.getSrcCharacter());
    }

    /**
     * Extracts the playable character from the given target.
     *
     * @param target the target containing a playable character
     * @return the playable character associated with the target, never {@code null}
     * @throws IllegalArgumentException if the target is {@code null} or does not represent a playable character
     * @throws NullPointerException if the target does not contain a playable character
     */
    private static PlayableCharacter getPlayableCharacterFromTarget(@Nullable InteractionTarget target) {
        if (target == null) {
            throw new IllegalArgumentException("A playable character cannot be gotten from a null target");
        }

        TargetCategory targetCategory = target.getCategory();
        if (targetCategory != TargetCategory.PlayableCharacter) {

            throw new IllegalArgumentException("No playable character in target of category: " + targetCategory);
        }

        return Objects.requireNonNull(target.getChosenPlayableCharacter(),
                "Playable character missing inside " + targetCategory + " target"
        );
    }

    /**
     * Determines whether the playable character can be played automatically.
     *
     * @param playableCharacter the playable character to evaluate
     * @return {@code true} if the character is mandatory and eligible for automatic play; {@code false} otherwise
     */
    private static boolean isAutoPlayableCharacter(@NonNull PlayableCharacter playableCharacter) {
        return playableCharacter.isMandatory() && isAutoCharacter(playableCharacter.getCharacter());
    }

    /**
     * Determines whether the character is eligible for automatic play.
     *
     * @param character the character to evaluate
     * @return {@code true} if the character has an automatically playable type and belongs to a different team
     *         than the puzzle player; {@code false} otherwise
     */
    private static boolean isAutoCharacter(@Nullable Character character) {
        return character != null &&
                isAutoCharacterType(character.getCharacterType()) &&
                character.getTeamColor() != PuzzleEditionUtils.getPuzzlePlayerTeamColor();
    }

    /**
     * Determines whether the character type is eligible for automatic play.
     *
     * @param characterType the character type to evaluate
     * @return {@code true} if the character type is {@link CharacterType#Nemesis}; {@code false} otherwise
     */
    private static boolean isAutoCharacterType(@NonNull CharacterType characterType) {
        return characterType == CharacterType.Nemesis;
    }
}
