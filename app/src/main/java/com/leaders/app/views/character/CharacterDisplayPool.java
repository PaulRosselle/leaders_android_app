package com.leaders.app.views.character;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class CharacterDisplayPool {
    private static final int CHARACTERS_POOL_SIZE = 36;

    @NonNull
    private final List<CharacterDisplay> available;

    public CharacterDisplayPool(@NonNull Context context, @NonNull ViewGroup parentView) {
        available = new ArrayList<>();
        for (int i = 0; i < CHARACTERS_POOL_SIZE; i++) {
            available.add(new CharacterDisplay(context, parentView));
        }
    }

    public void setDisplaysSize(int size) {
        for (CharacterDisplay characterDisplay : available) {
            characterDisplay.setSize(size);
        }
    }

    @NonNull
    public CharacterDisplay acquire() {
        if (available.isEmpty()) {
            throw new IllegalStateException("Character acquirements should never exceed the pool size");
        }
        CharacterDisplay acquiredDisplay = available.remove(available.size() - 1);
        acquiredDisplay.reset();
        return acquiredDisplay;
    }

    public void release(@NonNull CharacterDisplay display) {
        display.setVisibility(View.GONE);
        available.add(display);
    }
}
