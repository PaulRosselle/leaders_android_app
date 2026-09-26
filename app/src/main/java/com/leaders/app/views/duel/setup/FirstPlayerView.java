package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.FirstPlayerTeam;

import java.util.ArrayList;
import java.util.List;

public class FirstPlayerView extends ConstraintLayout {
    private final List<FirstPlayerButtonView> teamButtons;

    public FirstPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_first_player, this);

        teamButtons = new ArrayList<>();
        initButtons();
    }

    private void initButtons() {
        Context context = getContext();
        int buttonHeight = getResources().getDimensionPixelSize(R.dimen.round_button_size);

        LinearLayout llyButtons = findViewById(R.id.llyButtons_vwFirstPlayer);
        llyButtons.removeAllViews();
        for (FirstPlayerTeam team : FirstPlayerTeam.values()) {
            FirstPlayerButtonView teamButton = new FirstPlayerButtonView(context, team);
            teamButton.setOnTeamSelectedListener(this::onTeamSelectClick);
            llyButtons.addView(teamButton, getButtonLayoutParams(buttonHeight));
            teamButtons.add(teamButton);
        }

        setTeam(FirstPlayerTeam.Random);
    }

    private LinearLayout.LayoutParams getButtonLayoutParams(int height) {
        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                height,
                1
        );

        int horizontalMargin = (int) (4 * getResources().getDisplayMetrics().density);
        params.setMarginStart(horizontalMargin);
        params.setMarginEnd(horizontalMargin);

        return params;
    }

    private void onTeamSelectClick(@NonNull FirstPlayerTeam team) {
        setTeam(team);
    }

    public void setTeam(@NonNull FirstPlayerTeam team) {
        for (FirstPlayerButtonView teamButton : teamButtons) {
            teamButton.setTeamSelected(teamButton.getTeam() == team);
        }
    }

    public FirstPlayerTeam getTeam() {
        for (FirstPlayerButtonView teamButton : teamButtons) {
            if (teamButton.isSelectedTeam()) {
                return teamButton.getTeam();
            }
        }
        throw new IllegalStateException("No first team selected");
    }
}
