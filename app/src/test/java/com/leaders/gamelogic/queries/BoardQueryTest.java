package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class BoardQueryTest {

    @Test
    public void findCharacterCells_shouldReturnMatchingCharacters() {
        Board board = new Board();

        Character blackLeader = Character.create(
                CharacterType.LeaderKing,
                TeamColor.Black
        );
        Character whiteLeader = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.White
        );

        board.getCell(new Position(3, 0)).setCharacter(blackLeader);
        board.getCell(new Position(3, 6)).setCharacter(whiteLeader);

        List<Cell> cells = BoardQuery.findCharacterCells(
                board,
                TeamColor.Black,
                null
        );

        assertEquals(1, cells.size());
        assertSame(
                blackLeader,
                cells.get(0).getCharacter()
        );
    }

    @Test
    public void findCharacterCells_shouldFilterByTeamAndType() {
        Board board = new Board();

        Character blackLeader = Character.create(
                CharacterType.LeaderKing,
                TeamColor.Black
        );
        Character blackArcher = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );

        board.getCell(new Position(3, 0)).setCharacter(blackLeader);
        board.getCell(new Position(3, 1)).setCharacter(blackArcher);

        List<Cell> cells = BoardQuery.findCharacterCells(
                board,
                TeamColor.Black,
                CharacterType.Archer
        );

        assertEquals(1, cells.size());
        assertSame(
                blackArcher,
                cells.get(0).getCharacter()
        );
    }

    @Test
    public void findCharacterCells_shouldAllowNullAsWildcard() {
        Board board = new Board();

        Character blackArcher = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );
        Character whiteLeader = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.White
        );

        board.getCell(new Position(1, 1)).setCharacter(blackArcher);
        board.getCell(new Position(2, 2)).setCharacter(whiteLeader);

        assertEquals(
                2,
                BoardQuery.findCharacterCells(board, null, null).size()
        );

        assertEquals(
                1,
                BoardQuery.findCharacterCells(
                        board,
                        null,
                        CharacterType.Archer
                ).size()
        );
    }

    @Test
    public void findVisibleCharacterCell_shouldReturnFirstCharacterInDirection() {
        Board board = new Board();

        Position origin = new Position(3, 3);

        Character character = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );

        board.getCell(new Position(3, 1)).setCharacter(character);

        Cell result = BoardQuery.findVisibleCharacterCell(
                board,
                origin,
                Direction.Top,
                TeamColor.Black,
                CharacterType.Archer
        );

        assertSame(
                board.getCell(new Position(3, 1)),
                result
        );
    }

    @Test
    public void findVisibleCharacterCell_shouldSkipEmptyCells() {
        Board board = new Board();

        Position origin = new Position(3, 3);

        Character character = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );

        board.getCell(new Position(3, 1)).setCharacter(character);

        Cell result = BoardQuery.findVisibleCharacterCell(
                board,
                origin,
                Direction.Top,
                null,
                null
        );

        assertSame(
                board.getCell(new Position(3, 1)),
                result
        );
    }

    @Test
    public void findVisibleCharacterCell_shouldStopAtFirstCharacterWhenItDoesNotMatch() {
        Board board = new Board();

        Position origin = new Position(3, 3);

        Character firstCharacter = Character.create(
                CharacterType.Archer,
                TeamColor.White
        );
        Character secondCharacter = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );

        board.getCell(new Position(3, 2)).setCharacter(firstCharacter);
        board.getCell(new Position(3, 1)).setCharacter(secondCharacter);

        assertNull(
                BoardQuery.findVisibleCharacterCell(
                        board,
                        origin,
                        Direction.Top,
                        TeamColor.Black,
                        CharacterType.Archer
                )
        );
    }

    @Test
    public void findLeaderCell_shouldReturnLeaderOfSpecifiedTeam() {
        Board board = new Board();

        Character blackLeader = Character.create(
                CharacterType.LeaderKing,
                TeamColor.Black
        );

        board.getCell(new Position(3, 0)).setCharacter(blackLeader);

        assertSame(
                board.getCell(new Position(3, 0)),
                BoardQuery.findLeaderCell(board, TeamColor.Black)
        );
    }

    @Test
    public void findLeaderCell_shouldReturnNullWhenLeaderIsAbsent() {
        Board board = new Board();

        assertNull(
                BoardQuery.findLeaderCell(board, TeamColor.Black)
        );
    }

    @Test
    public void findAdjacentCell_shouldReturnAdjacentCell() {
        Board board = new Board();
        Position position = new Position(3, 3);

        Cell result = BoardQuery.findAdjacentCell(
                board,
                position,
                Direction.Top
        );

        assertSame(
                board.getCell(Objects.requireNonNull(position.adjacent(Direction.Top))),
                result
        );
    }

    @Test
    public void findEmptyCellsAround_shouldReturnCellsWithinDistance() {
        Board board = new Board();
        Position center = new Position(3, 3);

        List<Cell> cells = BoardQuery.findEmptyCellsAround(
                board,
                center,
                1
        );

        assertEquals(
                Direction.values().length,
                cells.size()
        );

        for (Cell cell : cells) {
            assertNull(cell.getCharacter());
        }
    }

    @Test
    public void findEmptyCellsAround_shouldNotIncludeOccupiedCells() {
        Board board = new Board();
        Position center = new Position(3, 3);

        board.getCell(
                Objects.requireNonNull(center.adjacent(Direction.Top))
        ).setCharacter(
                Character.create(
                        CharacterType.Archer,
                        TeamColor.Black
                )
        );

        List<Cell> cells = BoardQuery.findEmptyCellsAround(
                board,
                center,
                1
        );

        assertEquals(
                Direction.values().length - 1,
                cells.size()
        );
    }

    @Test
    public void getCellByCharacterId_shouldReturnCharacterCell() {
        Board board = new Board();

        Character character = Character.create(
                CharacterType.Archer,
                TeamColor.Black
        );

        Position position = new Position(3, 3);
        board.getCell(position).setCharacter(character);

        assertSame(
                board.getCell(position),
                BoardQuery.getCellByCharacterId(
                        board,
                        character.getId()
                )
        );
    }

    @Test(expected = NoSuchElementException.class)
    public void getCellByCharacterId_shouldThrowWhenCharacterIsAbsent() {
        BoardQuery.getCellByCharacterId(
                new Board(),
                UUID.randomUUID()
        );
    }

    @Test
    public void getRecruitmentCells_shouldReturnEmptyCellsOnBlackStartingRow() {
        Board board = new Board();

        List<Cell> cells = BoardQuery.getRecruitmentCells(
                board,
                TeamColor.Black
        );

        assertEquals(
                Board.COLUMN_COUNT,
                cells.size()
        );

        for (Cell cell : cells) {
            assertNull(cell.getCharacter());
        }
    }

    @Test
    public void getRecruitmentCells_shouldExcludeOccupiedCells() {
        Board board = new Board();

        Cell firstRecruitmentCell = BoardQuery.getLeaderStartingCell(
                board,
                TeamColor.Black
        );

        firstRecruitmentCell.setCharacter(
                Character.create(
                        CharacterType.LeaderKing,
                        TeamColor.Black
                )
        );

        List<Cell> cells = BoardQuery.getRecruitmentCells(
                board,
                TeamColor.Black
        );

        assertEquals(
                Board.COLUMN_COUNT - 1,
                cells.size()
        );

        assertFalse(cells.contains(firstRecruitmentCell));
    }

    @Test
    public void getLeaderStartingCell_shouldReturnDifferentStartingCellsForTeams() {
        Board board = new Board();

        Cell blackStartingCell =
                BoardQuery.getLeaderStartingCell(
                        board,
                        TeamColor.Black
                );

        Cell whiteStartingCell =
                BoardQuery.getLeaderStartingCell(
                        board,
                        TeamColor.White
                );

        assertNotSame(blackStartingCell.getPosition(), whiteStartingCell.getPosition());
    }
}