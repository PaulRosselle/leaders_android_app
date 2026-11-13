package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.CharactersSetup;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.utilities.DuelStartUtils;
import com.leaders.app.views.portrait.PortraitGroupView;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CharactersSetupView extends ConstraintLayout {
    public interface OpenFormListener {
        void onOpenFormClick(@NonNull CharactersSetup setup);
    }

    private static final int PORTRAITS_PER_GROUP = 8;

    private LinearLayout llyPortraits;
    private TextView txvInfo;
    private TextView txvCharactersCount;

    private CharactersSetup setup;

    private OpenFormListener openFormListener;
    private OnLongClickListener onPortraitLongClick;


    public CharactersSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_characters_setup, this);

        llyPortraits = findViewById(R.id.llyPortraits_vwCharactersSetup);
        llyPortraits.setOnClickListener(this::openFormClick);
        txvInfo = findViewById(R.id.txvInfo_vwCharactersSetup);
        txvCharactersCount = findViewById(R.id.txvCharactersCount_vwCharactersSetup);

        if (isInEditMode()) {
            applySetup(new CharactersSetup(new HashSet<>(), new HashSet<>()));
        }
    }

    public void applySetup(@NonNull CharactersSetup setup) {
        this.setup = new CharactersSetup(setup);

        List<CharacterCard> remainingCharacters = setup.getRemainingCharacters();
        int remainingCharactersCount = remainingCharacters.size();

        updatePortraits(remainingCharacters);
        updateInfo(remainingCharactersCount);
        updateEditIconPosition(remainingCharactersCount);
    }

    public CharactersSetup getSetup() {
        return setup;
    }

    private void updatePortraits(@NonNull List<CharacterCard> characterCards) {
        List<PortraitInfo> portraitInfos = characterCards.stream()
                .sorted(CharacterCardUtils.getByAbilityThenByNameComparator(getContext()))
                .map(card -> new PortraitInfo(
                        card, CharacterSkinType.Default, false, PortraitDisplayMode.Hexagonal
                )).collect(Collectors.toList());

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
            portraitGroupView.setPortraitsClickListener(this::openFormClick);
            portraitGroupView.setPortraitsLongClickListener(onPortraitLongClick);
            llyPortraits.addView(portraitGroupView, getPortraitGroupLayoutParams());
        }
    }

    private void updateInfo(int remainingCharactersCount) {
        int bansCount = remainingCharactersCount - DuelStartUtils.CHARACTERS_START_COUNT;
        String charactersInfo = DuelStartUtils.getCharactersInfo(getContext(), bansCount);
        txvInfo.setVisibility(charactersInfo.isEmpty() ? GONE : VISIBLE);
        txvInfo.setText(charactersInfo);
        txvCharactersCount.setText(String.format("%s/%s", remainingCharactersCount, DuelStartUtils.CHARACTERS_START_COUNT));
    }

    private void updateEditIconPosition(int displayedPortraitsCount) {
        Resources resources = getResources();

        llyPortraits.setPadding(
                llyPortraits.getPaddingLeft(),
                llyPortraits.getPaddingTop(),
                llyPortraits.getPaddingRight(),
                // We offset the bottom margin to display the icon when rows are full
                displayedPortraitsCount % PORTRAITS_PER_GROUP != 0 ? 0 :
                        resources.getDimensionPixelSize(R.dimen.characters_edit_icon_size) +
                        resources.getDimensionPixelSize(R.dimen.characters_edit_icon_margin)
        );
    }

    @NonNull
    private LinearLayout.LayoutParams getPortraitGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);

        return layoutParams;
    }

    private void openFormClick(View v) {
        if (openFormListener != null) {
            openFormListener.onOpenFormClick(getSetup());
        }
    }

    public void setOpenFormListener(OpenFormListener openFormListener) {
        this.openFormListener = openFormListener;
    }

    public void setOnPortraitLongClick(OnLongClickListener onPortraitLongClick) {
        this.onPortraitLongClick = onPortraitLongClick;
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            View child = llyPortraits.getChildAt(i);
            if (child instanceof PortraitGroupView) {
                ((PortraitGroupView) child).setPortraitsLongClickListener(onPortraitLongClick);
            }
        }
    }
}
