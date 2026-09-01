package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import java.util.List;

public final class CharacterPath {
    @NonNull
    private final List<Position> path;

    public CharacterPath(@NonNull List<Position> path) {
        if (path.size() < 2) {
            throw new IllegalArgumentException("A path require at least a start and a destination");
        }

        this.path = List.copyOf(path);
    }

    @NonNull
    public List<Position> getPositions() {
        return path;
    }

    @NonNull
    public Position getStart() {
        return path.get(0);
    }

    @NonNull
    public Position getDestination() {
        return path.get(path.size() - 1);
    }
}
