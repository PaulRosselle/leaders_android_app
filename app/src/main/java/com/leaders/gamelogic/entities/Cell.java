package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.Direction;

import java.util.HashMap;

public class Cell {
    @NonNull
    private final Position pos;
    private Character character;
    @NonNull
    private final HashMap<Direction, Cell> adjacentCells;

    public Cell(@NonNull Position pos) {
        this.pos = pos;
        adjacentCells = new HashMap<>();
    }

    @NonNull
    public Position getPos() {
        return pos;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Character getCharacter() {
        return character;
    }

    public void setAdjacentCell(@NonNull Direction direction, @NonNull Cell adjacentCell) {
        adjacentCells.put(direction, adjacentCell);
    }

    @NonNull
    public HashMap<Direction, Cell> getAdjacentCells() {
        return adjacentCells;
    }
}
