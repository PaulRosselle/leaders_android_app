package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;

import java.util.Arrays;
import java.util.List;

/**
 * Centralizes read-only queries related to character abilities whose scope goes
 * beyond a single character's own action resolution.
 * <p>
 * This class covers two kinds of queries:
 * <ul>
 *     <li><b>Cross-character ability effects</b>: abilities of one character that
 *     alter what another character can do (e.g. Jailer preventing an adjacent
 *     opponent's active ability, Vizier extending an allied Leader's movement
 *     range, Protector shielding allies from being moved).</li>
 *     <li><b>Phase/game-wide eligibility and scoring rules</b>: queries whose
 *     answer depends on state that spans the whole actions phase or team, rather
 *     than a single character in isolation (e.g. turn eligibility for Hermit and
 *     Cub, Nemesis's forced reaction to a leader movement, a character's
 *     contribution to an enemy leader capture).</li>
 * </ul>
 * <p>
 * Abilities that only affect their own character's available actions (e.g. the
 * Nemesis's own movement algorithm) do NOT belong here — they are implemented
 * in a dedicated {@code CharacterActionResolver} override for that character.
 * <p>
 * All methods are static and side effect free: they read the game state and
 * return a result without mutating anything.
 */
public final class CharacterAbilityQuery {
    public static final int LEADER_CAPTURE_VALUE = 2;

