package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DirectionTest {

    @Test
    public void directions_shouldHaveExpectedAxialCoordinates() {
        assertEquals(0, Direction.Top.getQ());
        assertEquals(-1, Direction.Top.getR());
        assertEquals(1, Direction.Top.getS());

        assertEquals(1, Direction.TopRight.getQ());
        assertEquals(-1, Direction.TopRight.getR());
        assertEquals(0, Direction.TopRight.getS());

        assertEquals(1, Direction.BottomRight.getQ());
        assertEquals(0, Direction.BottomRight.getR());
        assertEquals(-1, Direction.BottomRight.getS());

        assertEquals(0, Direction.Bottom.getQ());
        assertEquals(1, Direction.Bottom.getR());
        assertEquals(-1, Direction.Bottom.getS());

        assertEquals(-1, Direction.BottomLeft.getQ());
        assertEquals(1, Direction.BottomLeft.getR());
        assertEquals(0, Direction.BottomLeft.getS());

        assertEquals(-1, Direction.TopLeft.getQ());
        assertEquals(0, Direction.TopLeft.getR());
        assertEquals(1, Direction.TopLeft.getS());
    }

    @Test
    public void getOpposite_shouldReturnOppositeDirection() {
        assertEquals(Direction.Bottom, Direction.Top.getOpposite());
        assertEquals(Direction.BottomLeft, Direction.TopRight.getOpposite());
        assertEquals(Direction.TopLeft, Direction.BottomRight.getOpposite());

        assertEquals(Direction.Top, Direction.Bottom.getOpposite());
        assertEquals(Direction.TopRight, Direction.BottomLeft.getOpposite());
        assertEquals(Direction.BottomRight, Direction.TopLeft.getOpposite());
    }

    @Test
    public void getNextClockwise_shouldReturnNextDirection() {
        assertEquals(Direction.TopRight, Direction.Top.getNext(true));
        assertEquals(Direction.BottomRight, Direction.TopRight.getNext(true));
        assertEquals(Direction.Bottom, Direction.BottomRight.getNext(true));
        assertEquals(Direction.BottomLeft, Direction.Bottom.getNext(true));
        assertEquals(Direction.TopLeft, Direction.BottomLeft.getNext(true));
        assertEquals(Direction.Top, Direction.TopLeft.getNext(true));
    }

    @Test
    public void getNextCounterClockwise_shouldReturnPreviousDirection() {
        assertEquals(Direction.TopLeft, Direction.Top.getNext(false));
        assertEquals(Direction.Top, Direction.TopRight.getNext(false));
        assertEquals(Direction.TopRight, Direction.BottomRight.getNext(false));
        assertEquals(Direction.BottomRight, Direction.Bottom.getNext(false));
        assertEquals(Direction.Bottom, Direction.BottomLeft.getNext(false));
        assertEquals(Direction.BottomLeft, Direction.TopLeft.getNext(false));
    }

    @Test
    public void getOpposite_shouldBeInvolution() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, direction.getOpposite().getOpposite());
        }
    }

    @Test
    public void getNextClockwise_shouldWrapAround() {
        assertEquals(Direction.Top, Direction.TopLeft.getNext(true));
    }

    @Test
    public void getNextCounterClockwise_shouldWrapAround() {
        assertEquals(Direction.TopLeft, Direction.Top.getNext(false));
    }
}