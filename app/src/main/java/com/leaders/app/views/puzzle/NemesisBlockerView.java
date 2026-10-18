package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

public class NemesisBlockerView extends ConstraintLayout {
    public NemesisBlockerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_nemesis_blocker, this);

        PortraitView ptvPortrait = findViewById(R.id.ptvPortrait_vwNemesisBlocker);
        ptvPortrait.setPortraitCard(CharacterCard.Nemesis);

        // Added shadow behind the notification
        setBackgroundResource(R.drawable.round_rect_gloden_outline_bg);
        setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setElevation(getResources().getDisplayMetrics().density * 8);
    }
}
