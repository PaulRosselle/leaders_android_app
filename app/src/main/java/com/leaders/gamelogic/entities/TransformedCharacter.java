package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

public class TransformedCharacter {
    @NonNull
    private final Position position;
    @NonNull
    private final TransformedState transformedState;

    public TransformedCharacter(@NonNull Position position, @NonNull TransformedState transformedState) {
        this.position = position;
        this.transformedState = transformedState;
    }

    @NonNull
    public Position getPosition() {
        return position;
    }

    @NonNull
    public TransformedState getTransformedState() {
        return transformedState;
    }
}