    private CharacterAbilityQuery(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Computes the distance between a character and an enemy leader on the board.
     *
     * <p>This method assumes that the given character exists on the game board.
     * The distance is computed using the hexagonal grid distance defined by
     * {@link Position#distanceTo(Position)}.</p>
     *
     * @param game the current game state
     * @param leaderCell the cell occupied by the enemy leader
     * @param character the character whose distance to the leader is computed
     * @return the number of cells between the character and the enemy leader
     */
    private static int getLeaderDistance(@NonNull Game game, @NonNull Cell leaderCell,
                                         @NonNull Character character) {
        return leaderCell.getPosition().distanceTo(
                BoardQuery.getCellByCharacterId(game.getBoard(), character.getId()).getPosition()
        );
    }

    /**
     * Checks whether a character is aligned with an enemy leader on the board.
     *
     * <p>The character's cell is retrieved from the game board using its identifier.
     * The alignment is then determined using {@link Position#isAligned(Position)}.</p>
     *
     * @param game the current game state containing the character and the board
     * @param leaderCell the cell occupied by the enemy leader
     * @param character the character whose alignment with the leader is checked
     * @return {@code true} if the character and the enemy leader are aligned;
     *         {@code false} otherwise
     */
    private static boolean isLeaderAligned(@NonNull Game game, @NonNull Cell leaderCell,
                                           @NonNull Character character) {
        return leaderCell.getPosition().isAligned(BoardQuery.getCellByCharacterId(game.getBoard(), character.getId()).getPosition());
    }

    /**
     * Computes the contribution of a character to the capture of the enemy leader.
     *
     * <p>The contribution depends on the character type and its position relative
     * to the enemy leader:</p>
     * <ul>
     *     <li>An {@code Archer} contributes {@code 1} when exactly two cells away
     *     from the enemy leader.</li>
     *     <li>An {@code Assassin} captures the enemy leader immediately when
     *     adjacent to it and contributes {@link #LEADER_CAPTURE_VALUE}.</li>
     *     <li>A {@code Cub} never contributes to a capture.</li>
     *     <li>Other characters contribute {@code 1} when adjacent to the enemy leader.</li>
     * </ul>
     *
     * <p>If no enemy leader exists on the board, this method returns {@code 0}.
     * The game history parameter is currently reserved for passive abilities whose
     * effects depend on previous game states.</p>
     *
     * @param game the current game state
     * @param character the character whose capture contribution is evaluated
     * @return the character's contribution to the enemy leader capture
     */
    public static int getCaptureContribution(@NonNull Game game,
                                             @NonNull Character character,
                                             @NonNull Cell enemyLeaderCell) {
        switch (character.getCharacterType()) {
            // The archer only takes part in the capture in a straight line from a distance of two
            case Archer: {
                return isLeaderAligned(game, enemyLeaderCell, character) &&
                        getLeaderDistance(game, enemyLeaderCell, character) == 2 ? 1 : 0;
            }
            // The assassin captures the leader by himself if he is adjacent to it
            case Assassin: {
                return getLeaderDistance(game, enemyLeaderCell, character) == 1 ? LEADER_CAPTURE_VALUE : 0;
            }
            // The cub never takes part to the capture
            case Cub: return 0;
            // By default, a character count as 1 and must be adjacent to be taken into account
            default: return getLeaderDistance(game, enemyLeaderCell, character) == 1 ? 1 : 0;
        }
    }


    /**
     * Determines whether a character can act during the current actions phase.
     *
     * <p>A character can act if it belongs to the team whose turn it is, is not a
     * {@code Nemesis} (which only acts via {@link #mustActNow}), and has not already
     * acted during this phase. Special case: {@code Hermit} and {@code Cub} can only
     * act immediately after one another.</p>
     *
     * @param currentActionsPhase the current actions phase
     * @param character the character being evaluated
     * @return {@code true} if the character can act
     */
    public static boolean canAct(@NonNull ActionsPhase currentActionsPhase,
                                 @NonNull Character character) {
        // Characters are only allowed to act during a turn matching their team colors
        if (character.getTeamColor() != currentActionsPhase.getTurnTeamColor()) {
            return false;
        }

        // Nemesis ability prevents her to act normally. She can only be forced to act in
        // reaction to her ability activation condition (= enemy leader moving)
        if (character.getCharacterType() == CharacterType.Nemesis) {
            return false;
        }


        List<CharacterAction> characterActions = currentActionsPhase.getCharacterActions();
        // Since only one action per turn is allowed, we look for an action performed by the character
        for (CharacterAction characterAction : characterActions) {
            if (characterAction.getSrcCharacter().getId().equals(character.getId())) {
                return false;
            }
        }

        // Hermit and Cub can only act immediately after one another
        if (character.getCharacterType().getCharacterCard() == CharacterCard.HermitAndCub) {
            // We search for an action made by the other
            CharacterType searchedCharacterType;
            if (character.getCharacterType() == CharacterType.Hermit) {
                searchedCharacterType = CharacterType.Cub;
            } else {
                searchedCharacterType = CharacterType.Hermit;
            }
            CharacterAction foundAction = null;
            for (CharacterAction characterAction : characterActions) {
                if (characterAction.getSrcCharacter().getCharacterType() == searchedCharacterType) {
                    foundAction = characterAction;
                    break;
                }
            }
            // If an action is found, the character can only play if it was the last phase action
            return foundAction == null || foundAction == characterActions.get(characterActions.size() - 1);
        }

        // If they have not already acted, it means that they can act
        return true;
    }

    /**
     * Determines whether a character is required to act at some point during the
     * current turn (without any immediacy constraint).
     *
     * <p>No current ability imposes such a constraint; always returns {@code false}.
     * Reserved for future passive abilities.</p>
     *
     * @return {@code false} in the current implementation
     */
    public static boolean mustAct() {
        // There are currently no way for a character to be forced to play "during this turn"
        return false;
    }

    /**
     * Determines whether a character must act immediately, in reaction to the last
     * action of the phase.
     *
     * <p>Only {@code Nemesis} is affected: it must play right after any leader
     * movement.</p>
     *
     * @param currentActionsPhase the current actions phase
     * @param character the character being evaluated
     * @return {@code true} if the character must act immediately
     */
    public static boolean mustActNow(@NonNull ActionsPhase currentActionsPhase,
                                     @NonNull Character character) {
        // Nemesis must play immediately after an action moving a leader
        if (character.getCharacterType() == CharacterType.Nemesis &&
                !currentActionsPhase.getActions().isEmpty()) {
            List<CharacterAction> characterActions = currentActionsPhase.getCharacterActions();
            CharacterAction lastAction = characterActions.get(characterActions.size() - 1);
            // If we find an enemy leader movement in the action targets, the Nemesis is forced to play
            for (CharacterActionMotion motion : lastAction.getMotions()) {
                for (CharacterActionTarget actionTarget : motion.getTargets()) {
                    if (actionTarget.getCharacter().getCharacterType().getCharacterCard().isLeader() &&
                            actionTarget.getCharacter().getTeamColor() != character.getTeamColor() &&
                            actionTarget.getOriginPos() != null && actionTarget.getDestPos() != null) {
                        return true;
                    }
                }
            }
        }

        // By default, no character can be forced to play immediately
        return false;
    }


    /**
     * Checks whether a character matching the given type and team color is adjacent
     * to the reference character.
     *
     * @param game the current game state
     * @param refCharacter the character used as the position reference
     * @param searchedCharacterType the character type to search for
     * @param searchedTeamColor the team color to search for
     * @return {@code true} if a matching adjacent character exists, {@code false} otherwise
     */
    private static boolean hasAdjacentCharacter(@NonNull Game game, @NonNull Character refCharacter,
                                                @NonNull CharacterType searchedCharacterType,
                                                @NonNull TeamColor searchedTeamColor) {
        Cell characterCell = BoardQuery.getCellByCharacterId(game.getBoard(), refCharacter.getId());
        for (Direction direction : Direction.values()) {
            Position adjacentPos = characterCell.getPosition().adjacent(direction);
            if (adjacentPos != null) {
                Character adjacentCharacter = game.getBoard().getCell(adjacentPos).getCharacter();
                if (adjacentCharacter != null &&
                        adjacentCharacter.getCharacterType() == searchedCharacterType &&
                        adjacentCharacter.getTeamColor() == searchedTeamColor) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a character can use their active ability.
     * <p>
     * An active ability cannot be used if the character does not have one
     * or if it is prevented by an opposing passive ability (e.g. Jailer).
     *
     * @param game the current game state
     * @param character the character attempting to use the ability
     * @return {@code true} if the character can use their active ability, {@code false} otherwise
     */
    public static boolean canUseActiveAbility(@NonNull Game game, @NonNull Character character) {
        // First, the character must have an active ability
        if (!Arrays.asList(character.getCharacterType().getCharacterCard().getAbilityTypes()).contains(AbilityType.Active)) {
            return false;
        }

        // Then, their ability activation must not be prevented by a passive ability :
        // 1. Passive ability - Jailer prevents adjacent opponents active ability use
        return !hasAdjacentCharacter(game, character, CharacterType.Jailer, character.getTeamColor().getOpposite());
    }

    /**
     * Checks whether a character can be moved by enemy abilities.
     * <p>
     * Characters protected by a {@link CharacterType#Protector} cannot be moved
     * by enemy abilities.
     *
     * @param game the current game state
     * @param character the character to check
     * @return {@code true} if the character can be moved by enemy abilities, {@code false} otherwise
     */
    public static boolean canBeMovedByEnemyAbilities(@NonNull Game game,
                                                     @NonNull Character character) {
        // The protector prevents himself and its adjacent allies to be moved by enemy abilities
        return !(character.getCharacterType() == CharacterType.Protector) &&
                !hasAdjacentCharacter(game, character, CharacterType.Protector, character.getTeamColor());
    }

    /**
     * Checks whether a team has recruited a character of the given type.
     *
     * @param game the current game state
     * @param characterType the character type to search for
     * @param teamColor the team color to search within
     * @return {@code true} if the team has recruited a character of the given type, {@code false} otherwise
     */
    private static boolean teamContainsCharacter(@NonNull Game game,
                                                 @NonNull CharacterType characterType,
                                                 @NonNull TeamColor teamColor) {
        return game.getRecruitedCharacters().stream()
                .anyMatch(recruitedCharacter ->
                        recruitedCharacter.getCharacterType() == characterType &&
                                recruitedCharacter.getTeamColor() == teamColor);
    }

    /**
     * Returns the cells the specified character can move to using its normal movement.
     * Handles the cross-character extension granted by an allied Vizier for leaders.
     * <p>
     * Does not handle Nemesis, whose movement is fully self-contained and resolved
     * by its dedicated {@code CharacterActionResolver} override.
     *
     * @param game the current game.
     * @param character the character to move.
     * @return the list of valid destination cells.
     * @throws IllegalArgumentException if character is a Nemesis
     */
    @NonNull
    public static List<Cell> getNormalMovementDestCells(@NonNull Game game,
                                                        @NonNull Character character) {
        // Nemesis is an exception and uses her own movement algorithm
        if (character.getCharacterType() == CharacterType.Nemesis) {
            throw new IllegalArgumentException("Nemesis movement logic is handled apart from the generic movement function");
        }
        // By default, normal movement allow characters to go to an adjacent empty tile.
        // Leaders can move up to two cells when they have a vizier in their team
        return BoardQuery.findEmptyCellsAround(game.getBoard(),
                BoardQuery.getCellByCharacterId(game.getBoard(), character.getId()).getPosition(),
                character.getCharacterType().getCharacterCard().isLeader() &&
                        teamContainsCharacter(game, CharacterType.Vizier, character.getTeamColor()) ? 2 : 1);
    }
}
