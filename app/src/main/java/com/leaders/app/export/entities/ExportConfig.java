package com.leaders.app.export.entities;

import androidx.annotation.NonNull;

import com.leaders.app.export.actions.ExportAction;
import com.leaders.app.export.enums.ExportGameMode;

import java.util.List;

public class ExportConfig {
    @NonNull
    private final String title;
    @NonNull
    private final List<ExportPlayer> players;
    @NonNull
    private final ExportGameMode gameMode;
    @NonNull
    private final List<ExportAction> initialActions;

    public ExportConfig(@NonNull String title, @NonNull List<ExportPlayer> players, @NonNull ExportGameMode gameMode, @NonNull List<ExportAction> initialActions) {
        this.title = title;
        this.players = players;
        this.gameMode = gameMode;
        this.initialActions = initialActions;
    }
}
