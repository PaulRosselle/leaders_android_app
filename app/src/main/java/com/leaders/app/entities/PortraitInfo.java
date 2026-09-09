package com.leaders.app.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.interactions.InteractionTarget;

public class PortraitInfo {
    @NonNull
    private CharacterCard card;
    private boolean isBanned;
    @NonNull
    private PortraitDisplayMode displayMode;
    @Nullable
    private InteractionTarget target;

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned,
                        @NonNull PortraitDisplayMode displayMode,
                        @Nullable InteractionTarget target) {
        this.card = card;
        this.isBanned = isBanned;
        this.displayMode = displayMode;
        this.target = target;
    }

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned,
                        @NonNull PortraitDisplayMode displayMode) {
        this(card, isBanned, displayMode, null);
    }

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned) {
        this(card, isBanned, PortraitDisplayMode.Default, null);
    }

    public PortraitInfo(@NonNull CharacterCard card) {
        this(card, false, PortraitDisplayMode.Default, null);
    }

    @NonNull
    public CharacterCard getCard() {
        return card;
    }

    public void setCard(@NonNull CharacterCard card) {
        this.card = card;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    @NonNull
    public PortraitDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(@NonNull PortraitDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Nullable
    public InteractionTarget getTarget() {
        return target;
    }

    public void setTarget(@Nullable InteractionTarget target) {
        this.target = target;
    }
}
