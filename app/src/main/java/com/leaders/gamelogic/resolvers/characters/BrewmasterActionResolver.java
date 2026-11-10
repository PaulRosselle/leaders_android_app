package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.resolvers.ResolverUtils;

import java.util.List;

/**
 * Resolves the Brewmaster's active ability.
 *
 * <p>The Brewmaster can move an adjacent allied character by one cell.
 * The ability requires the player to first select an adjacent ally, then
 * select an empty cell adjacent to that ally.</p>
 */
public final class BrewmasterActionResolver extends MoverActionResolver {

    public BrewmasterActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    /** Returns the positions of all allied characters adjacent to the Brewmaster.
     * <p>Only characters belonging to the Brewmaster's team are considered.
     * Empty cells and enemy characters are ignored.</p> */
    @Override
    @NonNull
    protected List<Position> getTargetPositions() {
        List<Cell> allyCells = BoardQuery.getAdjacentCells(game.getBoard(), characterPos, cell ->
                cell.getCharacter() != null && cell.getCharacter().getTeamColor() == character.getTeamColor()
        );
        return ResolverUtils.getPositions(allyCells);
    }
}
