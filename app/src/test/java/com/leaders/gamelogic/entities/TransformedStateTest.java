package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class TransformedStateTest {
    @NonNull
    private static Character createCharacter(@Nullable Character character) {
        if (character == null) {
            return Character.create(CharacterType.Nemesis, TeamColor.Black);
        }
        return Character.transform(character, CharacterType.Frog, TeamColor.Black);
    }

    @Test
    public void constructor_sameId_createsState() {
        Character before = createCharacter(null);
        Character after = createCharacter(before);

        TransformedState state = new TransformedState(before, after);

        assertSame(before, state.getCharacterBefore());
        assertSame(after, state.getCharacterAfter());
    }

    @Test
    public void constructor_sameInstanceForBeforeAndAfter_isAccepted() {
        Character character = createCharacter(null);

        TransformedState state = new TransformedState(character, character);

        assertSame(character, state.getCharacterBefore());
        assertSame(character, state.getCharacterAfter());
    }

    @Test
    public void constructor_differentIds_throwsIllegalArgumentException() {
        Character before = createCharacter(null);
        Character after = createCharacter(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TransformedState(before, after)
        );

        assertEquals(
                "Cannot create a transformed state for two different characters",
                exception.getMessage()
        );
    }

    @Test
    public void getters_returnDistinctCharacters() {
        Character before = createCharacter(null);
        Character after = createCharacter(before);

        TransformedState state = new TransformedState(before, after);

        // Guards against before/after being swapped in the constructor or getters
        assertSame(before, state.getCharacterBefore());
        assertSame(after, state.getCharacterAfter());
    }
}