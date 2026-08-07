package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.Map;

public class BoardTest {

    @Test
    public void constructor_shouldCreateAllBoardCells() {
        Board board = new Board();

        assertEquals(37, board.getCells().size());
    }

    @Test
    public void constructor_shouldCreateExpectedNumberOfRowsForEachColumn() {
        Board board = new Board();

        for (int column = 0; column < Board.COLUMN_COUNT; column++) {
            int finalColumn = column;
            assertEquals(
                    Board.getRowCount(column),
                    board.getCells().keySet().stream()
                            .filter(position -> position.getX() == finalColumn)
                            .count()
            );
        }
    }

    @Test
    public void copy_shouldNotShareCells() {
        Board original = new Board();
        Position position = new Position(3, 3);
        Character testCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        original.getCell(position).setCharacter(testCharacter);
        Board copy = new Board(original);

        assertNotSame(
                original.getCell(position),
                copy.getCell(position)
        );
        // Characters are immutable and safely shared by reference
        assertSame(
                original.getCell(position).getCharacter(),
                copy.getCell(position).getCharacter()
        );
    }

    @Test
    public void copy_shouldNotShareMutableCellState() {
        Board original = new Board();
        Board copy = new Board(original);

        Position position = new Position(3, 3);
        Character testCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);

        original.getCell(position).setCharacter(testCharacter);

        assertNull(copy.getCell(position).getCharacter());
    }

    @Test
    public void getRowCount_shouldReturnExpectedValues() {
        assertEquals(4, Board.getRowCount(0));
        assertEquals(5, Board.getRowCount(1));
        assertEquals(6, Board.getRowCount(2));
        assertEquals(7, Board.getRowCount(3));
        assertEquals(6, Board.getRowCount(4));
        assertEquals(5, Board.getRowCount(5));
        assertEquals(4, Board.getRowCount(6));
    }

    @Test
    public void getCells_shouldContainCellForEveryValidPosition() {
        Board board = new Board();

        for (int x = 0; x < Board.COLUMN_COUNT; x++) {
            for (int y = 0; y < Board.getRowCount(x); y++) {
                assertTrue(board.getCells().containsKey(new Position(x, y)));
            }
        }
    }

    @Test
    public void getCell_shouldReturnCellAtRequestedPosition() {
        Board board = new Board();
        Position position = new Position(3, 3);

        Cell cell = board.getCell(position);

        assertNotNull(cell);
        assertEquals(position, cell.getPosition());
    }

    @Test
    public void getCell_shouldReturnSameCellInstanceForSamePosition() {
        Board board = new Board();
        Position position = new Position(3, 3);

        Cell first = board.getCell(position);
        Cell second = board.getCell(position);

        assertSame(first, second);
    }

    @Test
    public void getCells_shouldReturnUnmodifiableMap() {
        Board board = new Board();
        Map<Position, Cell> cells = board.getCells();

        assertThrows(
                UnsupportedOperationException.class,
                cells::clear
        );
    }
}