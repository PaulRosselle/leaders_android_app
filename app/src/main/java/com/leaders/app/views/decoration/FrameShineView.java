package com.leaders.app.views.decoration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;

public class FrameShineView extends View {

    private static final int SHINE_ANIMATION_DURATION = 1800;
    private static final float SHINE_ANGLE = -25f;
    private static final float SHINE_PATTERN_RATIO = 1.8f;

    private final Paint shinePaint;
    private final Matrix shineMatrix;
    private LinearGradient shineGradient;
    private float shinePosition;

    private float centerX;
    private float centerY;
    private float travelDistance;

    private ValueAnimator shineAnimator;


    public FrameShineView(Context context) {
        this(context, null);
    }

    public FrameShineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        shinePosition = -1f;

        shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shineMatrix = new Matrix();

        setWillNotDraw(false);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        centerX = width / 2f;
        centerY = height / 2f;

        travelDistance = (float) Math.hypot(width, height) + width;

        createShineShader(width, height);
    }


    private void createShineShader(int width, int height) {
        float size = Math.min(width, height);

        if (size <= 0) {
            return;
        }

        shineGradient = new LinearGradient(
                0f,
                0f,
                size * SHINE_PATTERN_RATIO, // Pattern thickness
                0f,
                getShineColors(),
                getShinePositions(),
                Shader.TileMode.CLAMP
        );

        shinePaint.setShader(shineGradient);

        updateShaderPosition();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (shineAnimator == null || !shineAnimator.isRunning()) {
            return;
        }

        canvas.save();

        canvas.rotate(SHINE_ANGLE, centerX, centerY);

        canvas.drawRect(
                -getWidth(),
                -getHeight(),
                getWidth() * 2f,
                getHeight() * 2f,
                shinePaint
        );

        canvas.restore();
    }


    private void updateShaderPosition() {
        if (shineGradient == null) {
            return;
        }

        float translation = shinePosition * travelDistance;

        shineMatrix.reset();
        shineMatrix.setTranslate(translation, 0f);
        shineGradient.setLocalMatrix(shineMatrix);

        invalidate();
    }

    public void playShine() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
        }

        shinePosition = -1f;
        updateShaderPosition();

        shineAnimator = ValueAnimator.ofFloat(-1f, 1f);
        shineAnimator.setDuration(SHINE_ANIMATION_DURATION);

        shineAnimator.setInterpolator(new AccelerateInterpolator(1.1f));

        shineAnimator.addUpdateListener(animation -> {
            shinePosition = (float) animation.getAnimatedValue();
            updateShaderPosition();
        });

        shineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shinePosition = 1f;
                updateShaderPosition();
            }
        });

        shineAnimator.start();
    }

    public void stopShine() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }

        shinePosition = -1f;

        updateShaderPosition();
    }

    private static float[] getShinePositions() {
        return new float[] {
                0.00f,

                // Large diffused reflexion
                0.07f,
                0.12f,
                0.19f,
                0.26f,

                // Transition
                0.32f,
                0.37f,

                // Main highlight
                0.410f,
                0.425f,
                0.440f,
                0.455f,

                // Diffused reflexion
                0.53f,
                0.61f,
                0.69f,

                // Secondary higlight
                0.755f,
                0.770f,
                0.785f,
                0.805f,

                // End motif
                0.88f,
                0.94f,
                1.00f
        };
    }

    private static int[] getShineColors() {
        int transparent = Color.TRANSPARENT;

        int diffuseVeryLow = Color.argb(4, 255, 255, 255);
        int diffuseLow = Color.argb(8, 255, 255, 255);
        int diffuseMedium = Color.argb(14, 255, 255, 255);

        int highlightSoft = Color.argb(18, 255, 255, 255);
        int highlightMedium = Color.argb(28, 255, 255, 255);
        int highlightBright = Color.argb(45, 255, 255, 255);

        return new int[] {
                transparent,

                transparent,
                diffuseVeryLow,
                diffuseLow,
                transparent,

                transparent,
                diffuseVeryLow,

                transparent,
                highlightSoft,
                highlightBright,
                highlightMedium,

                diffuseVeryLow,
                diffuseMedium,
                transparent,

                transparent,
                highlightSoft,
                highlightMedium,
                transparent,

                transparent,
                transparent,
                transparent
        };
    }


    @Override
    protected void onDetachedFromWindow() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }

        super.onDetachedFromWindow();
    }
}