package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.puzzlelogic.enums.PuzzleLifetime;
import com.leaders.puzzlelogic.utilities.GameStateHashUtils;

public class PuzzleIdentity {
    @NonNull
    private final String name;
    @NonNull
    private final PuzzleLifetime lifetime;
    @NonNull
    private final String stateHash;

    public PuzzleIdentity(@NonNull PuzzleSave puzzleSave) {
        this.name = puzzleSave.getName();
        this.lifetime = puzzleSave.getLifetime();

        GameHistory gameHistory = puzzleSave.getPuzzleGameHistory();
        Game game = GameFactory.create(gameHistory);

        this.stateHash = GameStateHashUtils.createHash(game, gameHistory);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PuzzleIdentity)) {
            return false;
        }

        PuzzleIdentity other = (PuzzleIdentity) obj;

        return lifetime == other.lifetime && stateHash.equals(other.stateHash);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(lifetime, stateHash);
    }
}
