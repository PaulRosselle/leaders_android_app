package com.leaders.app.views.character;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.views.board.BoardView;
import com.leaders.app.views.board.CellView;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CharacterActionAnimator {
    private static final int DURATION_ADD = 200;
    private static final int DURATION_REMOVE = DURATION_ADD;
    private static final int DURATION_MOVE = 400;
    private static final int DURATION_TELEPORT = 800;

    public static void animate(@NonNull BoardView boardView,
                               @NonNull CharacterActionMotion motion,
                               @Nullable Runnable onAnimationEnd) {
        List<CharacterActionTarget> targets = motion.getTargets();
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Invalid add motion : missing target");
        }
        
        switch (motion.getMotionType()) {
            case Add: animateAddCharacter(boardView, targets, onAnimationEnd); break;
            case Move: animateMoveCharacter(boardView, targets, onAnimationEnd); break;
            case Teleport: animateTeleportCharacter(boardView, targets, onAnimationEnd); break;
            case Push: animatePushCharacter(boardView, targets, onAnimationEnd); break;
            case Swap: animateSwapCharacter(boardView, targets, onAnimationEnd); break;
            case Jump: animateJumpCharacter(boardView, targets, onAnimationEnd); break;
            case Remove: animateRemoveCharacter(boardView, targets, onAnimationEnd); break;
            case Transform: animateTransformCharacter(boardView, targets, onAnimationEnd); break;
            default: throw new IllegalArgumentException("Character motion animation not handled: " + motion.getMotionType());
        }
    }

    private static void animateAddCharacter(@NonNull BoardView boardView,
                                            @NonNull List<CharacterActionTarget> targets,
                                            @Nullable Runnable onAnimationEnd) {
        Runnable onTargetAnimationEnd = onAnimationEnd;
        if (onAnimationEnd != null && targets.size() > 1) {
            AtomicInteger remaining = new AtomicInteger(targets.size());
            onTargetAnimationEnd = () -> {
                if (remaining.decrementAndGet() == 0) {
                    onAnimationEnd.run();
                }
            };
        }

        for (CharacterActionTarget target : targets) {
            Position destPos = Objects.requireNonNull(target.getDestPos(), "Invalid add target : missing destination position");

            CharacterDisplay characterDisplay = boardView.acquireCharacterDisplay(destPos);
            CellView destCellView = boardView.getCellView(destPos);
            characterDisplay.setPosition(destCellView.getX(), destCellView.getY());
            CharacterView characterView = characterDisplay.getCharacterView();
            characterView.setCharacter(target.getCharacter());
            characterView.setScaleX(0f);
            characterView.setScaleY(0f);
            characterView.setAlpha(0f);
            characterView.setVisibility(View.VISIBLE);

            animateFadeIn(characterDisplay, DURATION_ADD, onTargetAnimationEnd);
        }
    }

    private static void animateMoveCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        List<Position> originPositions = new ArrayList<>();
        List<Position> destinationPositions = new ArrayList<>();

        AtomicInteger remaining = new AtomicInteger(targets.size());
        Runnable onTargetAnimationEnd = () -> {
            if (remaining.decrementAndGet() == 0) {
                boardView.moveCharacterDisplays(originPositions, destinationPositions);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        };

        for (CharacterActionTarget target : targets) {
            Position originPos = Objects.requireNonNull(target.getOriginPos(), "Invalid teleport target : missing origin position");
            Position destPos = Objects.requireNonNull(target.getDestPos(), "Invalid teleport target : missing destination position");

            originPositions.add(originPos);
            destinationPositions.add(destPos);

            animateMove(boardView.getCharacterDisplay(originPos), boardView.getCellView(destPos), onTargetAnimationEnd);
        }
    }

    private static void animateTeleportCharacter(@NonNull BoardView boardView,
                                                 @NonNull List<CharacterActionTarget> targets,
                                                 @Nullable Runnable onAnimationEnd) {
        int respawnDelay = DURATION_TELEPORT / 6;
        int fadeInDuration = DURATION_TELEPORT / 2 - respawnDelay;
        int fadeOutDuration = fadeInDuration;

        Runnable onFadeInAnimationEnd;
        if (onAnimationEnd != null && targets.size() > 1) {
            AtomicInteger fadeInRemaining = new AtomicInteger(targets.size());
            onFadeInAnimationEnd = () -> {
                if (fadeInRemaining.decrementAndGet() == 0) {
                    onAnimationEnd.run();
                }
            };
        } else {
            onFadeInAnimationEnd = onAnimationEnd;
        }

        AtomicInteger fadeOutRemaining = new AtomicInteger(targets.size());
        Runnable onFadeOutAnimationEnd = () -> {
            if (fadeOutRemaining.decrementAndGet() == 0) {
                // 2. Move the character display position
                List<Position> originPositions = new ArrayList<>();
                List<Position> destinationPositions = new ArrayList<>();
                for (CharacterActionTarget target : targets) {
                    Position originPos = Objects.requireNonNull(target.getOriginPos(), "Invalid teleport target : missing origin position");
                    Position destPos = Objects.requireNonNull(target.getDestPos(), "Invalid teleport target : missing destination position");

                    originPositions.add(originPos);
                    destinationPositions.add(destPos);

                    CellView destCellView = boardView.getCellView(destPos);
                    boardView.getCharacterDisplay(originPos).setPosition(destCellView.getX(), destCellView.getY());
                }
                // Updates the Position -> CharacterDisplay mapping
                boardView.moveCharacterDisplays(originPositions, destinationPositions);

                // 3. Fade in the character displays at their destinations
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    for (Position destPos : destinationPositions) {
                        animateFadeIn(boardView.getCharacterDisplay(destPos), fadeInDuration, onFadeInAnimationEnd);
                    }
                }, respawnDelay);
            }
        };

        // 1. Fade out the character displays at their origin positions
        for (CharacterActionTarget target : targets) {
            Position fadeOutPos = Objects.requireNonNull(target.getOriginPos(), "Invalid teleport target : missing origin position");
            animateFadeOut(boardView.getCharacterDisplay(fadeOutPos), fadeOutDuration, onFadeOutAnimationEnd);
        }
    }

    private static void animatePushCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        // TODO
    }

    private static void animateSwapCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        animateMoveCharacter(boardView, targets, onAnimationEnd);
    }

    private static void animateJumpCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        // TODO
    }

    private static void animateRemoveCharacter(@NonNull BoardView boardView,
                                               @NonNull List<CharacterActionTarget> targets,
                                               @Nullable Runnable onAnimationEnd) {
        AtomicInteger remaining = new AtomicInteger(targets.size());
        Runnable onTargetAnimationEnd = () -> {
            if (remaining.decrementAndGet() == 0) {
                for (CharacterActionTarget target : targets) {
                    Position originPos = Objects.requireNonNull(target.getOriginPos(), "Invalid remove target : missing origin position");
                    boardView.releaseCharacterDisplay(originPos);
                }
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        };

        for (CharacterActionTarget target : targets) {
            Position originPos = Objects.requireNonNull(target.getOriginPos(), "Invalid remove target : missing origin position");
            CharacterDisplay characterDisplay = boardView.getCharacterDisplay(originPos);
            animateFadeOut(characterDisplay, DURATION_REMOVE, onTargetAnimationEnd);
        }
    }

    private static void animateTransformCharacter(@NonNull BoardView boardView,
                                                  @NonNull List<CharacterActionTarget> targets,
                                                  @Nullable Runnable onAnimationEnd) {
        // TODO
    }

    private static void animateFadeIn(@NonNull CharacterDisplay characterDisplay, int duration,
                                      @Nullable Runnable onAnimationEnd) {
        characterDisplay.getCharacterView().animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }

    private static void animateFadeOut(@NonNull CharacterDisplay characterDisplay, int duration,
                                       @Nullable Runnable onAnimationEnd) {
        CharacterView characterView = characterDisplay.getCharacterView();
        characterDisplay.stopHighlightAnimation();
        characterDisplay.getHighlightView().setVisibility(View.GONE);
        characterDisplay.getShadowView().setVisibility(View.GONE);

        characterView.animate().scaleX(0f).scaleY(0f).alpha(0f)
                .setDuration(getAnimationDuration(duration))
                .withEndAction(onAnimationEnd)
                .start();
    }

    private static void animateMove(@NonNull CharacterDisplay characterDisplay,
                                    @NonNull CellView destCellView,
                                    @Nullable Runnable onAnimationEnd) {
        float x = destCellView.getX();
        float y = destCellView.getY();


        CharacterView characterView = characterDisplay.getCharacterView();
        characterView.scaleForHighlight(false, true);
        characterView.bringToFront();

        characterDisplay.stopHighlightAnimation();
        characterDisplay.getHighlightView().setVisibility(View.GONE);
        characterDisplay.setPosition(CharacterDisplay.ViewType.Highlight, x, y);
        characterDisplay.getShadowView().setVisibility(View.GONE);
        characterDisplay.setPosition(CharacterDisplay.ViewType.Shadow, x, y);

        characterView.animate().x(x).y(y)
                .setDuration(getAnimationDuration(DURATION_MOVE))
                .withEndAction(onAnimationEnd)
                .start();
    }

    private static int getAnimationDuration(int duration) {
        // Will be helpfull to add a global animation speed ratio
        return duration;
    }
}
