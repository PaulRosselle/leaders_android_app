package com.leaders.app.views.board;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;

public final class ReadOnlyBoardView extends BoardView {

    public ReadOnlyBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.ReadOnlyBoardView)) {
            setCellPositionVisible(
                    customAttrs.getBoolean(R.styleable.ReadOnlyBoardView_position_visible, true)
            );
        }
    }

    public void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        // LongClickListener setter is visible to allow for character description display
        super.setOnCharacterLongClickListener(onCharacterLongClickListener);
    }
}
