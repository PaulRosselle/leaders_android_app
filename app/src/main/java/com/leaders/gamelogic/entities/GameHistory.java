package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

import java.util.ArrayList;

public final class GameHistory {
    @NonNull
    private final GameConfig config;
    @NonNull
    private final ArrayList<IHistoryEntry> entries;

    public GameHistory(@NonNull GameConfig config, @NonNull ArrayList<IHistoryEntry> entries) {
        this.config = config;
        this.entries = entries;
    }

    public GameHistory(@NonNull GameHistory refGameHistory) {
        // Since the game config is immutable, we can use the same reference safely
        this(refGameHistory.config, copyEntries(refGameHistory.entries));
    }

    private static ArrayList<IHistoryEntry> copyEntries(@NonNull ArrayList<IHistoryEntry> entries) {
        ArrayList<IHistoryEntry> entriesCopy = new ArrayList<>();
        for (IHistoryEntry entry : entries) {
            if (entry instanceof Turn) {
                entriesCopy.add(new Turn((Turn) entry));
            } else if (entry instanceof BanishmentPhase) {
                entriesCopy.add(new BanishmentPhase((BanishmentPhase) entry));
            } else {
                throw new IllegalArgumentException(
                        "Unsupported history entry: " + entry.getClass()
                );
            }
        }
        return entriesCopy;
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
