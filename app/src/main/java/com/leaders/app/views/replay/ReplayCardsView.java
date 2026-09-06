package com.leaders.app.views.replay;

import android.content.Context;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.PortraitGroupView;
import com.leaders.app.views.character.PortraitView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.queries.SelectableCardsQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReplayCardsView extends ConstraintLayout {
    private static final int GROUP_MARGIN_TOP = 4;
    private static final int DISCOVERY_PORTRAITS_PER_GROUP = 3;
    private static final int DISCOVERY_PORTRAIT_SPACING = 32;
    private static final int STRATEGIST_PORTRAITS_PER_GROUP = 8;
    private static final int STRATEGIST_PORTRAIT_SPACING = 2;
    private static final float RECRUITED_PORTRAIT_ALPHA = 0.4f;


    private final ScrollView scvPortraits;
    private final LinearLayout llyPortraits;

    private OnLongClickListener onPortraitLongClickListener;


    public ReplayCardsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_recruitable_cards, this);

        scvPortraits = findViewById(R.id.scvPortraits_vwRecruitableCards);
        llyPortraits = findViewById(R.id.llyPortraits_vwRecruitableCards);
    }

    public void updatePortraits(@NonNull Game game, @NonNull GameMode gameMode) {
        List<PortraitGroupView> portraitGroups;
        switch (gameMode) {
            case Discovery: portraitGroups = getDiscoveryPortraitGroups(game); break;
            case Strategist: portraitGroups = getStrategistPortraitGroups(game); break;
            default: throw new IllegalStateException("No portrait update possible for game mode: " + gameMode);
        }

        llyPortraits.removeAllViews();

        for (PortraitGroupView pgvGroup : portraitGroups) {
            pgvGroup.setDisplayMode(PortraitView.DisplayMode.Hexagonal);
            llyPortraits.addView(pgvGroup, getPortraitsGroupLayoutParams());
        }

        updatePortraitsScrollView();
    }

    private List<PortraitGroupView> getDiscoveryPortraitGroups(@NonNull Game game) {
        List<CharacterCard> availableCards = SelectableCardsQuery.getAvailableCards(game, GameMode.Discovery);

        return getPortraitGroups(
                availableCards,
                DISCOVERY_PORTRAITS_PER_GROUP,
                DISCOVERY_PORTRAIT_SPACING
        );
    }

    private List<PortraitGroupView> getStrategistPortraitGroups(@NonNull Game game) {
        List<CharacterCard> availableCards = SelectableCardsQuery.getAvailableCards(game, GameMode.Strategist);
        List<CharacterCard> recruitedCards = getRecruitedCards(game);
        List<CharacterCard> bannedCards = getBannedCards(game);

        List<CharacterCard> portraitsCards = new ArrayList<>();
        portraitsCards.addAll(availableCards);
        portraitsCards.addAll(recruitedCards);
        portraitsCards.addAll(bannedCards);

        List<PortraitGroupView> portraitGroups = getPortraitGroups(
                portraitsCards,
                STRATEGIST_PORTRAITS_PER_GROUP,
                STRATEGIST_PORTRAIT_SPACING
        );

        for (PortraitGroupView pgvGroup : portraitGroups) {
            for (PortraitView ptvPortrait : pgvGroup.getPortraits()) {
                CharacterCard portraitCard = ptvPortrait.getPortraitCard();
                if (bannedCards.contains(portraitCard)) {
                    ptvPortrait.setUseBannedDisplay(true);
                } else if (recruitedCards.contains(portraitCard)) {
                    ptvPortrait.setAlpha(RECRUITED_PORTRAIT_ALPHA);
                }
            }
        }
        return portraitGroups;
    }

    private List<PortraitGroupView> getPortraitGroups(@NonNull List<CharacterCard> portraitCards,
                                                      int portraitsPerGroup,
                                                      int portraitsSpacing) {
        List<PortraitGroupView> portraitGroups = new ArrayList<>();

        CharacterCardUtils.sort(getContext(), portraitCards);

        while (!portraitCards.isEmpty()) {
            // We add cards line per line within multiple "PortraitGroupView".
            // For each group, an array is alimented
            int portraitsInLineCount = Math.min(portraitsPerGroup, portraitCards.size());
            ArrayList<CharacterCard> portraitsCards = new ArrayList<>();
            for (int i = 0; i < portraitsInLineCount; i++) {
                portraitsCards.add(portraitCards.remove(0));
            }
            PortraitGroupView pgvGroup = PortraitGroupView.createFromCards(
                    getContext(), portraitsCards, portraitsPerGroup
            );
            pgvGroup.setPortraitsLongClickListener(onPortraitLongClickListener);
            pgvGroup.setPortraitSpacing(portraitsSpacing);

            portraitGroups.add(pgvGroup);
        }

        return portraitGroups;
    }

    private LinearLayout.LayoutParams getPortraitsGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (GROUP_MARGIN_TOP * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;

        return layoutParams;
    }

    private void updatePortraitsScrollView() {
        LayoutParams params = (LayoutParams) scvPortraits.getLayoutParams();

        int availableHeight = scvPortraits.getMeasuredHeight();
        if (availableHeight <= 0) {
            scvPortraits.post(this::updatePortraitsScrollView);
            return;
        }

        int portraitsHeight = llyPortraits.getMeasuredHeight();

        if (portraitsHeight <= availableHeight) {
            // The whole portraits linear layout fits on screen.
            // Let the ScrollView have its natural width and center it.
            params.height = LayoutParams.WRAP_CONTENT;

        } else {
            // The portraits linear layout is wider than the screen.
            // Make the ScrollView fill the available width so it can scroll.
            params.height = LayoutParams.MATCH_CONSTRAINT;

        }

        scvPortraits.setLayoutParams(params);
    }

    private List<CharacterCard> getRecruitedCards(@NonNull Game game) {
        Set<CharacterCard> recruitedCards = new HashSet<>();

        for (Character recruitedCharacter : game.getRecruitedCharacters()) {
            CharacterCard card = recruitedCharacter.getCharacterType().getCharacterCard();
            if (card.canBeRecruited()) {
                recruitedCards.add(card);
            }
        }

        return List.copyOf(recruitedCards);
    }

    private List<CharacterCard> getBannedCards(@NonNull Game game) {
        List<CharacterCard> bannedCards = new ArrayList<>();

        for (TeamColor teamColor : TeamColor.values()) {
            bannedCards.addAll(game.getBanishedCards(teamColor));
        }

        return bannedCards;
    }

    public void setOnCardPortraitLongClick(@Nullable OnLongClickListener onLongClickListener) {
        this.onPortraitLongClickListener = onLongClickListener;
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((PortraitGroupView) llyPortraits.getChildAt(i))
                    .setPortraitsLongClickListener(onLongClickListener);
        }
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
