package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BoardView extends ConstraintLayout {
    private static final float CHARACTER_SIZE_RATIO = 0.1410f;

    protected ImageView imvBoard;

    private final List<CellView> cellViews;
    private final List<ImageView> characterShadowViews;
    private final List<CharacterView> characterViews;

    @NonNull
    protected Map<Position, CellView> cellViewsMap;
    @NonNull
    protected BoardOrientation orientation;


    public BoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_board, this);

        cellViews = new ArrayList<>();
        characterViews = new ArrayList<>();
        characterShadowViews = new ArrayList<>();
        cellViewsMap = new HashMap<>();
        orientation = BoardOrientation.Default;

        initViews();
    }

    private void initViews() {
        imvBoard = findViewById(R.id.imvBoard_vwBoard);
        initCellViews();
        initCharacterViews();
        initCharacterShadowViews();
        // TODO - init CharacterHighlight views
    }

    private int[] getCellViewIds() {
        return new int[] {
                R.id.cvC0R0_vwBoard, R.id.cvC0R1_vwBoard, R.id.cvC0R2_vwBoard, R.id.cvC0R3_vwBoard,

                R.id.cvC1R0_vwBoard, R.id.cvC1R1_vwBoard, R.id.cvC1R2_vwBoard, R.id.cvC1R3_vwBoard,
                R.id.cvC1R4_vwBoard,

                R.id.cvC2R0_vwBoard, R.id.cvC2R1_vwBoard, R.id.cvC2R2_vwBoard, R.id.cvC2R3_vwBoard,
                R.id.cvC2R4_vwBoard, R.id.cvC2R5_vwBoard,

                R.id.cvC3R0_vwBoard, R.id.cvC3R1_vwBoard, R.id.cvC3R2_vwBoard, R.id.cvC3R3_vwBoard,
                R.id.cvC3R4_vwBoard, R.id.cvC3R5_vwBoard, R.id.cvC3R6_vwBoard,

                R.id.cvC4R0_vwBoard, R.id.cvC4R1_vwBoard, R.id.cvC4R2_vwBoard, R.id.cvC4R3_vwBoard,
                R.id.cvC4R4_vwBoard, R.id.cvC4R5_vwBoard,

                R.id.cvC5R0_vwBoard, R.id.cvC5R1_vwBoard, R.id.cvC5R2_vwBoard, R.id.cvC5R3_vwBoard,
                R.id.cvC5R4_vwBoard,

                R.id.cvC6R0_vwBoard, R.id.cvC6R1_vwBoard, R.id.cvC6R2_vwBoard, R.id.cvC6R3_vwBoard
        };
    }

    private void initCellViews() {
        for (int cellViewId : getCellViewIds()) {
            cellViews.add(findViewById(cellViewId));
        }
    }

    private void initCharacterViews() {
        for (int i = 0; i < getCharactersPoolSize(); i++) {
            CharacterView characterView = new CharacterView(getContext(), null);
            characterView.setVisibility(GONE);
            characterViews.add(characterView);
            addView(characterView);
        }
    }

    private void initCharacterShadowViews() {
        for (int i = 0; i < getCharactersPoolSize(); i++) {
            ImageView characterShadowView = new ImageView(getContext(), null);
            characterShadowView.setImageResource(R.drawable.character_piece_shadow);
            characterShadowView.setVisibility(GONE);
            characterShadowViews.add(characterShadowView);
            addView(characterShadowView);
        }
    }

    @Override
    protected void onSizeChanged(int weight, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(weight, height, oldWidth, oldHeight);

        final int characterSize = Math.round(weight * CHARACTER_SIZE_RATIO);

        updatedViewsSize(characterViews, characterSize);
        updatedViewsSize(characterShadowViews, characterSize);
    }

    private void updatedViewsSize(@NonNull List<? extends View> views, int size) {
        for (View view : views) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.height = size;
            layoutParams.width = size;
            view.setLayoutParams(layoutParams);
        }
    }

    private int getCharactersPoolSize() {
        if (cellViews.isEmpty()) {
            throw new IllegalStateException("Cannot get character pool size before cells initialization");
        }
        return cellViews.size() - 1;
    }

    public void setOrientation(@NonNull BoardOrientation orientation) {
        cellViewsMap.clear();

        // Depending on the board orientation we add the tile in index order or in reverse
        int cellViewIdx = 0;
        int cellViewIdxStep = 1;
        if (orientation == BoardOrientation.Rotated) {
            cellViewIdx = cellViews.size() - 1;
            cellViewIdxStep = -1;
        }

        for (int x = 0; x < Board.COLUMN_COUNT; x++) {
            for (int y = 0; y < Board.getRowCount(x); y++) {
                Position position = new Position(x, y);
                CellView cellView = cellViews.get(cellViewIdx);
                cellViewsMap.put(position, cellView);

                cellViewIdx += cellViewIdxStep;
            }
        }
    }
}
