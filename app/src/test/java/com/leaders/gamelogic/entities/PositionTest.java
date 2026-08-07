package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.enums.Direction;

import org.junit.Test;

public class PositionTest {

    @Test
    public void constructor_shouldPreserveOffsetCoordinates() {
        Position position = new Position(3, 2);

        assertEquals(3, position.getX());
        assertEquals(2, position.getY());
    }

    @Test
    public void constructor_shouldConvertOffsetToAxialCoordinates() {
        Position centralPosition = new Position(3, 3);

        assertEquals(0, centralPosition.getQ());
        assertEquals(0, centralPosition.getR());
        assertEquals(0, centralPosition.getS());


        Position otherPosition = new Position(3, 2);

        assertEquals(0, otherPosition.getQ());
        assertEquals(-1, otherPosition.getR());
        assertEquals(1, otherPosition.getS());
    }

    @Test
    public void constructor_shouldAcceptCenterPosition() {
        Position position = new Position(3, 3);

        assertEquals(0, position.getQ());
        assertEquals(0, position.getR());
        assertEquals(0, position.getS());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldRejectPositionOutsideBoard() {
        new Position(9, 9);
    }

    @Test
    public void translated_shouldReturnTranslatedPosition() {
        Position position = new Position(3, 3);

        Position translated = position.translated(1, -1, 0);

        assertEquals(new Position(4, 2), translated);
    }

    @Test
    public void translated_shouldReturnNullWhenResultIsOutsideBoard() {
        Position position = new Position(3, 0);

        assertNull(position.translated(0, -1, 1));
    }

    @Test
    public void adjacent_shouldReturnPositionInGivenDirection() {
        Position position = new Position(3, 3);

        assertEquals(
                new Position(3, 2),
                position.adjacent(Direction.Top)
        );

        assertEquals(
                new Position(4, 2),
                position.adjacent(Direction.TopRight)
        );

        assertEquals(
                new Position(4, 3),
                position.adjacent(Direction.BottomRight)
        );

        assertEquals(
                new Position(3, 4),
                position.adjacent(Direction.Bottom)
        );

        assertEquals(
                new Position(2, 3),
                position.adjacent(Direction.BottomLeft)
        );

        assertEquals(
                new Position(2, 2),
                position.adjacent(Direction.TopLeft)
        );
    }

    @Test
    public void adjacent_shouldReturnNullWhenDirectionLeadsOutsideBoard() {
        Position position = new Position(3, 0);

        assertNull(position.adjacent(Direction.Top));
        assertNotNull(position.adjacent(Direction.Bottom));
    }

    @Test
    public void isAligned_shouldReturnTrueForPositionsOnSameAxis() {
        Position center = new Position(3, 3);

        assertTrue(center.isAligned(new Position(3, 0)));
        assertTrue(center.isAligned(new Position(6, 3)));
        assertTrue(center.isAligned(new Position(0, 3)));
    }

    @Test
    public void isAligned_shouldReturnFalseForNonAlignedPositions() {
        Position center = new Position(3, 3);

        assertFalse(center.isAligned(new Position(4, 4)));
    }

    @Test
    public void isAligned_shouldReturnTrueForSamePosition() {
        Position position = new Position(3, 3);

        assertTrue(position.isAligned(new Position(3, 3)));
    }

    @Test
    public void distanceTo_shouldReturnZeroForSamePosition() {
        Position position = new Position(3, 3);

        assertEquals(0, position.distanceTo(new Position(3, 3)));
    }

    @Test
    public void distanceTo_shouldReturnCorrectDistanceForAlignedPositions() {
        Position center = new Position(3, 3);

        assertEquals(3, center.distanceTo(new Position(3, 0)));
        assertEquals(3, center.distanceTo(new Position(6, 3)));
        assertEquals(3, center.distanceTo(new Position(0, 3)));
    }

    @Test
    public void distanceTo_shouldReturnCorrectDistanceForNonAlignedPositions() {
        Position first = new Position(3, 3);
        Position second = new Position(5, 4);

        assertEquals(3, first.distanceTo(second));
    }

    @Test
    public void isWithinHexagon_shouldAcceptValidOffsetCoordinates() {
        assertTrue(Position.isWithinHexagon(3, 3));
        assertTrue(Position.isWithinHexagon(0, 3));
        assertTrue(Position.isWithinHexagon(6, 3));
        assertTrue(Position.isWithinHexagon(3, 0));
        assertTrue(Position.isWithinHexagon(3, 6));
    }

    @Test
    public void isWithinHexagon_shouldRejectInvalidOffsetCoordinates() {
        assertFalse(Position.isWithinHexagon(0, 4));
        assertFalse(Position.isWithinHexagon(9, 9));
        assertFalse(Position.isWithinHexagon(-1, 3));
        assertFalse(Position.isWithinHexagon(7, 3));
    }

    @Test
    public void isWithinHexagon_shouldValidateAxialCoordinates() {
        assertTrue(Position.isWithinHexagon(0, 0, 0));
        assertTrue(Position.isWithinHexagon(3, 0, -3));
        assertTrue(Position.isWithinHexagon(-3, 3, 0));

        assertFalse(Position.isWithinHexagon(3, 3, 1));
        assertFalse(Position.isWithinHexagon(4, 0, -4));
    }

    @Test
    public void equals_shouldReturnTrueForSameCoordinates() {
        Position first = new Position(3, 3);
        Position second = new Position(3, 3);

        assertEquals(first, second);
    }

    @Test
    public void equals_shouldReturnFalseForDifferentCoordinates() {
        Position first = new Position(3, 3);
        Position second = new Position(4, 3);

        assertNotEquals(first, second);
    }

    @Test
    public void hashCode_shouldBeEqualForEqualPositions() {
        Position first = new Position(3, 3);
        Position second = new Position(3, 3);

        assertEquals(first.hashCode(), second.hashCode());
    }
}