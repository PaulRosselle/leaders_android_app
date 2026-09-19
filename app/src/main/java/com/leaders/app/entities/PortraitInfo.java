package com.leaders.app.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionTarget;

public final class PortraitInfo {
    @NonNull
    private CharacterCard card;
    @Nullable
    private TeamColor teamColor;
    private boolean isBanned;
    @NonNull
    private PortraitDisplayMode displayMode;
    @Nullable
    private InteractionTarget target;

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned,
                        @NonNull PortraitDisplayMode displayMode,
                        @Nullable TeamColor teamColor,
                        @Nullable InteractionTarget target) {
        this.card = card;
        this.isBanned = isBanned;
        this.teamColor = teamColor;
        this.displayMode = displayMode;
        this.target = target;
    }

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned,
                        @NonNull PortraitDisplayMode displayMode) {
        this(card, isBanned, displayMode, null, null);
    }

    public PortraitInfo(@NonNull CharacterCard card, boolean isBanned, @Nullable TeamColor teamColor) {
        this(card, isBanned, PortraitDisplayMode.Default, teamColor, null);
    }

    public PortraitInfo(@NonNull CharacterCard card) {
        this(card, false, PortraitDisplayMode.Default, null, null);
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

    @Nullable
    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(@Nullable TeamColor teamColor) {
        this.teamColor = teamColor;
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
