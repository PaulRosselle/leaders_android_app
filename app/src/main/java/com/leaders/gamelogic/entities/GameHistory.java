package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.historyentries.IHistoryEntry;

import java.util.ArrayList;

public class GameHistory {
    @NonNull
    private final GameConfig config;
    @NonNull
    private final ArrayList<IHistoryEntry> entries;

    public GameHistory(@NonNull GameConfig config, @NonNull ArrayList<IHistoryEntry> entries) {
        this.config = config;
        this.entries = entries;
    }

    @NonNull
    public GameConfig getConfig() {
        return config;
    }

    @NonNull
    public ArrayList<IHistoryEntry> getEntries() {
        return entries;
    }
}
