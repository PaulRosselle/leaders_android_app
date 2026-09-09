package com.leaders.app.views.portrait;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;

public class AbilityPortraitGroupView extends ConstraintLayout {
    private static final int PORTRAITS_PER_GROUP = 6;

    private enum GroupType {
        Leaders,
        ActiveAbilities,
        PassiveAbilities,
        SpecialAbilities
    }

    private final List<PortraitGroupView> portraitGroupViews;

    public AbilityPortraitGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        portraitGroupViews = new ArrayList<>();

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.AbilityPortraitGroupView)) {
            GroupType groupType = GroupType.values()[customAttrs.getInteger(
                    R.styleable.AbilityPortraitGroupView_groupType,
                    GroupType.Leaders.ordinal()
            )];

            initHeader(groupType);
            initPortraits(groupType);
        }
    }

    private void initHeader(@NonNull GroupType groupType) {
        ImageView imvIcon = findViewById(R.id.imvIcon_vwAbilityPortraitGroup);
        TextView txvName = findViewById(R.id.txvName_vwAbilityPortraitGroup);

        switch (groupType) {
            case Leaders: {
                imvIcon.setImageResource(R.drawable.icon_leader);
                txvName.setText(R.string.group_name_leaders);
            } break;
            case ActiveAbilities: {
                imvIcon.setImageResource(R.drawable.icon_ability_active);
                txvName.setText(R.string.group_name_active_abilities);
            } break;
            case PassiveAbilities: {
                imvIcon.setImageResource(R.drawable.icon_ability_passive);
                txvName.setText(R.string.group_name_passive_abilities);
            } break;
            case SpecialAbilities: {
                imvIcon.setImageResource(R.drawable.icon_ability_special);
                txvName.setText(R.string.group_name_special_abilities);
            } break;
            default: throw new IllegalStateException("Unexpected group type: " + groupType);
        }
    }

    private void initPortraits(@NonNull GroupType groupType) {
        LinearLayout llyPortraits = findViewById(R.id.llyPortraits_vwAbilityPortraitGroup);

        List<PortraitInfo> portraitInfos = getPortraitInfos(groupType);
        for (int start = 0; start < portraitInfos.size(); start += PORTRAITS_PER_GROUP) {
            int end = Math.min(start + PORTRAITS_PER_GROUP, portraitInfos.size());

            List<PortraitInfo> groupPortraitInfos = new ArrayList<>(end - start);
            for (int groupIdx = start; groupIdx < end; groupIdx++) {
                groupPortraitInfos.add(portraitInfos.get(groupIdx));
            }

            addPortraitGroup(llyPortraits, groupPortraitInfos);
        }
    }

    private void addPortraitGroup(@NonNull LinearLayout llyPortraits,
                                  @NonNull List<PortraitInfo> groupPortraitInfos) {
        PortraitGroupView portraitsGroupView = new PortraitGroupView(
                getContext(), groupPortraitInfos, PORTRAITS_PER_GROUP
        );

        portraitGroupViews.add(portraitsGroupView);
        llyPortraits.addView(portraitsGroupView, getPortraitGroupLayoutParams());
    }

    private List<PortraitInfo> getPortraitInfos(@NonNull GroupType groupType) {
        List<PortraitInfo> portraitInfos = new ArrayList<>();

        for (CharacterCard card : CharacterCard.values()) {
            if (cardMatchGroupType(card, groupType)) {
                portraitInfos.add(new PortraitInfo(card, false, PortraitDisplayMode.Hexagonal));
            }
        }

        return portraitInfos;
    }

    private boolean cardMatchGroupType(@NonNull CharacterCard card, @NonNull GroupType groupType) {
        if (groupType == GroupType.Leaders) {
            return card.isLeader();
        }

        AbilityType[] abilityTypes = card.getAbilityTypes();
        if (abilityTypes.length < 1) {
            throw new IllegalStateException("Non leader cards must have an abilities");
        }

        switch (groupType) {
            case ActiveAbilities: return abilityTypes[0] == AbilityType.Active;
            case PassiveAbilities: return abilityTypes[0] == AbilityType.Passive;
            case SpecialAbilities: return abilityTypes[0] == AbilityType.Special;
            default: throw new IllegalStateException("Unexpected ability type: " + groupType);
        }
    }

    private LinearLayout.LayoutParams getPortraitGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.weight = 1;

        return layoutParams;
    }

    public void setOnPortraitClickListener(OnClickListener onPortraitClickListener) {
        for (PortraitGroupView portraitGroupView : portraitGroupViews) {
            portraitGroupView.setPortraitsClickListener(onPortraitClickListener);
        }
    }
}
