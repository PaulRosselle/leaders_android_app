package com.leaders.gamelogic.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

public enum Direction {
    Top,
    TopRight,
    BottomRight,
    Bottom,
    BottomLeft,
    TopLeft;

    /**
     * Returns the direction opposite to this one.
     *
     * @return the opposite {@link Direction}
     * @throws NoSuchElementException if no opposite direction is found
     */
    public Direction getOpposite() {
        switch (this) {
            case Top: return Bottom;
            case TopRight: return BottomLeft;
            case BottomRight: return TopLeft;
            case Bottom: return Top;
            case BottomLeft: return TopRight;
            case TopLeft: return BottomRight;
            default: throw new NoSuchElementException(String.format("No opposite direction found for %s", this));
        }
    }

    /**
     * Indicates whether this direction lies on the vertical axis (top or bottom).
     */
    public boolean isSameColumn() {
        return this == Direction.Top || this == Direction.Bottom;
    }

    /**
     * Indicates whether this direction points towards the top.
     */
    public boolean isTop() {
        return this == Direction.Top || this == Direction.TopLeft || this == Direction.TopRight;
    }

    /**
     * Indicates whether this direction points towards the left.
     */
    public boolean isLeft() {
        return this == Direction.TopLeft || this == Direction.BottomLeft;
    }

    /**
     * Returns the next direction following this one around the compass.
     *
     * @param clockwise {@code true} to get the next direction clockwise,
     *                  {@code false} for counter-clockwise
     * @return the next {@link Direction}
     */
    public Direction getNext(boolean clockwise) {
        // We add every direction in clockwise order.
        // For the function to cycle automatically, we add again the first direction at the end of the list
        ArrayList<Direction> directionsInOrder = new ArrayList<>(
                Arrays.asList(Direction.Top, Direction.TopRight, Direction.BottomRight,
                    Direction.Bottom, Direction.BottomLeft, Direction.TopLeft, Direction.Top)
        );
        // We just have to reverse the list order to get the next direction counter-clockwise
        if (!clockwise) {
            Collections.reverse(directionsInOrder);
        }
        // Since the last direction is always a repetition of the first one, we are
        // sure to never get out of bounds by getting the next direction with the enum ord + 1
        return directionsInOrder.get(this.ordinal() + 1);
    }
}
