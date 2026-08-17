package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.leaders.gamelogic.entities.Character;

public final class SerializationContext {
    // When serialized, an instance of a character is identified by its UUID.
    // To deserialize it to a single action, we use a dedicated map in the context
    @NonNull
    private final Map<UUID, Character> charactersMap;

    public SerializationContext() {
        this.charactersMap = new HashMap<>();
    }

    @NonNull
    public Map<UUID, Character> getCharactersMap() {
        return charactersMap;
    }
}
