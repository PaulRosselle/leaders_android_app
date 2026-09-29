package com.leaders.app.views.duel;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;

public class PlayerHaloView extends View {
    // Base bitmap dimensions
    private static final float BASE_WIDTH = 501f;
    private static final float BASE_HEIGHT = 369f;

    // Polygon coordinates in base bitmap
    private static final float TOP_LEFT_X = 0f;
    private static final float TOP_LEFT_Y = 0f;

    private static final float TOP_RIGHT_X = 499f;
    private static final float TOP_RIGHT_Y = 291f;

    private static final float BOTTOM_RIGHT_X = 499f;
    private static final float BOTTOM_RIGHT_Y = 367f;

    private static final float BOTTOM_LEFT_X = 0f;
    private static final float BOTTOM_LEFT_Y = 367f;

    private static final float HALO_DEFAULT_WIDTH_IN_DP = 8f;

    private final Paint polygonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path polygonPath = new Path();

    private float haloWidthPx;
    private BlurMaskFilter blurMaskFilter;


    public PlayerHaloView(Context context) {
        super(context);
        init();
    }

    public PlayerHaloView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerHaloView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setHaloWidthDp(HALO_DEFAULT_WIDTH_IN_DP);

        int color = getContext().getColor(R.color.app_golden);
        polygonPaint.setAntiAlias(true);
        polygonPaint.setStyle(Paint.Style.FILL);
        polygonPaint.setColor(color);

        haloPaint.setAntiAlias(true);
        haloPaint.setStyle(Paint.Style.FILL);
        haloPaint.setColor(color);


        // Required for the use of a BlurMaskFilter
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        final float viewWidth = getWidth();
        final float viewHeight = getHeight();

        if (viewWidth <= 0f || viewHeight <= 0f) {
            return;
        }

        final float contentWidth = viewWidth - haloWidthPx;
        final float contentHeight = viewHeight - 2f * haloWidthPx;

        if (contentWidth <= 0f || contentHeight <= 0f) {
            return;
        }

        // The polygon is scaled from the base bitmap dimensions to the view's dimensions
        final float scale = Math.min(contentWidth / BASE_WIDTH, contentHeight / BASE_HEIGHT);

        if (scale <= 0f) {
            return;
        }

        final float offsetX = 0f;
        final float offsetY = (viewHeight - (BASE_HEIGHT * scale)) / 2f;

        // The polygon is build as a path
        polygonPath.reset();

        polygonPath.moveTo(
                offsetX + TOP_LEFT_X * scale,
                offsetY + TOP_LEFT_Y * scale
        );

        polygonPath.lineTo(
                offsetX + TOP_RIGHT_X * scale,
                offsetY + TOP_RIGHT_Y * scale
        );

        polygonPath.lineTo(
                offsetX + BOTTOM_RIGHT_X * scale,
                offsetY + BOTTOM_RIGHT_Y * scale
        );

        polygonPath.lineTo(
                offsetX + BOTTOM_LEFT_X * scale,
                offsetY + BOTTOM_LEFT_Y * scale
        );

        polygonPath.close();

        // Important : the halo is not multiplied by the scale since we want its
        // width to be used for the final display
        haloPaint.setMaskFilter(blurMaskFilter);

        final int saveCount = canvas.saveLayer(
                0f,
                0f,
                viewWidth,
                viewHeight,
                null
        );

        canvas.drawPath(polygonPath, haloPaint);

        canvas.restoreToCount(saveCount);

        haloPaint.setMaskFilter(null);

        canvas.drawPath(polygonPath, polygonPaint);
    }

    public void setHaloWidthPx(float haloWidthPx) {
        if (haloWidthPx < 0f) {
            throw new IllegalArgumentException("haloWidthPx must be >= 0");
        }

        this.haloWidthPx = haloWidthPx;
        blurMaskFilter = new BlurMaskFilter(haloWidthPx, BlurMaskFilter.Blur.NORMAL);

        invalidate();
        requestLayout();
    }

    public void setHaloWidthDp(float haloWidthDp) {
        setHaloWidthPx(dpToPx(haloWidthDp));
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // We override the onMeasure so the view can autosize and respect the base bitmap ratio
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth;
        int measuredHeight;

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
            measuredHeight = heightSize;
        } else if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
            final float contentWidth = Math.max(0f, measuredWidth - haloWidthPx);

            final float contentHeight = contentWidth * BASE_HEIGHT / BASE_WIDTH;

            measuredHeight = Math.round(contentHeight + 2f * haloWidthPx);

            if (heightMode == MeasureSpec.AT_MOST) {
                measuredHeight = Math.min(measuredHeight, heightSize);
            }

        } else if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;

            final float contentHeight = Math.max(0f, measuredHeight - 2f * haloWidthPx);

            final float contentWidth = contentHeight * BASE_WIDTH / BASE_HEIGHT;

            measuredWidth = Math.round(contentWidth + haloWidthPx);

            if (widthMode == MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(measuredWidth, widthSize);
            }

        } else {
            measuredWidth = Math.round(BASE_WIDTH + haloWidthPx);

            measuredHeight = Math.round(BASE_HEIGHT + 2f * haloWidthPx);

            if (widthMode == MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(measuredWidth, widthSize);
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                measuredHeight = Math.min(measuredHeight, heightSize);
            }
        }

        setMeasuredDimension(
                resolveSize(measuredWidth, widthMeasureSpec),
                resolveSize(measuredHeight, heightMeasureSpec)
        );
    }
}
