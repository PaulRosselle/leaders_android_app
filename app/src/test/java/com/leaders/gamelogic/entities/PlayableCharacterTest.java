package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class PlayableCharacterTest {

    @Test
    public void constructor_shouldStoreCharacter() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Position position = new Position(3, 3);

        PlayableCharacter state =
                new PlayableCharacter(character, position, true, false);

        assertSame(character, state.getCharacter());
    }

    @Test
    public void constructor_shouldStorePosition() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Position position = new Position(3, 3);

        PlayableCharacter state =
                new PlayableCharacter(character, position, true, false);

        assertSame(position, state.getPosition());
    }

    @Test
    public void constructor_shouldStoreMandatoryState() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Position position = new Position(3, 3);

        PlayableCharacter state =
                new PlayableCharacter(character, position, true, false);

        assertTrue(state.isMandatory());
    }

    @Test
    public void constructor_shouldStoreCanUseActiveAbilityState() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Position position = new Position(3, 3);

        PlayableCharacter state =
                new PlayableCharacter(character, position, false, true);

        assertTrue(state.canUseActiveAbility());
    }

    @Test
    public void constructor_shouldStoreFalseBooleanStates() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Position position = new Position(3, 3);

        PlayableCharacter state =
                new PlayableCharacter(character, position, false, false);

        assertFalse(state.isMandatory());
        assertFalse(state.canUseActiveAbility());
    }
}