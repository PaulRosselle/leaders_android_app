package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.interactions.InteractionTarget;

public interface NemesisBlockerCallback {
    void onBlockingActionFound(@NonNull InteractionTarget target);

    void onSearchCompletedWithoutResult(@NonNull InteractionTarget defaultTarget);

    void onSearchCancelled();
}
