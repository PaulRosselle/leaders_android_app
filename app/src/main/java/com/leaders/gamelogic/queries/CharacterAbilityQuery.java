package com.leaders.gamelogic.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
     * Returns the distance between a leader cell and another cell.
     *
     * @param leaderCell the cell containing the leader
     * @param cell the cell to measure the distance to
     * @return the distance between the two cells
     */
    private static int getLeaderDistance(@NonNull Cell leaderCell, @NonNull Cell cell) {
        return leaderCell.getPosition().distanceTo(cell.getPosition());
    }

    /**
     * Checks whether a cell is aligned with the leader's cell.
     *
     * @param leaderCell the cell containing the leader
     * @param cell the cell to check
     * @return {@code true} if the two cells are aligned, otherwise {@code false}
     */
    private static boolean isLeaderAligned(@NonNull Cell leaderCell, @NonNull Cell cell) {
        return leaderCell.getPosition().isAligned(cell.getPosition());
    }

    /**
     * Returns the contribution of a character to the capture of an enemy leader.
     *
     * @param enemyLeaderCell the cell containing the enemy leader
     * @param characterCell the cell containing the character to evaluate
     * @param currentPhase current game phase
     * @return the character's contribution to the capture
     * @throws IllegalArgumentException if the character cell contains no character
     */
    public static int getCaptureContribution(@NonNull Cell enemyLeaderCell,
                                             @NonNull Cell characterCell,
                                             @NonNull GamePhase currentPhase) {
        if (characterCell.getCharacter() == null) {
            throw new IllegalArgumentException("Invalid capture cell, no character: " + characterCell.getPosition());
        }

        Character character = characterCell.getCharacter();
        if (!character.getCharacterType().canCapture()) {
            return 0;
        }

        switch (character.getCharacterType()) {
            // The archer only takes part in the capture in a straight line from a distance of two
            case Archer: {
                return isLeaderAligned(enemyLeaderCell, characterCell) &&
                        getLeaderDistance(enemyLeaderCell, characterCell) == 2 ? 1 : 0;
            }
            // The assassin captures the leader by himself if he is adjacent to it
            case Assassin: {
                return getLeaderDistance(enemyLeaderCell, characterCell) == 1 ? LEADER_CAPTURE_VALUE : 0;
            }
            // The elite archer takes part in the capture in a straight line under 2 cells
            case EliteArcher: {
                return isLeaderAligned(enemyLeaderCell, characterCell) &&
                        getLeaderDistance(enemyLeaderCell, characterCell) <= 2 ? 1 : 0;
            }
            case Sniper: {
                int leaderDistance = getLeaderDistance(enemyLeaderCell, characterCell);

                if (currentPhase.getPhaseType() == GamePhaseType.TurnStart &&
                        character.getTeamColor() == currentPhase.getPhasePlayer().getTeamColor() &&
                        isLeaderAligned(enemyLeaderCell, characterCell) &&
                        leaderDistance >= 3) {
                    return LEADER_CAPTURE_VALUE;
                }
                return leaderDistance == 1 ? 1 : 0;
            }
            // By default, a character count as 1 and must be adjacent to be taken into account
            default: return getLeaderDistance(enemyLeaderCell, characterCell) == 1 ? 1 : 0;
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
     * @param game the game to evaluate
     * @param currentActionsPhase the current actions phase
     * @param character the character being evaluated
     * @return {@code true} if the character can act
     */
    public static boolean canAct(@NonNull Game game,
                                 @NonNull ActionsPhase currentActionsPhase,
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
        if (isHermitOrCub(game, character)) {
            // We search for an action made by the other
            CharacterAction foundAction = null;
            for (CharacterAction characterAction : characterActions) {
                if (isHermitOrCub(game, characterAction.getSrcCharacter())) {
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
     * Checks whether the character is a Hermit or a Cub, either in its current
     * form or in its original form before transformation.
     *
     * @param game the game containing the character's transformed state
     * @param character the character to check
     * @return {@code true} if the character is or was a Hermit or a Cub,
     *         otherwise {@code false}
     */
    private static boolean isHermitOrCub(@NonNull Game game, @NonNull Character character) {
        if (isHermitOrCub(character)) {
            return true;
        }

        TransformedState transformedState = game.getTransformedState(character.getId());
        return transformedState != null && isHermitOrCub(transformedState.getCharacterBefore());
    }

    /**
     * Checks whether the character is a Hermit or a Cub.
     *
     * @param character the character to check
     * @return {@code true} if the character belongs to the Hermit and Cub card, otherwise {@code false}
     */
    private static boolean isHermitOrCub(@NonNull Character character) {
        return character.getCharacterType().getCharacterCard() == CharacterCard.HermitAndCub;
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
     * Checks whether a character can use their active ability.
     * <p>
     * An active ability cannot be used if the character does not have one
     * or if it is prevented by an opposing passive ability (e.g. Jailer, Tactician).
     *
     * @param game the game to evaluate
     * @param gameHistory the game history to evaluate
     * @param character the character attempting to use the ability
     * @return {@code true} if the character can use their active ability, {@code false} otherwise
     */
    public static boolean canUseActiveAbility(@NonNull Game game,
                                              @NonNull GameHistory gameHistory,
                                              @NonNull Character character) {
        // First, the character must have an active ability
        if (!Arrays.asList(character.getCharacterType().getCharacterCard().getAbilityTypes()).contains(AbilityType.Active)) {
            return false;
        }

        // Then, their ability activation must not be prevented by a passive ability:
        return !isBlockedByJailer(game, character) && !isBlockedByTactician(gameHistory, character);
    }

    /**
     * Checks whether the character's active ability is blocked by an opposing Jailer.
     * <p>
     * A Jailer prevents adjacent opponents from using their active abilities.
     *
     * @param game the game containing the character and board
     * @param character the character to check
     * @return {@code true} if the character is blocked by an opposing Jailer,
     *         {@code false} otherwise
     */
    private static boolean isBlockedByJailer(@NonNull Game game, @NonNull Character character) {
        // The Jailer prevents adjacent opponents active ability use
        Cell characterCell = BoardQuery.getCellByCharacterId(game.getBoard(), character.getId());
        return !BoardQuery.getAdjacentCells(game.getBoard(), characterCell.getPosition(),
                cell -> cell.getCharacter() != null &&
                        cell.getCharacter().getCharacterType() == CharacterType.Jailer &&
                        cell.getCharacter().getTeamColor() != character.getTeamColor()
        ).isEmpty();
    }

    /**
     * Checks whether the character was moved by a Tactician of the same team
     * during the current phase.
     *
     * @param gameHistory the game history containing the current phase
     * @param character the character to check
     * @return {@code true} if the character was moved by a friendly Tactician
     *         during the current phase, otherwise {@code false}
     */
    private static boolean isBlockedByTactician(@NonNull GameHistory gameHistory,
                                                @NonNull Character character) {
        // A character moved by the Tactician cannot use its ability during the turn
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (currentPhase == null) {
            return false;
        }

        for (IGameAction action : currentPhase.getActions()) {
            if (!(action instanceof CharacterAction)) {
                continue;
            }

            CharacterAction characterAction = (CharacterAction) action;
            Character sourceCharacter = characterAction.getSrcCharacter();

            if (sourceCharacter.getCharacterType() != CharacterType.Tactician ||
                    sourceCharacter.getTeamColor() != character.getTeamColor() ||
                    characterAction.getMotions().size() != 1) {
                continue;
            }

            // Characters are compared by ID to take into account transformed characters
            CharacterActionMotion motion = characterAction.getMotions().get(0);
            if (motion.getTargets().size() == 1 &&
                    motion.getTargets().get(0).getCharacter().getId().equals(character.getId())) {
                return true;
            }
        }

        return false;
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
        if (character.getCharacterType() == CharacterType.Protector) {
            return false;
        }

        Cell characterCell = BoardQuery.getCellByCharacterId(game.getBoard(), character.getId());
        return BoardQuery.getAdjacentCells(game.getBoard(), characterCell.getPosition(),
                cell -> cell.getCharacter() != null &&
                        cell.getCharacter().getCharacterType() == CharacterType.Protector &&
                        cell.getCharacter().getTeamColor() == character.getTeamColor()
        ).isEmpty();
    }

    /**
     * Gets the transformed state of the character currently transformed into a Frog.
     *
     * @param game the game to search
     * @return the transformed Frog state, or {@code null} if no transformed Frog exists
     * @throws NullPointerException if a transformed state cannot be found for a character ID
     * @throws IllegalStateException if more than one transformed Frog exists
     */
    @Nullable
    public static TransformedState getExistingTransformedFrogState(@NonNull Game game) {
        TransformedState transformedFrogState = null;

        for (UUID characterId : game.getTransformedCharacterIds()) {
            TransformedState transformedState = Objects.requireNonNull(
                    game.getTransformedState(characterId),
                    "No transformed character found matching id: " + characterId
            );

            if (transformedState.getCharacterAfter().getCharacterType() == CharacterType.Frog) {
                if (transformedFrogState != null) {
                    throw new IllegalStateException("There can be only one transformed frog on the board");
                }
                transformedFrogState = transformedState;
            }
        }

        return transformedFrogState;
    }

    /**
     * Checks whether a Frog transformation has ended during the given turn.
     *
     * @param turn the turn to check
     * @param state the transformed state to check
     * @return {@code true} if the character was not transformed into a Frog during
     *         this turn's actions phase, otherwise {@code false}
     */
    public static boolean isFrogTransformationOver(@NonNull Turn turn,
                                                   @NonNull TransformedState state) {
        // We search for the character transformation action during this turn actions phase
        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);
        for (CharacterAction action : actionsPhase.getCharacterActions()) {
            for (CharacterActionMotion motion : action.getMotions()) {
                if (motion.getMotionType() == CharacterMotionType.Transform &&
                    state.getCharacterId().equals(motion.getTargets().get(0).getCharacter().getId())) {
                    return false;
                }
            }
        }
        // A character transformed before a turn action phase is untransformed at this turn's end
        return true;
    }

    /**
     * Returns the paths the specified character can take using its normal movement.
     * Handles passive character abilities impacting other characters.
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
    public static List<CharacterPath> getNormalMovementPaths(@NonNull Game game,
                                                             @NonNull Character character) {
        // Nemesis is an exception and uses her own movement algorithm
        if (character.getCharacterType() == CharacterType.Nemesis) {
            throw new IllegalArgumentException("Nemesis movement logic is handled apart from the generic movement function");
        }

        // Leaders can move up to two cells when they have a vizier on the board in their team.
        boolean isEnhancedMovement = (character.getCharacterType().getCharacterCard().isLeader() &&
                !BoardQuery.findCharacterCells(game.getBoard(), character.getTeamColor(), CharacterType.Vizier).isEmpty());

        Position characterPos = BoardQuery.getCellByCharacterId(game.getBoard(), character.getId()).getPosition();

        return BoardQuery.getEmptyPathsAround(game.getBoard(), characterPos, isEnhancedMovement ? 2 : 1, true);
    }
}
