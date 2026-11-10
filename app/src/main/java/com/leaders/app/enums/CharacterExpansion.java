package com.leaders.app.enums;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.enums.CharacterCard;

public enum CharacterExpansion {
    BASE_GAME,
    VERMILION;

    @ColorRes
    public int getColor() {
        switch (this) {
            case BASE_GAME: return R.color.expansion_base_game;
            case VERMILION: return R.color.expansion_vermilion;
            default: throw new IllegalStateException("No color found matching expansion: " + this);
        }
    }

    @DrawableRes
    public int getIcon() {
        switch (this) {
            case BASE_GAME: return R.drawable.icon_expansion_base_game;
            case VERMILION: return R.drawable.icon_expansion_vermilion;
            default: throw new IllegalStateException("No icon found matching expansion: " + this);
        }
    }

    @NonNull
    public static CharacterExpansion getFromCard(@NonNull CharacterCard card) {
        // TODO - update once Vermilion expansion characters are added
        return BASE_GAME;
    }
}
