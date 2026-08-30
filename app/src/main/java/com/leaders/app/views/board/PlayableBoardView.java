package com.leaders.app.views.board;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.views.animators.CharacterActionAnimator;
import com.leaders.app.views.animators.RecruitmentActionAnimator;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterHighlightView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayableBoardView extends BoardView {
    public interface OnTargetClickListener {
        void onTargetClick(@NonNull InteractionTarget target);
        /**
         * Called when the user clicks on a part of the board that is not associated
         * with any interaction target.
         *
         * <p>This can be used to handle clicks that should cancel or exit the current
         * interaction, or simply clicks on an empty area of the board.</p>
         */
        void onEmptyClick();
    }

    private OnTargetClickListener onTargetClickListener;

    public PlayableBoardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOnCellClickListener(this::onCellClick);
        setOnCharacterDisplayClickListener(this::onCharacterDisplayClick);
    }


    //region TARGET APPLICATION METHODS

    public void applyTargets(@NonNull List<InteractionTarget> targets,
                             @NonNull InteractionContext context,
                             @NonNull Board board) {
        for (InteractionTarget target : targets) {
            switch (target.getCategory()) {
                case PlayableCharacter:
                    applyPlayableCharacterTarget(target);
                    break;
                case RecruitmentDestination:
                case MovementDestination:
                case ActiveAbilityDestination:
                    applyDestinationTarget(target, context, board);
                    break;
                case ActiveAbilityTargetPosition:
                    applyActiveAbilityTarget(target);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "target category \"" + target.getCategory() + "\" not handled by the playable board"
                    );
            }
        }
    }

    private void applyPlayableCharacterTarget(@NonNull InteractionTarget target) {
        PlayableCharacter playableCharacter = Objects.requireNonNull(target.getChosenPlayableCharacter(),
                "Invalid playable character target : playable character missing");

        CharacterDisplay characterDisplay = getCharacterDisplay(playableCharacter.getPosition());
        characterDisplay.getCharacterView().setAsPlayableTarget(target);
    }

    private void applyDestinationTarget(@NonNull InteractionTarget target,
                                        @NonNull InteractionContext context,
                                        @NonNull Board board) {
        UUID characterID = Objects.requireNonNull(context.getCharacter(),
                "Invalid destination context : source character missing").getId();

        Position sourcePos = BoardQuery.getCellByCharacterId(board, characterID).getPosition();
        Position destPos = Objects.requireNonNull(target.getChosenPosition(),
                "Invalid destination target : destination position missing");

        CellView sourceCellView = getCellView(sourcePos);
        CellView destCellView = getCellView(destPos);

        switch (target.getCategory()) {
            case RecruitmentDestination:
                destCellView.setAsRecruitmentDestinationTarget(target, orientation);
                break;
            case MovementDestination:
                destCellView.setAsMovementDestinationTarget(target, sourceCellView);
                break;
            case ActiveAbilityDestination:
                destCellView.setAsActiveAbilityDestinationTarget(target);
                break;
            default:
                throw new IllegalArgumentException("Invalid destination target category: " + target.getCategory());
        }
    }

    private void applyActiveAbilityTarget(@NonNull InteractionTarget target) {
        Position targetPos = Objects.requireNonNull(target.getChosenPosition(),
                "Invalid active ability target : playable character missing");

        getCharacterDisplay(targetPos).getCharacterView().setAsActiveAbilityTarget(target);
    }

    //endregion

    //region ANIMATION METHODS

    public void highlightPlayableCharacters(@NonNull List<PlayableCharacter> playableCharacters,
                                            @Nullable Character selectedCharacter,
                                            @NonNull Board board) {
        Position selectedCharacterPos = null;
        if (selectedCharacter != null) {
            Cell selectedCharacterCell = BoardQuery.getCellByCharacterId(board, selectedCharacter.getId());
            selectedCharacterPos = selectedCharacterCell.getPosition();
        }

        for (Map.Entry<Position, CharacterDisplay> entry : characterDisplayMap.entrySet()) {
            Position position = entry.getKey();
            CharacterDisplay display = entry.getValue();

            boolean isSelectedDisplay = position.equals(selectedCharacterPos);
            boolean isPlayableCharacter = positionContainsPlayableCharacter(playableCharacters, position);

            display.setHighlighted(isSelectedDisplay || isPlayableCharacter, true);
            if (isSelectedDisplay) {
                display.startHighlightAnimation();
            } else {
                display.stopHighlightAnimation();
            }
        }
    }

    public void animateFeedback(@NonNull InteractionFeedback feedback,
                                @Nullable Runnable onAnimationEnd) {
        switch (feedback.getFeedbackType()) {
            case CharacterAction:
                CharacterActionAnimator.animate(this, feedback.getCharacterActionMotions(), onAnimationEnd);
                break;
            case RecruitmentAction:
                RecruitmentActionAnimator.animate(this, feedback.getRecruitmentActionMotions(), onAnimationEnd);
                break;
            default:
                throw new IllegalArgumentException(
                        "Feedback type \"" + feedback.getFeedbackType() + "\" not handled by the playable board"
                );
        }
    }

    private boolean positionContainsPlayableCharacter(@NonNull List<PlayableCharacter> playableCharacters,
                                                      @NonNull Position position) {
        for (PlayableCharacter playableCharacter : playableCharacters) {
            if (playableCharacter.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region CELL AND CHARACTER CLICK LISTENERS

    private void onCellClick(View v) {
        CellView cellView = (CellView) v;

        if (onTargetClickListener == null) {
            return;
        }

        if (cellView.getTarget() != null) {
            onTargetClickListener.onTargetClick(cellView.getTarget());
        } else {
            onTargetClickListener.onEmptyClick();
        }
    }

    private void onCharacterDisplayClick(@NonNull CharacterDisplay characterDisplay) {
        CharacterView characterView = characterDisplay.getCharacterView();

        if (onTargetClickListener == null) {
            return;
        }

        if (characterView.getTarget() != null) {
            // When clicking on a highlighted character with a valid target, we animate its highlight
            CharacterHighlightView highlightView = characterDisplay.getHighlightView();
            if (highlightView.getVisibility() == VISIBLE) {
                highlightView.startAnimation();
            }

            onTargetClickListener.onTargetClick(characterView.getTarget());
        } else {
            onTargetClickListener.onEmptyClick();
        }
    }

    //endregion

    //region LISTENER SETTERS

    public void setOnCharacterLongClickListener(OnLongClickListener onCharacterLongClickListener) {
        super.setOnCharacterLongClickListener(onCharacterLongClickListener);
    }

    public void setOnTargetClickListener(OnTargetClickListener onTargetClickListener) {
        this.onTargetClickListener = onTargetClickListener;
    }

    //endregion
}
