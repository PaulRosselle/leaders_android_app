package com.leaders.gamelogic.enums;

import java.util.NoSuchElementException;

public enum Direction {
    Top (0, -1, 1),
    TopRight (1, -1, 0),
    BottomRight (1, 0, -1),
    Bottom (0, 1, -1),
    BottomLeft (-1, 1, 0),
    TopLeft (-1, 0, 1);

    private static final Direction[] CLOCKWISE_DIRECTIONS = {
        Top,
        TopRight,
        BottomRight,
        Bottom,
        BottomLeft,
        TopLeft
    };

    private final int q;
    private final int r;
    private final int s;

    Direction(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }

    /**
     * Returns the direction opposite to this one.
     *
     * @return the opposite {@link Direction}
     * @throws NoSuchElementException if no opposite direction is found
     */
    public Direction getOpposite() {
        return CLOCKWISE_DIRECTIONS[(ordinal() + CLOCKWISE_DIRECTIONS.length / 2) % CLOCKWISE_DIRECTIONS.length];
    }

    /**
     * Returns the next direction following this one around the compass.
     *
     * @param clockwise {@code true} to get the next direction clockwise,
     *                  {@code false} for counter-clockwise
     * @return the next {@link Direction}
     */
    public Direction getNext(boolean clockwise) {
        int offset = clockwise ? 1 : -1;
        return CLOCKWISE_DIRECTIONS[Math.floorMod(ordinal() + offset, CLOCKWISE_DIRECTIONS.length)];
    }
}
