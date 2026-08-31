package com.leaders.app.views.duel;

import android.content.Context;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.CharacterCardPortraitGroupView;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CharacterCardSelectionView extends ConstraintLayout {
    public interface OnCardSelectedListener {
        void onRecruitmentCardSelected(@NonNull InteractionTarget target);
        void onBanishmentCardSelected(@NonNull InteractionTarget target);
    }

    private final LinearLayout llyPortraits;
    private final ScrollView scvPortraits;

    private int portraitsPerGroup;
    private int portraitSpacing;
    private List<InteractionTarget> targets;

    private OnLongClickListener onPortraitLongClickListener;

    private OnCardSelectedListener onCardSelectedListener;

    public CharacterCardSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_card_selection, this);

        llyPortraits = findViewById(R.id.llyPortraits_vwCharacterCardSelection);
        scvPortraits = findViewById(R.id.scvPortraits_vwCharacterCardSelection);

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

    public void setTargets(@NonNull List<InteractionTarget> targets) {
        this.targets = targets;
        updatePortraitsFromTargets();
    }

    public void setCards(@NonNull List<SelectableCharacterCard> selectableCards) {
        this.targets = null;
        updatePortraitsFromCards(selectableCards);
    }

    private void updatePortraitsFromTargets() {
        llyPortraits.removeAllViews();

        for (int start = 0; start < targets.size(); start += portraitsPerGroup) {
            int end = Math.min(start + portraitsPerGroup, targets.size());

            List<InteractionTarget> groupTargets = new ArrayList<>(end - start);

            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                InteractionTarget target = targets.get(groupIdx);

                groupTargets.add(target);
            }

            CharacterCardPortraitGroupView portraitsGroupView = CharacterCardPortraitGroupView.createFromTargets(
                    getContext(), groupTargets, portraitsPerGroup
            );
            initPortraitsGroup(portraitsGroupView);
        }

        scvPortraits.setVisibility(VISIBLE);
    }


    private void updatePortraitsFromCards(@NonNull List<SelectableCharacterCard> selectableCards) {
        llyPortraits.removeAllViews();

        for (int start = 0; start < selectableCards.size(); start += portraitsPerGroup) {
            int end = Math.min(start + portraitsPerGroup, selectableCards.size());

            List<SelectableCharacterCard> groupCards = new ArrayList<>(end - start);

            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                groupCards.add(selectableCards.get(groupIdx));
            }

            CharacterCardPortraitGroupView portraitsGroupView = CharacterCardPortraitGroupView.createFromSelectableCards(
                    getContext(), groupCards, portraitsPerGroup
            );
            initPortraitsGroup(portraitsGroupView);
        }

        scvPortraits.setVisibility(VISIBLE);
    }

    private void initPortraitsGroup(@NonNull CharacterCardPortraitGroupView portraitsGroupView) {
        portraitsGroupView.setPortraitsClickListener(this::onPortraitClick);
        portraitsGroupView.setPortraitsLongClickListener(onPortraitLongClickListener);
        portraitsGroupView.setPortraitSpacing(portraitSpacing);

        llyPortraits.addView(portraitsGroupView, getPortraitsGroupLP());
    }

    private void onPortraitClick(View v) {
        CharacterCardPortraitView portraitView = (CharacterCardPortraitView) v;

        InteractionTarget target = portraitView.getTarget();
        if (target == null) {
            return;
        }

        if (target.getCategory().getResultType() != InteractionResultType.SelectableCharacterCardChosen) {
            throw new IllegalStateException("Invalid portrait target category: " + target.getCategory());
        }

        SelectableCharacterCard selectedCard = Objects.requireNonNull(
                target.getChosenSelectableCharacterCard(),
                "Invalid portrait target: selectable character card missing"
        );

        switch (selectedCard.getSelectionStatus()) {
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

    private void onRecruitableCardSelected(@NonNull InteractionTarget target) {
        if (onCardSelectedListener != null) {
            onCardSelectedListener.onRecruitmentCardSelected(target);
        }
    }
    private void onBanishableCardSelected(@NonNull InteractionTarget target) {
        if (onCardSelectedListener != null) {
            onCardSelectedListener.onBanishmentCardSelected(target);
        }
    }

    private void onInvalidCardSelected(@NonNull SelectableCharacterCard selectedCard) {
        String messageFormat;
        if (selectedCard.getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned) {
            messageFormat = getContext().getString(R.string.card_already_banned);
        } else {
            messageFormat = getContext().getString(R.string.card_cannot_be_recruited);
        }
        String cardName = getContext().getString(CharacterCardUtils.getNameId(selectedCard.getCharacterCard()));

        Toast.makeText(getContext(), String.format(messageFormat, cardName), Toast.LENGTH_SHORT).show();
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
}
