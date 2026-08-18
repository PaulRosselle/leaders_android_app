package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.leaders.R;

public final class PuzzleEditorBoardView extends BoardView {
    public PuzzleEditorBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        imvBoard.setImageResource(R.drawable.board_editor);
        imvBoard.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cell_positions));
    }
}
