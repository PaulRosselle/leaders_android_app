package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

public class Cell {
    @NonNull
    private final Position position;
    private Character character;

    public Cell(@NonNull Position position) {
        this.position = position;
    }

    @NonNull
    public Position getPosition() {
        return position;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Character getCharacter() {
        return character;
    }
}
