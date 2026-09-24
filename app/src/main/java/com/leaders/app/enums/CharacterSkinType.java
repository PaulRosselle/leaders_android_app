package com.leaders.app.enums;

import com.leaders.R;

public enum CharacterSkinType {
    Default,
    LaughingMoonSage;

    public int getNameId() {
        switch (this) {
            case Default: return R.string.skin_default;
            case LaughingMoonSage: return R.string.skin_laughing_moon_sage;
            default: throw new IllegalStateException("No name found for skin: " + this);
        }
    }
}
