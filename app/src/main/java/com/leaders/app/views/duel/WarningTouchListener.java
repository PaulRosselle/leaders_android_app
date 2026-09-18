package com.leaders.app.views.duel;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.leaders.app.entities.WarningDimension;

public final class WarningTouchListener implements View.OnTouchListener {
    private static final float REFERENCE_WIDTH = 501f;
    private static final float REFERENCE_HEIGHT = 369f;

    @NonNull
    private final WarningDimension dimension;
    @NonNull
    private final Matrix inverseMatrix = new Matrix();
    @NonNull
    private final float[] point = new float[2];
    @NonNull
    private final Runnable onWarningTouch;

    public WarningTouchListener(@NonNull WarningDimension dimension, @NonNull Runnable onWarningTouch) {
        this.dimension = dimension;
        this.onWarningTouch = onWarningTouch;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // We're only detecting when the user touches down the view
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        if (!(v instanceof ImageView)) {
            return false;
        }
        ImageView imageView = (ImageView) v;

        // Adjusted view coordinates
        float x = event.getX() - imageView.getPaddingLeft();
        float y = event.getY() - imageView.getPaddingTop();

        // View loaded bitmap coordinates
        if (!imageView.getImageMatrix().invert(inverseMatrix)) {
            return false;
        }
        point[0] = x;
        point[1] = y;
        inverseMatrix.mapPoints(point);

        // Original bitmap coordinates
        Drawable drawable = imageView.getDrawable();
        float scaleX = REFERENCE_WIDTH / drawable.getIntrinsicWidth();
        float scaleY = REFERENCE_HEIGHT / drawable.getIntrinsicHeight();
        point[0] *= scaleX;
        point[1] *= scaleY;

        // If the click is within the warning dimension, we can run "onTouch" runnable
        if (dimension.contains(point[0], point[1])) {
            onWarningTouch.run();
            v.performClick();
            return true;
        }

        // If outside the warning dimension, we let the event propagate
        return false;
    }
}