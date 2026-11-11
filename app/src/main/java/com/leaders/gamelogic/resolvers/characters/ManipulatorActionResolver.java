package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the Manipulator's active ability.
 *
 * <p>The Manipulator moves an enemy visible in a straight line and not adjacent
 * by one cell. The enemy can be moved to any valid adjacent empty cell.</p>
 *
 * <p>The ability requires the player to first select the enemy to move, then
 * select its destination.</p>
 */
public final class ManipulatorActionResolver extends MoverActionResolver {

    public ManipulatorActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory,
                                     @NonNull Character character) {
        super(game, gameHistory, character);
    }

    /**
     * Returns the positions of all enemies visible to the Manipulator.
     *
     * <p>Only the first character encountered in each direction is considered.
     * Adjacent characters are excluded because the Manipulator can only target
     * non-adjacent enemies.</p>
     */
    @Override
    @NonNull
    protected List<Position> getTargetPositions() {
        List<Position> targets = new ArrayList<>();

        for (Cell targetCell : BoardQuery.findVisibleCharacterCells(
                game.getBoard(), characterPos, character.getTeamColor().getOpposite(),
                null, true)) {
            if (CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, targetCell.getCharacter())) {
                targets.add(targetCell.getPosition());
            }
        }

        return targets;
    }
}