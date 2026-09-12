package com.leaders.app.enums;

import com.leaders.R;

public enum HighlightColor {

    Default,
    Blue,
    Red,
    Green,
    Neon;

    public int getOutlineResId() {
        switch (this) {
            case Blue: return R.drawable.highlight_outline_blue;
            case Red: return R.drawable.highlight_outline_red;
            case Green: return R.drawable.highlight_outline_green;
            case Neon: return R.drawable.highlight_outline_neon;
            default: return R.drawable.highlight_outline_default;
        }
    }

    public int getColorResId() {
        switch (this) {
            case Blue: return R.drawable.highlight_color_blue;
            case Red: return R.drawable.highlight_color_red;
            case Green: return R.drawable.highlight_color_green;
            case Neon: return R.drawable.highlight_color_neon;
            default: return R.drawable.highlight_color_default;
        }
    }
}
