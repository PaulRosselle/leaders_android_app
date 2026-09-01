package com.leaders.app.views.character;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

public class CharacterShineView extends View {
    private static final int SHINE_ANIMATION_DURATION = 600;
    private static final float SHINE_THICKNESS_RATIO = 0.26f;

    private final Paint shinePaint;
    private LinearGradient shineGradient;
    private final Matrix shineMatrix;
    private float shinePosition;

    private final Path clipPath;

    private float centerX;
    private float centerY;

    private ValueAnimator shineAnimator;



    public CharacterShineView(Context context) {
        this(context, null);
    }

    public CharacterShineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        shinePosition = -1f;
        clipPath = new Path();
        shineMatrix = new Matrix();
        shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setWillNotDraw(false);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        centerX = width / 2f;
        centerY = height / 2f;
        float radius = Math.min(width, height) * 0.42f;

        clipPath.reset();
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW);

        createShineShader(width, height);
    }

    private void createShineShader(int width, int height) {
        float size = Math.min(width, height);
        if (size <= 0) {
            return;
        }

        float shineThickness = size * SHINE_THICKNESS_RATIO;

        int edgeColor = Color.TRANSPARENT;
        int middleColor = Color.argb(30, 255, 255, 255);
        int centerColor = Color.argb(120, 255, 255, 255);

        int[] colorValues = new int[] { edgeColor, middleColor, centerColor, middleColor, edgeColor };
        float[] colorPositions = new float[] {0.0f, 0.30f, 0.50f, 0.70f, 1.0f};

        shineGradient = new LinearGradient(
                -shineThickness, 0, shineThickness, 0,
                colorValues, colorPositions, Shader.TileMode.CLAMP
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

        if (shineAnimator != null && shineAnimator.isRunning()) {
            canvas.save();

            canvas.clipPath(clipPath);
            canvas.rotate(-25f, centerX, centerY);
            canvas.drawRect(-getWidth(), -getHeight(), getWidth() * 2f, getHeight() * 2f, shinePaint);

            canvas.restore();
        }
    }

    private void updateShaderPosition() {
        if (shineGradient == null) {
            return;
        }

        // The shadow position goes from -1 to 1
        float translation = shinePosition * getWidth();

        shineMatrix.reset();
        shineMatrix.setTranslate(translation, 0);
        shineGradient.setLocalMatrix(shineMatrix);

        invalidate();
    }

    public void playShine() {
        // Si une animation est déjà en cours, on la redémarre proprement.
        if (shineAnimator != null) {
            shineAnimator.cancel();
        }

        shinePosition = -1f;
        updateShaderPosition();

        shineAnimator = ValueAnimator.ofFloat(-1f, 1f);
        shineAnimator.setDuration(SHINE_ANIMATION_DURATION);
        shineAnimator.setInterpolator(new DecelerateInterpolator(1.2f));

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

    @Override
    protected void onDetachedFromWindow() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }

        super.onDetachedFromWindow();
    }
}