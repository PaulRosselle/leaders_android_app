package com.leaders.app.views.duel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.PortraitGroupView;
import com.leaders.app.views.character.PortraitView;
import com.leaders.app.views.decoration.FrameShineView;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CharacterCardSelectionView extends ConstraintLayout {
    public interface OnCardSelectedListener {
        void onRecruitmentCardSelected(@NonNull InteractionTarget target);
        void onBanishmentCardSelected();
        void onNotSelectableCardClick();
    }

    private final LinearLayout llyPortraits;
    private final ScrollView scvPortraits;
    private final FrameShineView fsvShineEffect;

    private static final int SHINE_CYCLE_PAUSE = 2000;
    private static final int SHINE_ANIMATION_INTERVAL = FrameShineView.SHINE_ANIMATION_DURATION;
    @Nullable
    private ValueAnimator shineAnimator;

    private int portraitsPerGroup;
    private int portraitSpacing;
    private List<InteractionTarget> targets;
    private InteractionTarget selectedTarget;

    private OnLongClickListener onPortraitLongClickListener;

    private OnCardSelectedListener onCardSelectedListener;

    public CharacterCardSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        targets = null;
        selectedTarget = null;

        inflate(context, R.layout.view_character_card_selection, this);

        llyPortraits = findViewById(R.id.llyPortraits_vwCharacterCardSelection);
        scvPortraits = findViewById(R.id.scvPortraits_vwCharacterCardSelection);
        fsvShineEffect = findViewById(R.id.fsvShineEffect_vwCharacterCardSelection);

        applyGameModeParams(GameMode.Discovery);
    }

    public void applyGameModeParams(@NonNull GameMode gameMode) {
        // Portrait display params changes depending on the game mode
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) scvPortraits.getLayoutParams();
        if (gameMode == GameMode.Discovery) {
            portraitsPerGroup = 3;
            portraitSpacing = 16;
            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        } else {
            portraitsPerGroup = 6;
            portraitSpacing = 2;
            params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        }
    }

    public void applyTargets(@NonNull List<InteractionTarget> targets) {
        this.targets = targets;
        selectedTarget = null;
        updatePortraitsFromTargets();
    }

    public void applyCards(@NonNull List<SelectableCharacterCard> selectableCards) {
        this.targets = null;
        selectedTarget = null;
        updatePortraitsFromCards(selectableCards);
    }

    public InteractionTarget getSelectedTarget() {
        return selectedTarget;
    }

    private void updatePortraitsFromTargets() {
        llyPortraits.removeAllViews();

        // First we order each target from their card
        List<CharacterCard> allCards = new ArrayList<>(Arrays.asList(CharacterCard.values()));
        Context context = getContext();
        CharacterCardUtils.sort(context, allCards);

        List<InteractionTarget> sortedBannedTargets = new ArrayList<>();
        List<InteractionTarget> sortedTargets = new ArrayList<>();
        for (CharacterCard card : allCards) {
            Optional<InteractionTarget> matchingTarget = targets.stream()
                    .filter(target -> getSelectableCardFromTarget(target).getCharacterCard() == card)
                    .findFirst();

            if (matchingTarget.isPresent()) {
                if (getSelectableCardFromTarget(matchingTarget.get()).getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned) {
                    sortedBannedTargets.add(matchingTarget.get());
                } else {
                    sortedTargets.add(matchingTarget.get());
                }
            }
        }
        sortedTargets.addAll(sortedBannedTargets);

        for (int start = 0; start < sortedTargets.size(); start += portraitsPerGroup) {
            int end = Math.min(start + portraitsPerGroup, sortedTargets.size());

            List<InteractionTarget> groupTargets = new ArrayList<>(end - start);

            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                InteractionTarget target = sortedTargets.get(groupIdx);

                groupTargets.add(target);
            }

            PortraitGroupView portraitsGroupView = PortraitGroupView.createFromTargets(
                    getContext(), groupTargets, portraitsPerGroup
            );
            initPortraitsGroup(portraitsGroupView);
        }

        scvPortraits.setVisibility(VISIBLE);
        updatePortraitsScrollView();
    }


    private void updatePortraitsFromCards(@NonNull List<SelectableCharacterCard> selectableCards) {
        llyPortraits.removeAllViews();

        // First we order each selectable card
        List<CharacterCard> allCards = new ArrayList<>(Arrays.asList(CharacterCard.values()));
        Context context = getContext();
        CharacterCardUtils.sort(context, allCards);

        List<SelectableCharacterCard> sortedBannedCards = new ArrayList<>();
        List<SelectableCharacterCard> sortedSelectableCards = new ArrayList<>();
        for (CharacterCard card : allCards) {
            Optional<SelectableCharacterCard> matchingSelectableCard = selectableCards.stream()
                    .filter(selectableCard -> selectableCard.getCharacterCard() == card).findFirst();
            if (matchingSelectableCard.isPresent()) {
                if (matchingSelectableCard.get().getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned) {
                    sortedBannedCards.add(matchingSelectableCard.get());
                } else {
                    sortedSelectableCards.add(matchingSelectableCard.get());
                }
            }
        }
        sortedSelectableCards.addAll(sortedBannedCards);

        for (int start = 0; start < sortedSelectableCards.size(); start += portraitsPerGroup) {
            int end = Math.min(start + portraitsPerGroup, sortedSelectableCards.size());

            List<SelectableCharacterCard> groupCards = new ArrayList<>(end - start);

            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                groupCards.add(sortedSelectableCards.get(groupIdx));
            }

            PortraitGroupView portraitsGroupView = PortraitGroupView.createFromSelectableCards(
                    getContext(), groupCards, portraitsPerGroup
            );
            initPortraitsGroup(portraitsGroupView);
        }

        scvPortraits.setVisibility(VISIBLE);
        updatePortraitsScrollView();
    }

    private void initPortraitsGroup(@NonNull PortraitGroupView portraitsGroupView) {
        portraitsGroupView.setPortraitsClickListener(this::onPortraitClick);
        portraitsGroupView.setPortraitsLongClickListener(onPortraitLongClickListener);
        portraitsGroupView.setPortraitSpacing(portraitSpacing);

        llyPortraits.addView(portraitsGroupView, getPortraitsGroupLP());
    }

    private void onPortraitClick(View v) {
        PortraitView portraitView = (PortraitView) v;

        InteractionTarget target = portraitView.getTarget();
        if (target == null) {
            return;
        }

        if (target.getCategory().getResultType() != InteractionResultType.SelectableCharacterCardChosen) {
            throw new IllegalStateException("Invalid portrait target category: " + target.getCategory());
        }

        SelectableCharacterCard selectedCard = getSelectableCardFromTarget(target);

        switch (selectedCard.getSelectionStatus()) {
            case NotSelectable: onNotSelectableCardClick();
                break;

            case Recruitable: onRecruitableCardSelected(target);
                break;

            case Banishable: onBanishableCardSelected(target);
                break;

            case RecruitmentImpossible:
            case AlreadyBanned:
                onInvalidCardSelected(selectedCard);
                break;

            default: throw new IllegalStateException("Unexpected selectable card status: " + selectedCard.getSelectionStatus());
        }
    }

    private SelectableCharacterCard getSelectableCardFromTarget(@NonNull InteractionTarget target) {
        return Objects.requireNonNull(
                target.getChosenSelectableCharacterCard(),
                "Invalid portrait target: selectable character card missing"
        );
    }

    private void onNotSelectableCardClick() {
        if (onCardSelectedListener != null) {
            onCardSelectedListener.onNotSelectableCardClick();
        }
    }

    private void onRecruitableCardSelected(@NonNull InteractionTarget target) {
        selectedTarget = target;

        if (onCardSelectedListener != null) {
            onCardSelectedListener.onRecruitmentCardSelected(target);
        }
    }
    private void onBanishableCardSelected(@NonNull InteractionTarget target) {
        selectedTarget = target;
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            PortraitGroupView groupView = (PortraitGroupView) llyPortraits.getChildAt(i);
            for (PortraitView portraitView : groupView.getPortraits()) {
                if (selectedTarget == portraitView.getTarget()) {
                    portraitView.setUseBannedDisplay(true);
                } else {
                    boolean alreadyBanned = Objects.requireNonNull(
                            Objects.requireNonNull(portraitView.getTarget(),
                                            "Invalid portrait: target missing")
                                    .getChosenSelectableCharacterCard(),
                                    "Invalid portrait target: card missing")
                            .getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned;
                    if (!alreadyBanned) {
                        portraitView.setUseBannedDisplay(false);
                    }
                }
            }
        }

        if (onCardSelectedListener != null) {
            onCardSelectedListener.onBanishmentCardSelected();
        }
    }

    private void onInvalidCardSelected(@NonNull SelectableCharacterCard selectedCard) {
        // Without a valid target, invalid cards are considered like not selectable cards
        if (targets == null) {
            if (onCardSelectedListener != null) {
                onCardSelectedListener.onNotSelectableCardClick();
            }
        }

        String messageFormat;
        if (selectedCard.getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned) {
            messageFormat = getContext().getString(R.string.card_already_banned);
        } else {
            messageFormat = getContext().getString(R.string.card_cannot_be_recruited);
        }
        String cardName = getContext().getString(CharacterCardUtils.getNameId(selectedCard.getCharacterCard()));

        Toast.makeText(getContext(), String.format(messageFormat, cardName), Toast.LENGTH_SHORT).show();
    }

    private void updatePortraitsScrollView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) scvPortraits.getLayoutParams();

        int availableHeight = scvPortraits.getMeasuredHeight();
        if (availableHeight <= 0) {
            scvPortraits.post(this::updatePortraitsScrollView);
            return;
        }

        int portraitsHeight = llyPortraits.getMeasuredHeight();

        if (portraitsHeight <= availableHeight) {
            // The whole portraits linear layout fits on screen.
            // Let the ScrollView have its natural width and center it.
            params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        } else {
            // The portraits linear layout is wider than the screen.
            // Make the ScrollView fill the available width so it can scroll.
            params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;

        }

        scvPortraits.setLayoutParams(params);
    }

    private LinearLayout.LayoutParams getPortraitsGroupLP() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        int marginValueInPx = (int) (2 * getResources().getDisplayMetrics().density);
        layoutParams.topMargin = marginValueInPx;
        layoutParams.bottomMargin = marginValueInPx;
        layoutParams.weight = 1;

        return layoutParams;
    }

    public void setOnPortraitLongClickListener(OnLongClickListener onPortraitLongClickListener) {
        this.onPortraitLongClickListener = onPortraitLongClickListener;
    }

    public void setOnCardSelectedListener(OnCardSelectedListener onCardSelectedListener) {
        this.onCardSelectedListener = onCardSelectedListener;
    }

    public void setOnScrollViewClickListener(OnClickListener onScrollViewClickListener) {
        scvPortraits.setOnClickListener(onScrollViewClickListener);
    }

    public void show(boolean animate) {
        // We use a fading animation for the visibility change
        if (animate) {
            Transition transition = new Fade();
            transition.setDuration(400);
            transition.addTarget(this);
            TransitionManager.beginDelayedTransition((ViewGroup) this.getParent(), transition);
        }
        setVisibility(VISIBLE);
    }

    public void hide() {
        // Hiding the view is always instantaneous
        setVisibility(GONE);
    }

    public void setPortraitsVisible(boolean visible) {
        scvPortraits.setVisibility(visible ? VISIBLE : GONE);
    }

    public void startShineAnimation() {
        stopShineAnimation();

        final int cycleDuration = SHINE_ANIMATION_INTERVAL + SHINE_CYCLE_PAUSE;
        final int pauseDuration = SHINE_CYCLE_PAUSE / 2;
        final boolean[] animationStarted = {false};

        shineAnimator = ValueAnimator.ofInt(0, cycleDuration);
        shineAnimator.setDuration(cycleDuration);
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.setInterpolator(new LinearInterpolator());

        shineAnimator.addUpdateListener(animation -> {
            long elapsed = animation.getCurrentPlayTime() % cycleDuration;

            // Pause before and after the shine animation
            if (elapsed < pauseDuration || elapsed >= SHINE_ANIMATION_INTERVAL + pauseDuration) {
                animationStarted[0] = false;
                return;
            }

            if (!animationStarted[0]) {
                animationStarted[0] = true;
                fsvShineEffect.playShine();
            }
        });

        shineAnimator.start();
    }


    public void stopShineAnimation() {
        if (shineAnimator != null) {
            fsvShineEffect.stopShine();
            shineAnimator.cancel();
            shineAnimator = null;
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        stopShineAnimation();
        super.onDetachedFromWindow();
    }
}
