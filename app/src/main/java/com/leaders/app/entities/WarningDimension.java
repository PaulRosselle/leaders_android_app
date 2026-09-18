package com.leaders.app.entities;

import android.graphics.PointF;

import androidx.annotation.NonNull;

public class WarningDimension {
    @NonNull
    private final PointF a;
    @NonNull
    private final PointF b;
    @NonNull
    private final PointF c;

    public WarningDimension(@NonNull PointF a, @NonNull PointF b, @NonNull PointF c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public WarningDimension(float ax, float ay, float bx, float by, float cx, float cy) {
        this(new PointF(ax, ay), new PointF(bx, by), new PointF(cx, cy));
    }

    /**
     * Checks whether a point is inside or on the boundary of the triangle.
     *
     * @param px the x-coordinate of the point
     * @param py the y-coordinate of the point
     * @return {@code true} if the point is inside or on the boundary, otherwise {@code false}
     */
    public boolean contains(float px, float py) {
        float d1 = crossProduct(px, py, a.x, a.y, b.x, b.y);
        float d2 = crossProduct(px, py, b.x, b.y, c.x, c.y);
        float d3 = crossProduct(px, py, c.x, c.y, a.x, a.y);

        boolean hasNegative = (d1 < 0f) || (d2 < 0f) || (d3 < 0f);
        boolean hasPositive = (d1 > 0f) || (d2 > 0f) || (d3 > 0f);

        return !(hasNegative && hasPositive);
    }

    /**
     * Computes the cross product of two vectors defined by a point and two other points.
     *
     * @param px the x-coordinate of the point
     * @param py the y-coordinate of the point
     * @param ax the x-coordinate of the first point
     * @param ay the y-coordinate of the first point
     * @param bx the x-coordinate of the second point
     * @param by the y-coordinate of the second point
     * @return the cross product value
     */
    private static float crossProduct(float px, float py,
                                      float ax, float ay,
                                      float bx, float by) {
        return (px - bx) * (ay - by) - (ax - bx) * (py - by);
    }
}
