package com.leaders.app.enums;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.leaders.R;

public enum PlayerWarning {
    BARRAGE,
    SNIPER,
    SNIPER_AND_BARRAGE;

    @DrawableRes
    public int getBottomResId() {
        switch (this) {
            case BARRAGE: return R.drawable.player_bottom_warning_barrage;
            case SNIPER: return R.drawable.player_bottom_warning_sniper;
            case SNIPER_AND_BARRAGE: return R.drawable.player_bottom_warning_sniper_and_barrage;
            default: throw new IllegalStateException("No bottom resource id found for player warning: " + this);
        }
    }

    @StringRes
    public int getTitleResId() {
        switch (this) {
            case BARRAGE: return R.string.barrage_warning_title;
            case SNIPER: return R.string.sniper_warning_title;
            case SNIPER_AND_BARRAGE: return R.string.barrage_and_sniper_warning_title;
            default: throw new IllegalStateException("No title found for player warning: " + this);
        }
    }

    @StringRes
    public int getMessageResId() {
        switch (this) {
            case BARRAGE: return R.string.barrage_warning_message;
            case SNIPER: return R.string.sniper_warning_message;
            case SNIPER_AND_BARRAGE: return R.string.barrage_and_sniper_warning_message;
            default: throw new IllegalStateException("No title found for player warning: " + this);
        }
    }
}
