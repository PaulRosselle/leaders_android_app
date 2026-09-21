package com.leaders.app.activities.rules.tutorial;

import android.text.Spannable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.SpannableUtils;

import java.util.List;

public final class RulesTutorialCaptureActivity extends RulesTutorialActivity {
    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.CaptureLeader;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialCapture;
    }

    protected Spannable getTextBeforeSpannable() {
        int iconSize = getResources().getDimensionPixelSize(R.dimen.image_span_default_size);

        ImageSpan iconSpan = SpannableUtils.getImageSpan(
                this,
                R.drawable.character_piece_leader_queen_w,
                iconSize,
                iconSize
        );

        return SpannableUtils.getImageSpannable(
                this,
                getChapter().getTextBeforeResId(),
                List.of(iconSpan)
        );
    }
}
