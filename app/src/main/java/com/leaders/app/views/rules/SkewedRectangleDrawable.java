package com.leaders.app.views.rules;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class SkewedRectangleDrawable extends Drawable {
    public enum CutSide {
        Left,
        Right
    }

    @NonNull
    private final Paint fillPaint;
    @NonNull
    private final Paint strokePaint;
    @NonNull
    private final Path path;

    private float cutSize;
    @NonNull
    private CutSide cutSide;
    private float strokeWidth;

    @ColorInt
    private int fillColor;

    @ColorInt
    private int strokeColor;

    public SkewedRectangleDrawable(@ColorInt int fillColor,
                                   @ColorInt int strokeColor,
                                   float cutSize,
                                   @NonNull CutSide cutSide,
                                   float strokeWidth) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.cutSize = cutSize;
        this.cutSide = cutSide;
        this.strokeWidth = strokeWidth;

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
        strokePaint.setStrokeCap(Paint.Cap.BUTT);

        path = new Path();

        updatePaints();
    }

    private void updatePaints() {
        fillPaint.setColor(fillColor);

        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        float halfStroke = strokeWidth / 2f;

        float left = bounds.left + halfStroke;
        float top = bounds.top + halfStroke;
        float right = bounds.right - halfStroke;
        float bottom = bounds.bottom - halfStroke;

        // Cut size cannot exceed the drawable size
        float actualCutSize = Math.min(cutSize, Math.max(0f, right - left));

        path.reset();

        switch (cutSide) {
            case Right: {
                path.moveTo(left, top);
                path.lineTo(right - actualCutSize, top);
                path.lineTo(right, bottom);
                path.lineTo(left, bottom);
            } break;
            case Left: {
                path.moveTo(left + actualCutSize, top);
                path.lineTo(right, top);
                path.lineTo(right, bottom);
                path.lineTo(left, bottom);
            } break;
            default: throw new IllegalStateException("Cut side not handled: " + cutSide);
        }

        path.close();

        // Background color
        canvas.drawPath(path, fillPaint);

        // Stroke
        if (strokeWidth > 0f) {
            canvas.drawPath(path, strokePaint);
        }
    }

    //region GETTERS AND SETTERS

    public void setCutSize(float cutSize) {
        this.cutSize = Math.max(0f, cutSize);
        invalidateSelf();
    }

    public float getCutSize() {
        return cutSize;
    }

    public void setCutSide(@NonNull CutSide cutSide) {
        this.cutSide = cutSide;
        invalidateSelf();
    }

    @NonNull
    public CutSide getCutSide() {
        return cutSide;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = Math.max(0f, strokeWidth);
        updatePaints();
        invalidateSelf();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setFillColor(@ColorInt int fillColor) {
        this.fillColor = fillColor;
        fillPaint.setColor(fillColor);
        invalidateSelf();
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        this.strokeColor = strokeColor;
        strokePaint.setColor(strokeColor);
        invalidateSelf();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    //endregion

    //region DRAWABLE OVERRIDEN METHODS

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    //endregion
}