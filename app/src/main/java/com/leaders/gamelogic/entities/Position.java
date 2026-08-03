package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a position on the game board of Leaders.
 * <p>
 * The board is a regular hexagon of radius {@value #HEX_RADIUS}. Each position
 * is defined by offset coordinates {@code (x, y)}, and is internally converted
 * to axial/cubic coordinates {@code (q, r, s)} (with {@code q + r + s == 0}),
 * which are more convenient for hexagonal board operations such as distance
 * computation or alignment checks.
 * <p>
 * Instances are immutable and validated at construction: a {@link Position}
 * can only represent a cell that actually lies within the hexagon.
 */
public final class Position {
    private static final int HEX_RADIUS = 3;

    private final int x;
    private final int y;

    private final int q;
    private final int r;
    private final int s;

    /**
     * Creates a position from offset coordinates.
     *
     * @param x the offset x-coordinate
     * @param y the offset y-coordinate
     * @throws IllegalArgumentException if the resulting position falls outside
     *                                   the hexagonal board
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;

        int[] axialCoordinates = toAxial(x, y);
        this.q = axialCoordinates[0];
        this.r = axialCoordinates[1];
        this.s = axialCoordinates[2];

        if (!isWithinHexagon(q, r, s)) {
            throw new IllegalArgumentException("Out of range position: (" + x + ", " + y + ")");
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
     * Converts offset coordinates {@code (x, y)} into axial/cubic coordinates
     * {@code (q, r, s)}.
     *
     * @param x the offset x-coordinate
     * @param y the offset y-coordinate
     * @return an array {@code [q, r, s]} with the corresponding axial coordinates
     */
    private static int[] toAxial(int x, int y) {
        int q = x - HEX_RADIUS;
        int r = Math.max(-HEX_RADIUS, -q - HEX_RADIUS) + y;
        int s = -q - r;
        return new int[]{q, r, s};
    }

    /**
     * Computes the axial/cubic delta between this position and the given one.
     *
     * @param other the other {@link Position}
     * @return an array {@code [deltaQ, deltaR, deltaS]} representing the
     *         component-wise difference between this position and {@code other}
     */
    private int[] deltaTo(@NonNull Position other) {
        return new int[]{other.q - q, other.r - r, other.s - s};
    }

    /**
     * Indicates whether this position and the given one lie on the same
     * straight line along one of the 6 hexagonal directions.
     *
     * @param other the other {@link Position}
     * @return {@code true} if both positions are aligned, {@code false} otherwise
     */
    public boolean isAligned(@NonNull Position other) {
        for (int delta : deltaTo(other)) {
            if (delta == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the exact distance, in number of cells, between this position
     * and the given one on the hexagonal grid.
     *
     * @param other the other {@link Position}
     * @return the distance between the two positions
     */
    public int distanceTo(@NonNull Position other) {
        int[] deltas = deltaTo(other);
        return (Math.abs(deltas[0]) + Math.abs(deltas[1]) + Math.abs(deltas[2])) / 2;
    }

    /**
     * Indicates whether the given offset coordinates lie within the hexagonal
     * board.
     *
     * @param x the offset x-coordinate
     * @param y the offset y-coordinate
     * @return {@code true} if the position is within the hexagon, {@code false} otherwise
     */
    public static boolean isWithinHexagon(int x, int y) {
        int[] axialCoordinates = toAxial(x, y);
        return isWithinHexagon(axialCoordinates[0], axialCoordinates[1], axialCoordinates[2]);
    }

    /**
     * Indicates whether the given axial coordinates lie within the hexagonal
     * board, i.e. within {@value #HEX_RADIUS} cells of the center on each axis.
     *
     * @param q the axial q-coordinate
     * @param r the axial r-coordinate
     * @param s the axial s-coordinate
     * @return {@code true} if the position is within the hexagon, {@code false} otherwise
     */
    public static boolean isWithinHexagon(int q, int r, int s) {
        return Math.abs(q) <= HEX_RADIUS && Math.abs(r) <= HEX_RADIUS && Math.abs(s) <= HEX_RADIUS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @NonNull
    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + ", q=" + q + ", r=" + r + ", s=" + s + '}';
    }
}