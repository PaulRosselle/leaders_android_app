package com.leaders.puzzlelogic.utilities.blocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread-safe transposition table: state hash -> "current team can force a win". */
public final class SearchCache {
    private static final int DEFAULT_MAX_ENTRIES = 1_000_000;

    private final ConcurrentHashMap<String, Boolean> table = new ConcurrentHashMap<>();
    private final AtomicInteger size = new AtomicInteger();
    private final int maxEntries;

    public SearchCache() { this(DEFAULT_MAX_ENTRIES); }

    public SearchCache(int maxEntries) { this.maxEntries = maxEntries; }

    @Nullable
    Boolean get(@NonNull String hash) {
        return table.get(hash);
    }

    void put(@NonNull String hash, boolean playerWins) {
        // Bounded: once full, we simply stop memoizing (results stay correct).
        if (size.get() >= maxEntries) {
            return;
        }
        if (table.putIfAbsent(hash, playerWins) == null) {
            size.incrementAndGet();
        }
    }
}