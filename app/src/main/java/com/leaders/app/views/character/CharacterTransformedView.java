package com.leaders.app.views.character;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

public class CharacterTransformedView extends FrameLayout {
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final CharacterView characterView;
    private final float minRingPx;

    private float ringWidthPx;

    public CharacterTransformedView(@NonNull Context context) {
        this(context, null);
    }

    public CharacterTransformedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CharacterTransformedView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        minRingPx = getResources().getDimensionPixelSize(R.dimen.transformed_piece_min_size);
        ringWidthPx = minRingPx;

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(context.getColor(R.color.frog_transformation_color));

        characterView = new CharacterView(context);
        addView(characterView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Tells the parent view group that this view draws itself
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float size = Math.min(w, h);
        float newRing = Math.max(minRingPx, size * getMaxScaleValue());

        ringWidthPx = newRing;
        ringPaint.setStrokeWidth(newRing);

        float scaleValue = 1f - newRing / size;
        characterView.setScaleX(scaleValue);
        characterView.setScaleY(scaleValue);
    }

    private float getMaxScaleValue() {
        // Android float dimensions must be gotten through a typed value
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.transformed_piece_max_ratio, outValue, true);
        return outValue.getFloat();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        // The outer ring is drawn on top of the character
        float canvasX = getWidth() / 2f;
        float canvasY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - ringWidthPx / 2f;

        canvas.drawCircle(canvasX, canvasY, radius, ringPaint);
    }

    public void setCharacter(@NonNull Character character, @NonNull CharacterSkinType skinType) {
        characterView.setCharacter(character, skinType);
    }

    public void clearCharacter() {
        characterView.setCharacter(
                null,
                CharacterSkinType.Default,
                TeamColor.Black
        );
    }

    @Nullable
    public CharacterType getCharacterType() {
        return characterView.getCharacterType();
    }
}
