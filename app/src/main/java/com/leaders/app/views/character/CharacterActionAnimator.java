package com.leaders.app.views.character;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

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

import kotlin.NotImplementedError;

public final class CharacterActionAnimator {
    private static final int DURATION_ADD = 200;
    private static final int DURATION_REMOVE = DURATION_ADD;
    private static final int DURATION_MOVE = 400;
    private static final int DURATION_TELEPORT = 800;
    private static final int DURATION_PUSH = 400;

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
        int fadingDuration = DURATION_TELEPORT / 2 - respawnDelay;

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
                        animateFadeIn(boardView.getCharacterDisplay(destPos), fadingDuration, onFadeInAnimationEnd);
                    }
                }, respawnDelay);
            }
        };

        // 1. Fade out the character displays at their origin positions
        for (CharacterActionTarget target : targets) {
            Position fadeOutPos = Objects.requireNonNull(target.getOriginPos(), "Invalid teleport target : missing origin position");
            animateFadeOut(boardView.getCharacterDisplay(fadeOutPos), fadingDuration, onFadeOutAnimationEnd);
        }
    }

    private static void animatePushCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        if (targets.size() != 2) {
            throw new IllegalArgumentException("Invalid push motion : expected exactly 2 targets");
        }

        CharacterActionTarget pushingTarget = targets.get(0);
        CharacterActionTarget pushedTarget = targets.get(1);

        Position pushingOriginPos = Objects.requireNonNull(pushingTarget.getOriginPos(),
                "Invalid push target : missing pushing origin position");
        Position pushingDestPos = Objects.requireNonNull(pushingTarget.getDestPos(),
                "Invalid push target : missing pushing destination position");

        Position pushedOriginPos = Objects.requireNonNull(pushedTarget.getOriginPos(),
                "Invalid pushed target : missing origin position");
        Position pushedDestPos = Objects.requireNonNull(pushedTarget.getDestPos(),
                "Invalid pushed target : missing destination position");

        CharacterDisplay pushingCharacter = boardView.getCharacterDisplay(pushingOriginPos);
        CharacterDisplay pushedCharacter = boardView.getCharacterDisplay(pushedOriginPos);

        CharacterView pushingView = pushingCharacter.getCharacterView();
        CharacterView pushedView = pushedCharacter.getCharacterView();

        CellView pushingOriginCell = boardView.getCellView(pushingOriginPos);
        CellView pushingDestCell = boardView.getCellView(pushingDestPos);

        CellView pushedDestCell = boardView.getCellView(pushedDestPos);

        float pushingOriginX = pushingOriginCell.getX();
        float pushingOriginY = pushingOriginCell.getY();

        float pushingDestX = pushingDestCell.getX();
        float pushingDestY = pushingDestCell.getY();

        float pushedDestX = pushedDestCell.getX();
        float pushedDestY = pushedDestCell.getY();

        setupForMovement(pushedCharacter, pushedDestX, pushedDestY);
        setupForMovement(pushingCharacter, pushingDestX, pushingDestY);

        int totalDuration = getAnimationDuration(DURATION_PUSH);

        // ANTICIPATION ANIMATION
        float dx = pushingDestX - pushingOriginX;
        float dy = pushingDestY - pushingOriginY;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float anticipationDistance = distance * 0.08f;

        float anticipationX = pushingOriginX;
        float anticipationY = pushingOriginY;

        if (distance > 0f) {
            anticipationX -= dx / distance * anticipationDistance;
            anticipationY -= dy / distance * anticipationDistance;
        }

        int anticipationDuration = totalDuration / 5;
        int pushDuration = totalDuration - anticipationDuration;

        ObjectAnimator anticipationXAnimator = ObjectAnimator.ofFloat(pushingView, View.X, anticipationX, pushingOriginX);
        ObjectAnimator anticipationYAnimator = ObjectAnimator.ofFloat(pushingView, View.Y, anticipationY, pushingOriginY);

        anticipationXAnimator.setDuration(anticipationDuration);
        anticipationYAnimator.setDuration(anticipationDuration);

        AnticipateInterpolator anticipateInterpolator = new AnticipateInterpolator(1.5f);

        anticipationXAnimator.setInterpolator(anticipateInterpolator);
        anticipationYAnimator.setInterpolator(anticipateInterpolator);

        AnimatorSet anticipationSet = new AnimatorSet();
        anticipationSet.playTogether(anticipationXAnimator, anticipationYAnimator);

        // PUSHING ANIMATION

        ObjectAnimator pushXAnimator = ObjectAnimator.ofFloat(pushingView, View.X, pushingOriginX, pushingDestX);
        ObjectAnimator pushYAnimator = ObjectAnimator.ofFloat(pushingView, View.Y, pushingOriginY, pushingDestY);

        pushXAnimator.setDuration(pushDuration);
        pushYAnimator.setDuration(pushDuration);

        pushXAnimator.setInterpolator(new android.view.animation.AccelerateInterpolator(1.5f));
        pushYAnimator.setInterpolator(new android.view.animation.AccelerateInterpolator(1.5f));

        AnimatorSet pushSet = new AnimatorSet();
        pushSet.playTogether(pushXAnimator, pushYAnimator);

        // PUSHED ANIMATION

        ObjectAnimator pushedXAnimator = ObjectAnimator.ofFloat(pushedView, View.X, pushedDestX);
        ObjectAnimator pushedYAnimator = ObjectAnimator.ofFloat(pushedView, View.Y, pushedDestY);

        int pushedDuration = (int) (totalDuration / 1.5f);

        pushedXAnimator.setDuration(pushedDuration);
        pushedYAnimator.setDuration(pushedDuration);

        pushedXAnimator.setInterpolator(new DecelerateInterpolator(1f));
        pushedYAnimator.setInterpolator(new DecelerateInterpolator(1f));
        AnimatorSet pushedSet = new AnimatorSet();
        pushedSet.playTogether(pushedXAnimator, pushedYAnimator);

        // ANIMATION SEQUENCE : Anticipation -> Pushing -> Pushed
        AnimatorSet pushSequence = new AnimatorSet();
        pushSequence.playSequentially(anticipationSet, pushSet, pushedSet);

        pushSequence.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Realign the whole displays at their destination positions
                pushingCharacter.setPosition(pushingDestX, pushingDestY);
                pushedCharacter.setPosition(pushedDestX, pushedDestY);

                List<Position> originPositions = List.of(pushingOriginPos, pushedOriginPos);
                List<Position> destinationPositions = List.of(pushingDestPos, pushedDestPos);

                boardView.moveCharacterDisplays(originPositions, destinationPositions);

                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        pushSequence.start();
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
        throw new NotImplementedError("Transform character animation not implemented");
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

        setupForMovement(characterDisplay, x, y);

        characterDisplay.getCharacterView().animate().x(x).y(y)
                .setDuration(getAnimationDuration(DURATION_MOVE))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Realign the whole display at the destination position
                        characterDisplay.setPosition(x, y);

                        if (onAnimationEnd != null) {
                            onAnimationEnd.run();
                        }
                    }
                })
                .start();
    }

    private static void setupForMovement(@NonNull CharacterDisplay characterDisplay,
                                         float destX, float destY) {
        CharacterView characterView = characterDisplay.getCharacterView();
        characterView.scaleForHighlight(false, true);
        characterView.bringToFront();

        characterDisplay.stopHighlightAnimation();
        characterDisplay.getHighlightView().setVisibility(View.GONE);
        characterDisplay.setPosition(CharacterDisplay.ViewType.Highlight, destX, destY);
        characterDisplay.getShadowView().setVisibility(View.GONE);
        characterDisplay.setPosition(CharacterDisplay.ViewType.Shadow, destX, destY);
    }

    private static int getAnimationDuration(int duration) {
        // Will be helpfull to add a global animation speed ratio
        return duration;
    }
}
