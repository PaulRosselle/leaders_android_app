package com.leaders.app.views.board;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.BoardOrientation;
import com.leaders.app.views.animators.CharacterActionAnimator;
import com.leaders.app.views.animators.RecruitmentActionAnimator;
import com.leaders.app.views.character.CharacterDisplay;
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
import java.util.stream.Collectors;

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
    private static final int CHARACTER_SHINE_CYCLE_PAUSE = 1600;
    private static final int CHARACTER_SHINE_ANIMATION_INTERVAL = 1200;
    @Nullable
    private ValueAnimator shineAnimator;

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

    private List<CharacterDisplay> getShineDisplays() {
        final int compareFactor = orientation == BoardOrientation.Rotated ? -1 : 1;

        return characterDisplayMap.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    Position pos1 = entry1.getKey();
                    Position pos2 = entry2.getKey();

                    int compareX = Integer.compare(pos1.getX(), pos2.getX());
                    if (compareX != 0) {
                        return compareX * compareFactor;
                    }

                    return Integer.compare(pos1.getY(), pos2.getY()) * compareFactor;
                })
                .filter(entry -> entry.getValue().isHighlighted())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void startPlayableCharactersShineAnimation() {
        stopPlayableCharactersShineAnimation();

        List<CharacterDisplay> displays = getShineDisplays();

        if (displays.isEmpty()) {
            return;
        }

        final int animationDuration = CHARACTER_SHINE_ANIMATION_INTERVAL * displays.size();
        final int cycleDuration = animationDuration + CHARACTER_SHINE_CYCLE_PAUSE;
        final int pauseDuration = CHARACTER_SHINE_CYCLE_PAUSE / 2;
        final int[] lastIndex = {-1};

        shineAnimator = ValueAnimator.ofInt(0, cycleDuration);
        shineAnimator.setDuration(cycleDuration);
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.setInterpolator(new LinearInterpolator());

        shineAnimator.addUpdateListener(animation -> {
            long elapsed = animation.getCurrentPlayTime() % cycleDuration;
            // The animation pauses for a moment after a full cycle
            if (elapsed < pauseDuration || elapsed >= animationDuration + pauseDuration) {
                lastIndex[0] = -1;
                return;
            }

            int index = (int) ((elapsed - pauseDuration) / CHARACTER_SHINE_ANIMATION_INTERVAL);
            if (index != lastIndex[0]) {
                lastIndex[0] = index;
                displays.get(index).playShineAnimation();
            }
        });

        shineAnimator.start();
    }

    public void stopPlayableCharactersShineAnimation() {
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }
    }

    public void highlightPlayableCharacters(@Nullable List<PlayableCharacter> playableCharacters,
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
            boolean isPlayableCharacter = playableCharacters != null &&
                    positionContainsPlayableCharacter(playableCharacters, position);

            display.setIsHighlighted(isSelectedDisplay || isPlayableCharacter, true);
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
            if (characterDisplay.isHighlighted()) {
                characterDisplay.startHighlightAnimation();
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

    @Override
    protected void onDetachedFromWindow() {
        stopPlayableCharactersShineAnimation();
        super.onDetachedFromWindow();
    }
}
