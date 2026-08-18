package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public abstract class BoardView extends ConstraintLayout {
    protected ImageView imvBoard;

    public BoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_board, this);
    }

    private void initViews() {
        imvBoard = findViewById(R.id.imvBoard_vwBoard);
        // TODO - init CellView list
        // TODO - init CharacterView pool
        // TODO - init ShadowView pool
        // TODO - init CharacterSelection view
    }
}
