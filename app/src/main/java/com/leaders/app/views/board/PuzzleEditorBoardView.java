package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;

import java.util.Map;
import java.util.Objects;

public final class PuzzleEditorBoardView extends BoardView {
    public PuzzleEditorBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        imvBoard.setImageResource(R.drawable.board_editor);
        // imvBoard.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.cell_positions));
    }

    public void setOnCellClickListener(OnClickListener onCellClickListener) {
        super.setOnCellClickListener(onCellClickListener);
    }

    public void setOnCharacterClickListener(OnClickListener onCharacterClickListener) {
        super.setOnCharacterClickListener(onCharacterClickListener);
    }

    public void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        super.setOnCharacterLongClickListener(onCharacterLongClickListener);
    }

    public void selectCharacterAt(@NonNull Position position) {
        CharacterDisplay characterDisplay = Objects.requireNonNull(characterDisplayMap.get(position),
                "Character to unselected not found at position:" + position);
        characterDisplay.setHighlighted(true, true);
        characterDisplay.startHighlightAnimation();
    }

    public void clearCharacterSelection() {
        for (CharacterDisplay characterDisplay : characterDisplayMap.values()) {
            characterDisplay.setHighlighted(false, true);
            characterDisplay.stopHighlightAnimation();
        }
    }

    public void applyCharacterTargets(@NonNull Board board) {
        for (Map.Entry<Position, CharacterDisplay> entry : characterDisplayMap.entrySet()) {
            CharacterView characterView = entry.getValue().getCharacterView();
            Position characterPos = entry.getKey();
            Character character = Objects.requireNonNull(board.getCell(characterPos).getCharacter(),
                "No character found matching character view at position:" + characterPos);

            characterView.setAsTarget(new InteractionTarget(
                    TargetCategory.PlayableCharacter,
                    new PlayableCharacter(character, characterPos, false, false)
            ));
        }
    }

    public void applyCellTargets() {
        for (Map.Entry<Position, CellView> entry : cellViewsMap.entrySet()) {
            entry.getValue().setAsTarget(new InteractionTarget(
                    TargetCategory.MovementDestination,
                    entry.getKey()
            ));
        }
    }
}
