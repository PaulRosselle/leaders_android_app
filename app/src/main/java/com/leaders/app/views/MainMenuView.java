package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public final class MainMenuView extends ConstraintLayout {
    public MainMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_main_menu, this);
    }

    public void setOnPlayClickListener(OnClickListener onClickListener) {
        findViewById(R.id.mmbvPlay_vwMainMenu).setOnClickListener(onClickListener);
    }

    public void setOnReplayClickListener(OnClickListener onClickListener) {
        findViewById(R.id.mmbvReplay_vwMainMenu).setOnClickListener(onClickListener);
    }

    public void setOnPuzzlesClickListener(OnClickListener onClickListener) {
        findViewById(R.id.mmbvPuzzles_vwMainMenu).setOnClickListener(onClickListener);
    }

    public void setOnRulesClickListener(OnClickListener onClickListener) {
        findViewById(R.id.mmbvRules_vwMainMenu).setOnClickListener(onClickListener);
    }

    public void setOnSettingsClickListener(OnClickListener onClickListener) {
        findViewById(R.id.mmbvSettings_vwMainMenu).setOnClickListener(onClickListener);
    }
}
