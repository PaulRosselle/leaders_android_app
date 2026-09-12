package com.leaders.app.enums;

import com.leaders.R;

public enum AnimationSpeed {
    VerySlow,
    Slow,
    Normal,
    Fast,
    VeryFast;

    public float getMultiplier() {
        switch (this) {
            case VerySlow: return 1.75f;
            case Slow: return 1.25f;
            case Normal: return 1f;
            case Fast: return 0.75f;
            case VeryFast: return 0.33f;
            default: throw new IllegalStateException("No speed multiplier associed with: " + this);
        }
    }

    public int getNameResId() {
        switch (this) {
            case VerySlow: return R.string.very_slow_speed;
            case Slow: return R.string.slow_speed;
            case Normal: return R.string.normal_speed;
            case Fast: return R.string.fast_speed;
            case VeryFast: return R.string.very_fast_speed;
            default: throw new IllegalStateException("No name found matching animation speed: " + this);
        }
    }
}
