package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ReadOnlyBoardView extends BoardView {

    public ReadOnlyBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setCellPositionVisible(true);
    }

    public void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        // LongClickListener setter is visible to allow for character description display
        super.setOnCharacterLongClickListener(onCharacterLongClickListener);
    }
}
