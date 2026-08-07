package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class CharacterTest {

    @Test
    public void create_shouldGenerateUniqueId() {
        Character first = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Character second = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    public void create_shouldSetCharacterType() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        assertEquals(CharacterType.LeaderQueen, character.getCharacterType());
    }

    @Test
    public void create_shouldSetTeamColor() {
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        assertEquals(TeamColor.Black, character.getTeamColor());
    }

    @Test
    public void transform_shouldCreateNewInstance() {
        Character source = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        Character transformed = Character.transform(
                source,
                CharacterType.LeaderKing,
                TeamColor.White
        );

        assertNotSame(source, transformed);
    }

    @Test
    public void transform_shouldPreserveId() {
        Character source = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        Character transformed = Character.transform(
                source,
                CharacterType.LeaderKing,
                TeamColor.White
        );

        assertEquals(source.getId(), transformed.getId());
    }

    @Test
    public void transform_shouldChangeCharacterType() {
        Character source = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        Character transformed = Character.transform(
                source,
                CharacterType.LeaderKing,
                TeamColor.Black
        );

        assertEquals(CharacterType.LeaderKing, transformed.getCharacterType());
    }

    @Test
    public void transform_shouldChangeTeamColor() {
        Character source = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        Character transformed = Character.transform(
                source,
                CharacterType.LeaderQueen,
                TeamColor.White
        );

        assertEquals(TeamColor.White, transformed.getTeamColor());
    }

    @Test
    public void transform_shouldPreserveUnchangedValues() {
        Character source = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        Character transformed = Character.transform(
                source,
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        assertEquals(source.getId(), transformed.getId());
        assertEquals(source.getCharacterType(), transformed.getCharacterType());
        assertEquals(source.getTeamColor(), transformed.getTeamColor());
        assertNotSame(source, transformed);
    }
}