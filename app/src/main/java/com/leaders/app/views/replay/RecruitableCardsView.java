package com.leaders.app.views.replay;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.CharacterCardPortraitGroupView;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.queries.SelectableCardsQuery;

import java.util.ArrayList;
import java.util.List;

public class RecruitableCardsView extends ConstraintLayout {
    private static final int PORTRAITS_PER_GROUP = 8;

    private final LinearLayout llyPortraits;

    public RecruitableCardsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_recruitable_cards, this);

        llyPortraits = findViewById(R.id.llyPortraits_vwRecruitableCards);
    }

    public void updateRecruitableCards(@NonNull Game game, @NonNull GameMode gameMode) {
        llyPortraits.removeAllViews();

        List<CharacterCard> availableCards = SelectableCardsQuery.getAvailableCards(game, gameMode);

        List<CharacterCard> recruitableCards = new ArrayList<>(game.getRecruitableCards());
        Context context = getContext();
        CharacterCardUtils.sort(context, recruitableCards);

        while (!recruitableCards.isEmpty()) {
            // We add cards line per line within multiple "PortraitGroupView".
            // For each group, an array is alimented
            int portraitsInLineCount = Math.min(PORTRAITS_PER_GROUP, recruitableCards.size());
            ArrayList<CharacterCard> portraitsCards = new ArrayList<>();
            for (int i = 0; i < portraitsInLineCount; i++) {
                portraitsCards.add(recruitableCards.remove(0));
            }
            CharacterCardPortraitGroupView ptvPortraits = CharacterCardPortraitGroupView.createFromCards(
                    context, portraitsCards, PORTRAITS_PER_GROUP
            );
            ptvPortraits.setClickable(false);
            ptvPortraits.setLongClickable(false);
            for (CharacterCardPortraitView portraitView : ptvPortraits.getPortraits()) {
                portraitView.setDisplayMode(CharacterCardPortraitView.DisplayMode.Hexagonal);
                if (!availableCards.contains(portraitView.getPortraitCard())) {
                    portraitView.setAlpha(0.5f);
                }
            }

            llyPortraits.addView(ptvPortraits, getPortraitsGroupLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getPortraitsGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;
        return layoutParams;
    }

    public void setOnCardPortraitClick(@Nullable OnClickListener onClickListener) {
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((CharacterCardPortraitGroupView) llyPortraits.getChildAt(i)).setPortraitsClickListener(onClickListener);
        }
    }

    public void setOnCardPortraitLongClick(@Nullable OnLongClickListener onLongClickListener) {
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((CharacterCardPortraitGroupView) llyPortraits.getChildAt(i)).setPortraitsLongClickListener(onLongClickListener);
        }
    }
}
