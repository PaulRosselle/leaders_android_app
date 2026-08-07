package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class Board {
    // There is as many column as the diameter of the hexagonal board
    public static final int COLUMN_COUNT = Position.HEX_RADIUS * 2 + 1;

    @NonNull
    private final Map<Position, Cell> cells;

    public Board() {
        cells = new HashMap<>();
        for (int x = 0; x < COLUMN_COUNT; x++) {
            for (int y = 0; y < getRowCount(x); y++) {
                Position cellPos = new Position(x, y);
                cells.put(cellPos, new Cell(cellPos));
            }
        }
    }

    public Board(@NonNull Board refBoard) {
        this();
        for (Map.Entry<Position, Cell> entry : cells.entrySet()) {
            entry.getValue().setCharacter(refBoard.getCell(entry.getKey()).getCharacter());
        }
    }

    public static int getRowCount(int columnIdx) {
        return COLUMN_COUNT - Math.abs(columnIdx - Position.HEX_RADIUS);
    }

    /**
     * Returns an unmodifiable view of all cells on the board.
     *
     * @return an unmodifiable mapping between positions and their corresponding
     *         cells
     */
    @NonNull
    public Map<Position, Cell> getCells() {
        return Collections.unmodifiableMap(cells);
    }

    /**
     * Returns the cell located at the given position.
     *
     * @param position the position of the requested cell
     * @return the cell located at the given position
     * @throws java.util.NoSuchElementException if the position does not belong
     *                                          to this board
     */
    @NonNull
    public Cell getCell(@NonNull Position position) {
        Cell cell = cells.get(position);
        if (cell == null) {
            throw new NoSuchElementException("No cell found matching position " + position);
        }
        return cell;
    }
}
