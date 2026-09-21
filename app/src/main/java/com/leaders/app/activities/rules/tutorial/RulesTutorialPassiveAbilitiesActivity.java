package com.leaders.app.activities.rules.tutorial;

import android.text.Spannable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.SpannableUtils;

import java.util.List;

public class RulesTutorialPassiveAbilitiesActivity extends RulesTutorialActivity {
    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialPassiveAbilities;
    }

    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.PassiveAbilities;
    }

    protected Spannable getTextBeforeSpannable() {
        int iconSize = getResources().getDimensionPixelSize(R.dimen.image_span_default_size);

        ImageSpan iconSpan = SpannableUtils.getImageSpan(
                this,
                R.drawable.icon_ability_passive,
                iconSize,
                iconSize
        );

        ImageSpan acrobatSpan = SpannableUtils.getImageSpan(
                this,
                R.drawable.character_piece_vizier_b,
                iconSize,
                iconSize
        );

        ImageSpan illusionistSpan = SpannableUtils.getImageSpan(
                this,
                R.drawable.character_piece_leader_king_b,
                iconSize,
                iconSize
        );

        return SpannableUtils.getImageSpannable(
                this,
                getChapter().getTextBeforeResId(),
                List.of(iconSpan, acrobatSpan, illusionistSpan)
        );
    }
}
