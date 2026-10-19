package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.queries.AutoPlayQuery;
import com.leaders.puzzlelogic.entities.blocker.NemesisBlocker;
import com.leaders.puzzlelogic.entities.blocker.NemesisBlockerCallback;

public class NemesisBlockerView extends ConstraintLayout {
    private final MaterialButton btnInterrupt;
    private NemesisBlocker nemesisBlocker;

    public NemesisBlockerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_nemesis_blocker, this);

        PortraitView ptvPortrait = findViewById(R.id.ptvPortrait_vwNemesisBlocker);
        ptvPortrait.setPortraitCard(CharacterCard.Nemesis);
        btnInterrupt = findViewById(R.id.btnInterrupt_vwNemesisBlocker);

        // Added shadow behind the notification
        setBackgroundResource(R.drawable.round_rect_gloden_outline_bg);
        setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setElevation(getResources().getDisplayMetrics().density * 8);

        nemesisBlocker = null;
    }

    public boolean canAutoBlock(@NonNull InteractionRequest request) {
        return AutoPlayQuery.canAutoPlayCharacter(GameMode.Puzzle, request);
    }

    public void clear() {
        nemesisBlocker = null;
    }

    @NonNull
    public NemesisBlocker initBlocker(@NonNull GameHistory gameHistory,
                                      @NonNull InteractionRequest request,
                                      @NonNull NemesisBlockerCallback callback) {
        nemesisBlocker = new NemesisBlocker(gameHistory, request, callback);
        return nemesisBlocker;
    }

    public void shutdownBlocker() {
        if (nemesisBlocker == null) {
            return;
        }

        if (nemesisBlocker.canBeInterrupted()) {
            nemesisBlocker.cancel();
        }
        clear();
    }

    public void setOnInterruptClickListener(OnClickListener onClickListener) {
        btnInterrupt.setOnClickListener(onClickListener);
    }
}
