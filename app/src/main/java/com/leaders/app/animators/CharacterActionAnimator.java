package com.leaders.app.animators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.views.board.BoardView;
import com.leaders.app.views.board.CellView;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class CharacterActionAnimator extends ActionAnimator<CharacterAction> {
    private static final int DURATION_ADD = 200;
    private static final int DURATION_REMOVE = 200;
    private static final int DURATION_MOVE = 400;
    private static final int DURATION_TELEPORT = 800;
    private static final int DURATION_PUSH = 400;
    private static final int DURATION_JUMP = 400;
    private static final int DURATION_FLY = 600;
    private static final int DURATION_TRANSFORM = 800;

    public CharacterActionAnimator(@NonNull AnimationSpeed speed) {
        super(speed);
    }

    @Override
    public void animate(@NonNull BoardView boardView, 
                        @NonNull CharacterAction action,
                        @Nullable Runnable onAnimationEnd) {
        animate(boardView, action.getMotions(), onAnimationEnd);
    }

    public void animate(@NonNull BoardView boardView,
                        @NonNull List<CharacterActionMotion> motions,
                        @Nullable Runnable onAnimationEnd) {
        if (motions.isEmpty()) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
            return;
        }

        animateMotionSequence(boardView, motions, 0, onAnimationEnd);
    }

    public void animate(@NonNull BoardView boardView,
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
            case Fly: animateFlyCharacter(boardView, targets, onAnimationEnd); break;
            case Remove: animateRemoveCharacter(boardView, targets, onAnimationEnd); break;
            case Transform: animateTransformCharacter(boardView, targets, onAnimationEnd); break;
            default: throw new IllegalArgumentException("Character motion animation not handled: " + motion.getMotionType());
        }
    }

    private void animateMotionSequence(@NonNull BoardView boardView,
                                       @NonNull List<CharacterActionMotion> motions,
                                       int index, @Nullable Runnable onAnimationEnd) {
        if (index >= motions.size()) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            } return;
        }

        CharacterActionMotion motion = motions.get(index);

        animate(boardView, motion, () ->
                animateMotionSequence(boardView, motions, index + 1, onAnimationEnd)
        );
    }

    private void animateAddCharacter(@NonNull BoardView boardView,
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

    private void animateMoveCharacter(@NonNull BoardView boardView,
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

    private void animateTeleportCharacter(@NonNull BoardView boardView,
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

    private void animatePushCharacter(@NonNull BoardView boardView,
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

    private void animateSwapCharacter(@NonNull BoardView boardView,
                                             @NonNull List<CharacterActionTarget> targets,
                                             @Nullable Runnable onAnimationEnd) {
        animateMoveCharacter(boardView, targets, onAnimationEnd);
    }

    private void animateJumpCharacter(@NonNull BoardView boardView,
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
            Position originPos = Objects.requireNonNull(target.getOriginPos(),
                    "Invalid jump target : missing origin position");
            Position destPos = Objects.requireNonNull(target.getDestPos(),
                    "Invalid jump target : missing destination position");

            originPositions.add(originPos);
            destinationPositions.add(destPos);

            CharacterDisplay characterDisplay = boardView.getCharacterDisplay(originPos);
            CellView destCellView = boardView.getCellView(destPos);

            float destX = destCellView.getX();
            float destY = destCellView.getY();

            setupForMovement(characterDisplay, destX, destY);

            CharacterView characterView = characterDisplay.getCharacterView();

            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(characterView, View.X, destX);
            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(characterView, View.Y, destY);

            Keyframe scaleStart = Keyframe.ofFloat(0f, 1f);
            Keyframe scalePeak = Keyframe.ofFloat(0.45f, 1.15f);
            Keyframe scaleEnd = Keyframe.ofFloat(1f, 1f);

            PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X, scaleStart, scalePeak, scaleEnd);

            PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y, scaleStart, scalePeak, scaleEnd);

            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(characterView, scaleX, scaleY);


            int duration = getAnimationDuration(DURATION_JUMP);

            xAnimator.setDuration(duration);
            yAnimator.setDuration(duration);

            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            xAnimator.setInterpolator(interpolator);
            yAnimator.setInterpolator(interpolator);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(xAnimator, yAnimator, scaleAnimator);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Realign the whole display at the destination position
                    characterDisplay.setPosition(destX, destY);

                    onTargetAnimationEnd.run();
                }
            });

            animatorSet.start();
        }
    }

    private void animateRemoveCharacter(@NonNull BoardView boardView,
                                               @NonNull List<CharacterActionTarget> targets,
                                               @Nullable Runnable onAnimationEnd) {
        AtomicInteger remaining = new AtomicInteger(targets.size());
        Runnable onTargetAnimationEnd = () -> {
            if (remaining.decrementAndGet() == 0) {
                for (CharacterActionTarget target : targets) {
                    Position originPos = Objects.requireNonNull(target.getOriginPos(),
                            "Invalid remove target : missing origin position");
                    boardView.releaseCharacterDisplay(originPos);
                }
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        };

        for (CharacterActionTarget target : targets) {
            Position originPos = Objects.requireNonNull(target.getOriginPos(),
                    "Invalid remove target : missing origin position");
            CharacterDisplay characterDisplay = boardView.getCharacterDisplay(originPos);
            animateFadeOut(characterDisplay, DURATION_REMOVE, onTargetAnimationEnd);
        }
    }

    private void animateTransformCharacter(@NonNull BoardView boardView,
                                                  @NonNull List<CharacterActionTarget> targets,
                                                  @Nullable Runnable onAnimationEnd) {
        if (targets.size() != 2) {
            throw new IllegalArgumentException("Invalid transform motion : expected exactly 2 targets");
        }

        CharacterActionTarget sourceTarget = targets.get(0);
        CharacterActionTarget destinationTarget = targets.get(1);

        Position sourcePos = Objects.requireNonNull(sourceTarget.getOriginPos(),
                "Invalid transform target : missing source origin position");
        Position destPos = Objects.requireNonNull(destinationTarget.getDestPos(),
                "Invalid transform target : missing destination position");

        CharacterDisplay sourceCharacter = boardView.getCharacterDisplay(sourcePos);
        CharacterView characterView = sourceCharacter.getCharacterView();

        CellView destCell = boardView.getCellView(destPos);

        // The source CharacterDisplay remains at its position throughout the entire animation.
        setupForMovement(sourceCharacter, destCell.getX(), destCell.getY());

        // Temporarily use the same CharacterDisplay for the transformation.
        // The new character will be injected in the middle of the animation.

        int duration = getAnimationDuration(DURATION_TRANSFORM);

        int transformOutDuration = duration / 2;
        int transformInDuration = duration - transformOutDuration;

        // TRANSFORM OUT ANIMATION
        ObjectAnimator outScaleX = ObjectAnimator.ofFloat(characterView, View.SCALE_X, 1f, 1.15f, 1.30f, 0.75f);
        ObjectAnimator outScaleY = ObjectAnimator.ofFloat(characterView, View.SCALE_Y, 1f, 1.15f, 1.30f, 0.75f);
        ObjectAnimator outRotation = ObjectAnimator.ofFloat(characterView, View.ROTATION, 0f, -8f, 12f, 0f);
        ObjectAnimator outAlpha = ObjectAnimator.ofFloat(characterView, View.ALPHA, 1f, 1f, 0.6f, 0f);

        outScaleX.setDuration(transformOutDuration);
        outScaleY.setDuration(transformOutDuration);
        outRotation.setDuration(transformOutDuration);
        outAlpha.setDuration(transformOutDuration);

        AccelerateDecelerateInterpolator outInterpolator = new AccelerateDecelerateInterpolator();

        outScaleX.setInterpolator(outInterpolator);
        outScaleY.setInterpolator(outInterpolator);
        outRotation.setInterpolator(outInterpolator);
        outAlpha.setInterpolator(outInterpolator);

        AnimatorSet transformOut = new AnimatorSet();
        transformOut.playTogether(outScaleX, outScaleY, outRotation, outAlpha);

        // The character change must happen between the two animation phases.
        transformOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset to a clean state for the appearance phase.
                characterView.setCharacter(destinationTarget.getCharacter());

                characterView.setScaleX(0.75f);
                characterView.setScaleY(0.75f);
                characterView.setRotation(0f);
                characterView.setAlpha(0f);
            }
        });

        // TRANSFORM IN ANIMATION
        ObjectAnimator inScaleX = ObjectAnimator.ofFloat(characterView, View.SCALE_X, 0.75f, 1.15f, 0.95f, 1f);
        ObjectAnimator inScaleY = ObjectAnimator.ofFloat(characterView, View.SCALE_Y, 0.75f, 1.15f, 0.95f, 1f);
        ObjectAnimator inRotation = ObjectAnimator.ofFloat(characterView, View.ROTATION, 0f, -8f, 4f, 0f);
        ObjectAnimator inAlpha = ObjectAnimator.ofFloat(characterView, View.ALPHA, 0f, 1f);

        inScaleX.setDuration(transformInDuration);
        inScaleY.setDuration(transformInDuration);
        inRotation.setDuration(transformInDuration);
        inAlpha.setDuration(transformInDuration);

        DecelerateInterpolator inInterpolator = new DecelerateInterpolator(1.5f);

        inScaleX.setInterpolator(inInterpolator);
        inScaleY.setInterpolator(inInterpolator);
        inRotation.setInterpolator(inInterpolator);
        inAlpha.setInterpolator(inInterpolator);

        AnimatorSet transformIn = new AnimatorSet();
        transformIn.playTogether(inScaleX, inScaleY, inRotation, inAlpha);

        AnimatorSet transformSequence = new AnimatorSet();
        transformSequence.playSequentially(transformOut, transformIn);

        transformSequence.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // The transformed character is now fully displayed.
                characterView.setScaleX(1f);
                characterView.setScaleY(1f);
                characterView.setRotation(0f);
                characterView.setAlpha(1f);

                // The CharacterDisplay remains associated with the position,
                // but its CharacterView now represents the transformed character.
                List<Position> originPositions = List.of(sourcePos);

                List<Position> destinationPositions = List.of(destPos);

                boardView.moveCharacterDisplays(originPositions, destinationPositions);

                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        transformSequence.start();
    }

    private void animateFlyCharacter(@NonNull BoardView boardView,
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
            Position originPos = Objects.requireNonNull(target.getOriginPos(), "Invalid fly target : missing origin position");
            Position destPos = Objects.requireNonNull(target.getDestPos(), "Invalid fly target : missing destination position");

            originPositions.add(originPos);
            destinationPositions.add(destPos);

            CharacterDisplay characterDisplay = boardView.getCharacterDisplay(originPos);

            CellView originCell = boardView.getCellView(originPos);
            CellView destCell = boardView.getCellView(destPos);

            float originX = originCell.getX();
            float originY = originCell.getY();

            float destX = destCell.getX();
            float destY = destCell.getY();

            setupForMovement(characterDisplay, destX, destY);

            CharacterView characterView = characterDisplay.getCharacterView();

            // Linear movement for X axis
            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(characterView, View.X, originX, destX);

            float arcHeight = 50f;

            TypeEvaluator<Float> arcEvaluator = (fraction, startValue, endValue) -> {
                float linearY = startValue + (endValue - startValue) * fraction;
                float arc = 4f * fraction * (1f - fraction);
                return linearY - arcHeight * arc;
            };

            ObjectAnimator yAnimator = ObjectAnimator.ofObject(characterView, View.Y, arcEvaluator, originY, destY);

            // SCALING ANIMATION
            Keyframe scaleStart = Keyframe.ofFloat(0f, 1f);
            Keyframe scalePeak = Keyframe.ofFloat(0.45f, 1.15f);
            Keyframe scaleEnd = Keyframe.ofFloat(1f, 1f);

            PropertyValuesHolder scaleXValues = PropertyValuesHolder.ofKeyframe(View.SCALE_X, scaleStart, scalePeak, scaleEnd);
            PropertyValuesHolder scaleYValues = PropertyValuesHolder.ofKeyframe(View.SCALE_Y, scaleStart, scalePeak, scaleEnd);

            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(characterView, scaleXValues, scaleYValues);

            // ROTATION ANIMATION
            float rotationDirection = destX >= originX ? 1f : -1f;

            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(characterView, View.ROTATION, 0f, 5f * rotationDirection, 0f);

            int duration = getAnimationDuration(DURATION_FLY);

            xAnimator.setDuration(duration);
            yAnimator.setDuration(duration);
            scaleAnimator.setDuration(duration);
            rotationAnimator.setDuration(duration);

            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

            xAnimator.setInterpolator(interpolator);
            yAnimator.setInterpolator(interpolator);
            scaleAnimator.setInterpolator(interpolator);
            rotationAnimator.setInterpolator(interpolator);

            AnimatorSet animatorSet = new AnimatorSet();

            animatorSet.playTogether(xAnimator, yAnimator, scaleAnimator, rotationAnimator);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    characterDisplay.setPosition(destX, destY);

                    characterView.setRotation(0f);
                    characterView.setScaleX(1f);
                    characterView.setScaleY(1f);

                    onTargetAnimationEnd.run();
                }
            });

            animatorSet.start();
        }
    }

    private void animateMove(@NonNull CharacterDisplay characterDisplay,
                                    @NonNull CellView destCellView,
                                    @Nullable Runnable onAnimationEnd) {
        float x = destCellView.getX();
        float y = destCellView.getY();

        setupForMovement(characterDisplay, x, y);

        characterDisplay.getCharacterView().animate().x(x).y(y)
                .setDuration(getAnimationDuration(DURATION_MOVE))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // Realign the whole display at the destination position
                    characterDisplay.setPosition(x, y);

                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                })
                .start();
    }

    private void setupForMovement(@NonNull CharacterDisplay characterDisplay,
                                         float destX, float destY) {
        characterDisplay.stopHighlightAnimation();
        characterDisplay.setIsHighlighted(false, true);
        characterDisplay.bringToFront();

        // Since we're animating the movement of the characterView alone,
        // we place all other views directly at the destination
        for (CharacterDisplay.ViewType viewType : CharacterDisplay.ViewType.values()) {
            if (viewType != CharacterDisplay.ViewType.Character) {
                characterDisplay.setPosition(viewType, destX, destY);
            }
        }
    }
}
