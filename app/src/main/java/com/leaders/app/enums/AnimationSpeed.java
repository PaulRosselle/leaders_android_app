package com.leaders.app.enums;

import com.leaders.R;

public enum AnimationSpeed {
    noAnimation,
    VerySlow,
    Slow,
    Normal,
    Fast,
    VeryFast;

    public float getMultiplier() {
        switch (this) {
            case VerySlow: return 0.25f;
            case Slow: return 0.5f;
            case Normal: return 1f;
            case Fast: return 1.5f;
            case VeryFast: return 2f;
            default: throw new IllegalStateException("No speed multiplier associed with: " + this);
        }
    }

    public int getNameResId() {
        switch (this) {
            case noAnimation: return R.string.no_animation_speed;
            case VerySlow: return R.string.very_slow_speed;
            case Slow: return R.string.slow_speed;
            case Normal: return R.string.normal_speed;
            case Fast: return R.string.fast_speed;
            case VeryFast: return R.string.very_fast_speed;
            default: throw new IllegalStateException("No name found matching animation speed: " + this);
        }
    }
}
