package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterDisplayPool;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class BoardView extends ConstraintLayout {
    private static final float CHARACTER_SIZE_TO_BOARD_RATIO = 0.1410f;

    protected ImageView imvBoard;
    private final List<CellView> cellViews;
    private final CharacterDisplayPool characterDisplayPool;

    @NonNull
    protected final Map<Position, CellView> cellViewsMap;
    @NonNull
    protected final Map<Position, CharacterDisplay> characterDisplayMap;

    @NonNull
    protected BoardOrientation orientation;

    private OnClickListener onCellClickListener;
    private OnClickListener onCharacterClickListener;
    private OnLongClickListener onCharacterLongClickListener;


    public BoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        cellViews = new ArrayList<>();
        cellViewsMap = new HashMap<>();
        characterDisplayMap = new HashMap<>();

        inflate(context, R.layout.view_board, this);

        imvBoard = findViewById(R.id.imvBoard_vwBoard);
        initCellViews();
        characterDisplayPool = new CharacterDisplayPool(getContext(), this);

        setOrientation(BoardOrientation.Default);
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

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        final int characterDisplaySize = getCharacterDisplaySize(width);

        characterDisplayPool.setDisplaysSize(characterDisplaySize);
        for (Map.Entry<Position, CharacterDisplay> entry : characterDisplayMap.entrySet()) {
            CharacterDisplay display = entry.getValue();
            display.setSize(characterDisplaySize);
            CellView cellView = getCellView(entry.getKey());
            display.setPosition(cellView.getX(), cellView.getY());
        }
    }

    public int getCharacterDisplaySize() {
        return getCharacterDisplaySize(getWidth());
    }

    private int getCharacterDisplaySize(int boardWidth) {
        return Math.round(boardWidth * CHARACTER_SIZE_TO_BOARD_RATIO);
    }

    public final void setOrientation(@NonNull BoardOrientation orientation) {
        this.orientation = orientation;

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

        // Since Position -> CellView mapping can have changed, we must update character
        // displays so they stay on top of their corresponding cell position
        for (Map.Entry<Position, CharacterDisplay> entry : characterDisplayMap.entrySet()) {
            CellView cellView = getCellView(entry.getKey());
            entry.getValue().setPosition(cellView.getX(), cellView.getY());
        }
    }

    public final void setBoard(@NonNull Board board) {
        clearTargets();

        for (Cell cell : board.getCells().values()) {
            Position cellPosition = cell.getPosition();
            CharacterDisplay characterDisplay = characterDisplayMap.get(cellPosition);

            if (cell.getCharacter() != null) {
                if (characterDisplay == null) {
                    characterDisplay = acquireCharacterDisplay(cellPosition);
                }

                characterDisplay.reset();
                CharacterView characterView = characterDisplay.getCharacterView();
                characterView.setVisibility(VISIBLE);
                characterView.setCharacter(cell.getCharacter());

                CellView cellView = getCellView(cellPosition);
                characterDisplay.setPosition(cellView.getX(), cellView.getY());

            } else if (characterDisplay != null) {
                releaseCharacterDisplay(cellPosition);
            }
        }
    }

    @NonNull
    protected final CharacterDisplay acquireCharacterDisplay(@NonNull Position position) {
        CharacterDisplay characterDisplay = characterDisplayPool.acquire();

        characterDisplay.setOnCharacterClickListener(onCharacterClickListener);
        characterDisplay.setOnCharacterLongClickListener(onCharacterLongClickListener);

        characterDisplayMap.put(position, characterDisplay);
        return characterDisplay;
    }

    protected final void releaseCharacterDisplay(@NonNull Position position) {
        CharacterDisplay characterDisplay = Objects.requireNonNull(characterDisplayMap.remove(position),
                "CharacterDisplay to release not found");

        characterDisplay.reset();

        characterDisplay.setOnCharacterClickListener(null);
        characterDisplay.setOnCharacterLongClickListener(null);

        characterDisplayPool.release(characterDisplay);
    }

    public final void clearTargets() {
        for (Position position : cellViewsMap.keySet()) {
            getCellView(position).clearTarget();
            CharacterDisplay characterDisplay = characterDisplayMap.get(position);
            if (characterDisplay != null) {
                characterDisplay.getCharacterView().clearTarget();
            }
        }
    }

    protected final CellView getCellView(@NonNull Position position) {
        return Objects.requireNonNull(cellViewsMap.get(position), "No CellView found at Position:" + position);
    }

    protected void setOnCellClickListener(OnClickListener onCellClickListener) {
        this.onCellClickListener = onCellClickListener;

        for (CellView cellView : cellViews) {
            cellView.setOnClickListener(onCellClickListener);
        }
    }

    protected void setOnCharacterClickListener(OnClickListener onCharacterClickListener) {
        this.onCharacterClickListener = onCharacterClickListener;

        for (CharacterDisplay characterDisplay : characterDisplayMap.values()) {
            characterDisplay.setOnCharacterClickListener(onCharacterClickListener);
        }
    }

    protected void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        this.onCharacterLongClickListener = onCharacterLongClickListener;

        for (CharacterDisplay characterDisplay : characterDisplayMap.values()) {
            characterDisplay.setOnCharacterLongClickListener(onCharacterLongClickListener);
        }
    }
}
