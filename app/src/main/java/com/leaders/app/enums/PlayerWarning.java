package com.leaders.app.enums;

import androidx.annotation.DrawableRes;

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
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
