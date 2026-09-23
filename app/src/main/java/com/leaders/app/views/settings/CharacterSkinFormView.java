package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.CharacterCardAdapter;
import com.leaders.app.views.character.CharacterCardView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterSkinFormView extends ConstraintLayout {
    @NonNull
    private final LinearLayout llyPortraits;
    @NonNull
    private final RecyclerView rcvCards;

    private final List<PortraitView> portraitViews;
    private final LinearSnapHelper cardSnapHelper;
    private final LinearLayoutManager cardLayoutManager;
    private CharacterCardAdapter cardAdapter;

    public CharacterSkinFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_skin_form, this);

        llyPortraits = findViewById(R.id.llyPortraits_vwCharacterSkinForm);
        rcvCards = findViewById(R.id.rcvCards_vwCharacterSkinForm);

        portraitViews = new ArrayList<>();
        cardSnapHelper = new LinearSnapHelper();
        cardLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        initViews();
        initListeners();
        initDatas();
    }

    private void initViews() {
        initPortraits();
        initCards();
    }

    private void initListeners() {
        findViewById(R.id.btnChoose_vwCharacterSkinForm).setOnClickListener(this::onChooseClick);

        rcvCards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                float center = recyclerView.getWidth() / 2f;

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);

                    float childCenter = (child.getLeft() + child.getRight()) / 2f;

                    float distance = Math.abs(center - childCenter);
                    float normalized = Math.min(distance / center, 1f);
                    float scale = 1f - (0.15f * normalized);

                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            }
        });
    }

    private void initDatas() {
        selectPortrait(portraitViews.get(0));
    }

    private void initPortraits() {
        for (CharacterCard card : CharacterCard.values()) {
            List<CharacterSkinType> skins = CharacterCardUtils.getSkins(card);
            if (skins.size() > 1) {
                PortraitView portraitView = new PortraitView(getContext(), null);
                // TODO - handle skin appearance
                portraitView.setInfo(new PortraitInfo(card, false, PortraitDisplayMode.Hexagonal));
                portraitView.setOnClickListener(this::onPortraitClick);
                portraitViews.add(portraitView);
                llyPortraits.addView(portraitView, getPortraitLayoutParams());
            }
        }
    }
    private void initCards() {
        rcvCards.setLayoutManager(cardLayoutManager);
        cardSnapHelper.attachToRecyclerView(rcvCards);
    }

    private void onPortraitClick(View v) {
        selectPortrait((PortraitView) v);
    }

    private void onChooseClick(View v) {
        View snappedView = cardSnapHelper.findSnapView(cardLayoutManager);
        if (!(snappedView instanceof CharacterCardView)) {
            return;
        }

        CharacterCardView cardView = (CharacterCardView) snappedView;
        selectCardSkin(cardView.getCard(), cardView.getSkinType());
    }

    private void selectPortrait(@NonNull PortraitView ptvSelected) {
        for (PortraitView ptvPortrait : portraitViews) {
            ptvPortrait.setAlpha(ptvPortrait == ptvSelected ? 1f : 0.5f);
        }

        CharacterCard card = ptvSelected.getPortraitCard();

        loadCardSkins(card);
    }

    private void selectCardSkin(@NonNull CharacterCard card, @NonNull CharacterSkinType skinType) {
        for (int i = 0; i < rcvCards.getChildCount(); i++) {
            View childView = rcvCards.getChildAt(i);
            if (childView instanceof CharacterCardView) {
                CharacterCardView cardView = (CharacterCardView) childView;
                cardView.setHighlighted(cardView.getCard() == card && cardView.getSkinType() == skinType);
            }
        }

        cardAdapter.setSelectedSkinType(skinType);
        // TODO - save choice
    }

    private void loadCardSkins(@NonNull CharacterCard card) {
        List<CharacterSkinType> skins = CharacterCardUtils.getSkins(card);

        List<Map.Entry<CharacterCard, CharacterSkinType>> cardSkinMap = new ArrayList<>();
        for (CharacterSkinType skinType : skins) {
            cardSkinMap.add(Map.entry(card, skinType));
        }

        cardAdapter = new CharacterCardAdapter(cardSkinMap, CharacterSkinType.Default);
        rcvCards.setAdapter(cardAdapter);
    }

    private LinearLayout.LayoutParams getPortraitLayoutParams() {
        return new LinearLayout.LayoutParams(
                (int) (60 * getResources().getDisplayMetrics().density),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }
}
