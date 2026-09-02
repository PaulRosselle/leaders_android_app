package com.leaders.app.views.character;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CharacterCardPortraitGroupView extends LinearLayout {
    private static final int PORTRAIT_DEFAULT_MARGIN = 2;

    private final ArrayList<CharacterCardPortraitView> characterCardPortraitViews;
    private OnClickListener onPortraitClickListener;
    private OnLongClickListener onPortraitLongClickListener;
    private int portraitSpacing;


    public CharacterCardPortraitGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        characterCardPortraitViews = new ArrayList<>();
        onPortraitClickListener = null;
        onPortraitLongClickListener = null;
        portraitSpacing = PORTRAIT_DEFAULT_MARGIN;

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterCardPortraitGroupView)) {
            int groupSize = customAttrs.getInteger(R.styleable.CharacterCardPortraitGroupView_maxGroupSize, 6);
            setPortraits(new ArrayList<>(), groupSize);
        }

        setOrientation(HORIZONTAL);
    }

    public void setPortraits(@NonNull List<CharacterCard> portraitCards, int groupSize) {
        // The smallest value for group max size is 2 since it wouldn't make
        // sense to create a group view for less than two portraits
        groupSize = Math.max(groupSize, 2);

        characterCardPortraitViews.clear();
        removeAllViews();

        for (int i = 0; i < groupSize; i++) {
            CharacterCardPortraitView portraitView = new CharacterCardPortraitView(getContext(), null);

            if (i < portraitCards.size()) {
                portraitView.setVisibility(VISIBLE);
                portraitView.setPortraitCard(portraitCards.get(i));
            } else {
                portraitView.setVisibility(INVISIBLE);
            }

            addView(portraitView, getPortraitLayoutParams());
            characterCardPortraitViews.add(portraitView);
        }

        updatePortraitsClickListener();
        updatePortraitsLongClickListener();
    }

    public List<CharacterCardPortraitView> getPortraits() {
        List<CharacterCardPortraitView> portraitViews = new ArrayList<>();
        for (CharacterCardPortraitView portraitView : characterCardPortraitViews) {
            if (portraitView.getVisibility() == VISIBLE) {
                portraitViews.add(portraitView);
            }
        }

        return Collections.unmodifiableList(portraitViews);
    }

    public void setPortraitSpacing(int portraitSpacing) {
        this.portraitSpacing = portraitSpacing;
        for (CharacterCardPortraitView ptvPortrait : characterCardPortraitViews) {
            ptvPortrait.setLayoutParams(getPortraitLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getPortraitLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int marginValueInPx = (int) (portraitSpacing * getResources().getDisplayMetrics().density);
        layoutParams.setMarginStart(marginValueInPx);
        layoutParams.setMarginEnd(marginValueInPx);
        layoutParams.weight = 1;
        return layoutParams;
    }

    public void setPortraitsClickListener(OnClickListener onPortraitClickListener) {
        this.onPortraitClickListener = onPortraitClickListener;
        updatePortraitsClickListener();
    }

    public void setPortraitsLongClickListener(OnLongClickListener onPortraitLongClickListener) {
        this.onPortraitLongClickListener = onPortraitLongClickListener;
        updatePortraitsLongClickListener();
    }

    private void updatePortraitsClickListener() {
        for (CharacterCardPortraitView characterCardPortraitView : characterCardPortraitViews) {
            characterCardPortraitView.setOnClickListener(onPortraitClickListener);
        }
    }

    private void updatePortraitsLongClickListener() {
        for (CharacterCardPortraitView characterCardPortraitView : characterCardPortraitViews) {
            characterCardPortraitView.setOnLongClickListener(onPortraitLongClickListener);
        }
    }

    public static CharacterCardPortraitGroupView createFromCards(@NonNull Context context,
                                                                 @NonNull List<CharacterCard> portraitCards,
                                                                 int groupSize) {
        CharacterCardPortraitGroupView groupView = new CharacterCardPortraitGroupView(context, null);
        groupView.setPortraits(portraitCards, groupSize);
        return groupView;
    }


    public static CharacterCardPortraitGroupView createFromTargets(@NonNull Context context,
                                                                   @NonNull List<InteractionTarget> portraitTargets,
                                                                   int groupSize) {
        CharacterCardPortraitGroupView groupView = new CharacterCardPortraitGroupView(context, null);

        List<CharacterCard> portraitCards = new ArrayList<>();
        for (InteractionTarget target : portraitTargets) {
            portraitCards.add(Objects.requireNonNull(
                            target.getChosenSelectableCharacterCard(),
                            "Invalid portrait target: character card missing")
                    .getCharacterCard()
            );
        }

        groupView.setPortraits(portraitCards, groupSize);

        for (int i = 0; i < portraitTargets.size(); i++) {
            groupView.characterCardPortraitViews.get(i).setTarget(portraitTargets.get(i));
        }

        return groupView;
    }

    public static CharacterCardPortraitGroupView createFromSelectableCards(@NonNull Context context,
                                                                           @NonNull List<SelectableCharacterCard> selectableCards,
                                                                           int groupSize) {
        CharacterCardPortraitGroupView groupView = new CharacterCardPortraitGroupView(context, null);

        List<CharacterCard> portraitCards = new ArrayList<>();
        for (SelectableCharacterCard selectableCard : selectableCards) {
            portraitCards.add(selectableCard.getCharacterCard());
        }

        groupView.setPortraits(portraitCards, groupSize);

        for (int i = 0; i < selectableCards.size(); i++) {
            groupView.characterCardPortraitViews.get(i).setUseBannedDisplay(
                    selectableCards.get(i).getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned);
        }

        return groupView;
    }
}
