package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.CharacterExpansion;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.portrait.PortraitGroupView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpansionCharactersSetupView extends ConstraintLayout {
    interface Listener {
        void onPortraitClick(@NonNull CharacterCard card);
        void onExpansionClick(@NonNull CharacterExpansion expansion);
    }

    private static final int PORTRAITS_PER_GROUP = 6;

    private ImageView imvIcon;
    private TextView txvTitle;
    private ImageView imvChecked;
    private LinearLayout llyPortraits;

    private CharacterExpansion expansion;
    private Listener listener;


    public ExpansionCharactersSetupView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ExpansionCharactersSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_expansion_characters_setup, this);

        imvIcon = findViewById(R.id.imvIcon_vwExpansionCharactersSetup);
        txvTitle = findViewById(R.id.txvTitle_vwExpansionCharactersSetup);
        imvChecked = findViewById(R.id.imvChecked_vwExpansionCharactersSetup);
        imvChecked.setOnClickListener(this::onCheckClick);
        llyPortraits = findViewById(R.id.llyPortraits_vwExpansionCharactersSetup);
    }

    public CharacterExpansion getExpansion() {
        return expansion;
    }

    public void setExpansion(@NonNull CharacterExpansion expansion) {
        this.expansion = expansion;

        // ICON
        imvIcon.setImageResource(expansion.getIcon());
        imvIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), expansion.getColor()));
        // TITLE
        txvTitle.setText(expansion.getTitle());
    }

    public void update(@NonNull List<CharacterCard> expansionCards,
                       @NonNull Set<CharacterCard> bannedCards) {
        updatePortraits(expansionCards, bannedCards);
        updateCheckedState(expansionCards, bannedCards);
    }

    private void updateCheckedState(@NonNull List<CharacterCard> expansionCards,
                                    @NonNull Set<CharacterCard> bannedCards) {
        boolean hasSelectedCharacter = expansionCards.stream().anyMatch(
                card -> !bannedCards.contains(card)
        );

        imvChecked.setImageResource(hasSelectedCharacter ? R.drawable.checked_box : R.drawable.unchecked_box);
    }

    private void updatePortraits(@NonNull List<CharacterCard> expansionCards,
                                 @NonNull Set<CharacterCard> bannedCards) {
        List<PortraitInfo> portraitInfos = expansionCards.stream()
                .sorted(CharacterCardUtils.getByAbilityThenByNameComparator(getContext()))
                .map(card -> getPortraitInfo(card, bannedCards.contains(card)))
                .collect(Collectors.toList());

        llyPortraits.removeAllViews();
        for (int start = 0; start < portraitInfos.size(); start += PORTRAITS_PER_GROUP) {
            int end = Math.min(start + PORTRAITS_PER_GROUP, portraitInfos.size());

            List<PortraitInfo> groupPortraitInfos = new ArrayList<>(end - start);
            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                groupPortraitInfos.add(portraitInfos.get(groupIdx));
            }

            PortraitGroupView portraitGroupView = new PortraitGroupView(
                    getContext(), groupPortraitInfos, PORTRAITS_PER_GROUP
            );
            portraitGroupView.setPortraitsClickListener(this::onPortraitClick);
            llyPortraits.addView(portraitGroupView, getPortraitGroupLayoutParams());
        }
    }

    @NonNull
    private PortraitInfo getPortraitInfo(@NonNull CharacterCard card, boolean isBanned) {
        return new PortraitInfo(card, CharacterSkinType.Default, isBanned, PortraitDisplayMode.Hexagonal);
    }

    private LinearLayout.LayoutParams getPortraitGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;

        return layoutParams;
    }

    private void onPortraitClick(View v) {
        if (listener != null) {
            listener.onPortraitClick(((PortraitView) v).getPortraitCard());
        }
    }

    private void onCheckClick(View v) {
        if (listener != null) {
            listener.onExpansionClick(expansion);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }
}
