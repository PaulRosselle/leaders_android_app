package com.leaders.app.activities.rules.tutorial;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.entities.CharacterPiece;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.SpannableUtils;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

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

        Context context = getBaseContext();
        ImageSpan acrobatSpan = SpannableUtils.getImageSpan(
                new CharacterPiece(
                        CharacterType.Vizier,
                        CharacterSkinType.Default,
                        TeamColor.Black
                ).getDrawable(context),
                iconSize,
                iconSize
        );

        ImageSpan illusionistSpan = SpannableUtils.getImageSpan(
                new CharacterPiece(
                        CharacterType.LeaderKing,
                        CharacterSkinType.Default,
                        TeamColor.Black
                ).getDrawable(context),
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
