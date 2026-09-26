package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.gamelogic.enums.GameMode;

import java.util.List;

public class GameModeView extends ConstraintLayout {
    private final MaterialButton btnDiscovery, btnStrategist;
    private final TextView txvSummary;

    private GameMode gameMode;

    public GameModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_game_mode, this);

        btnDiscovery = findViewById(R.id.btnDiscovery_vwGameMode);
        btnStrategist = findViewById(R.id.btnStrategist_vwGameMode);
        txvSummary = findViewById(R.id.txvSummary_vwGameMode);

        setGameMode(GameMode.Discovery);
    }

    public void setGameMode(@NonNull GameMode gameMode) {
        this.gameMode = gameMode;
        update();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    private void update() {
        Context context = getContext();
        boolean isDiscovery = gameMode == GameMode.Discovery;

        for (MaterialButton button : List.of(btnDiscovery, btnStrategist)) {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(context,
                    isDiscovery ^ button == btnStrategist ? R.color.selected_golden : R.color.darker_background
            ));
        }

        txvSummary.setText(isDiscovery ? R.string.discovery_mode_summary : R.string.strategist_mode_summary);
    }
}
