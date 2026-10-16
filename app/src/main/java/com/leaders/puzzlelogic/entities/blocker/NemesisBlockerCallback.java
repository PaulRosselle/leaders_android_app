package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;

public interface NemesisBlockerCallback {
    void onBlockingActionFound(@NonNull CharacterAction action);

    void onSearchCompletedWithoutResult(@Nullable CharacterAction defaultAction);

    void onSearchCancelled();
}
