package com.leaders.gamelogic.resolvers.characters;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the Tactician's active ability.
 *
 * <p>When an ally is adjacent to the opponent's leader, the Tactician can move any ally
 * by one cell. The enemy can be moved to any valid adjacent empty cell.</p>
 *
 * <p>The ability requires the player to first select the enemy to move, then
 * select its destination.</p>
 */
public final class TacticianActionResolver extends MoverActionResolver {
    public TacticianActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        super(game, gameHistory, character);
    }

    /**
     * Checks whether the Tactician can use its active ability.
     *
     * @return {@code true} if the ability can be used and at least one friendly
     *         character is adjacent to the enemy leader, otherwise {@code false}
     */
    @Override
    protected boolean canUseActiveAbility() {
        if (!CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character)) {
            return false;
        }

        TeamColor tacticianTeamColor = character.getTeamColor();
        Board board = game.getBoard();

        Cell enemyLeaderCell = BoardQuery.findLeaderCell(board, tacticianTeamColor.getOpposite());
        if (enemyLeaderCell == null) {
            return false;
        }

        return !BoardQuery.getAdjacentCells(board, enemyLeaderCell.getPosition(),
                adjacentCell -> adjacentCell.getCharacter() != null &&
                        adjacentCell.getCharacter().getTeamColor() == tacticianTeamColor
        ).isEmpty();
    }

    /**
     * Gets the positions of all characters belonging to the Tactician's team.
     *
     * @return the list of positions occupied by friendly characters
     */
    @Override
    @NonNull
    protected List<Position> getTargetPositions() {
        return BoardQuery.findCharacterCells(game.getBoard(), character.getTeamColor(), null)
                .stream()
                .map(Cell::getPosition)
                .collect(Collectors.toList());
    }
}
