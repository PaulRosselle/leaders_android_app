package com.leaders.app.views.mainmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.AppSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MainMenuView extends ConstraintLayout {
    public interface OnSectionClickListener {
        void onSectionClick(@NonNull AppSection appSection);
    }

    private final Map<View, AppSection> viewSectionMap;

    private OnSectionClickListener sectionClickListener;

    public MainMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_main_menu, this);

        viewSectionMap = new HashMap<>();

        addSection(R.id.mmbvPuzzles_vwMainMenu, AppSection.Puzzles);
        addSection(R.id.mmbvDuel_vwMainMenu, AppSection.Duel);
        addSection(R.id.mmbvReplay_vwMainMenu, AppSection.Replay);
        addSection(R.id.mmbvRules_vwMainMenu, AppSection.Rules);
        addSection(R.id.mmbvSettings_vwMainMenu, AppSection.Settings);
        addSection(R.id.mmbvCredits_vwMainMenu, AppSection.Credits);
    }

    private void addSection(int viewResId, @NonNull AppSection appSection) {
        View sectionView = findViewById(viewResId);
        viewSectionMap.put(sectionView, appSection);
        sectionView.setOnClickListener(this::onSectionViewClick);
    }

    private void onSectionViewClick(View v) {
        if (sectionClickListener != null) {
            sectionClickListener.onSectionClick(
                    Objects.requireNonNull(viewSectionMap.get(v),
                            "All sections should be mapped to a view within main menu"
                    )
            );
        }
    }

    public void setSectionClickListener(OnSectionClickListener sectionClickListener) {
        this.sectionClickListener = sectionClickListener;
    }
}
