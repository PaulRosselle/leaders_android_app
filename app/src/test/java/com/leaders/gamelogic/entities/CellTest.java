package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class CellTest {

    @Test
    public void constructor_shouldStorePosition() {
        Position position = new Position(3, 3);

        Cell cell = new Cell(position);

        assertSame(position, cell.getPosition());
    }

    @Test
    public void constructor_shouldInitializeWithoutCharacter() {
        Cell cell = new Cell(new Position(3, 3));

        assertNull(cell.getCharacter());
    }

    @Test
    public void setCharacter_shouldStoreCharacter() {
        Cell cell = new Cell(new Position(3, 3));
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        cell.setCharacter(character);

        assertSame(character, cell.getCharacter());
    }

    @Test
    public void setCharacter_shouldAllowRemovingCharacter() {
        Cell cell = new Cell(new Position(3, 3));
        Character character = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );

        cell.setCharacter(character);
        cell.setCharacter(null);

        assertNull(cell.getCharacter());
    }

    @Test
    public void setCharacter_shouldReplaceExistingCharacter() {
        Cell cell = new Cell(new Position(3, 3));

        Character first = Character.create(
                CharacterType.LeaderQueen,
                TeamColor.Black
        );
        Character second = Character.create(
                CharacterType.LeaderKing,
                TeamColor.White
        );

        cell.setCharacter(first);
        cell.setCharacter(second);

        assertSame(second, cell.getCharacter());
    }
}