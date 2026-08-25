package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.leaders.R;

public final class ReadOnlyBoardView extends BoardView {
    private boolean isCellPositionVisible;

    public ReadOnlyBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setCellPositionVisible(true);
    }

    public void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        // LongClickListener setter is visible to allow for character description display
        super.setOnCharacterLongClickListener(onCharacterLongClickListener);
    }

    public void setCellPositionVisible(boolean visible) {
        isCellPositionVisible = visible;
        if (visible) {
            imvBoard.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cell_positions));
        } else {
            imvBoard.setForeground(null);
        }
    }

    public boolean isCellPositionVisible() {
        return isCellPositionVisible;
    }
}
