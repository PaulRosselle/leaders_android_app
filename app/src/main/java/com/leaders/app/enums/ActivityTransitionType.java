package com.leaders.app.enums;

import com.leaders.R;

import kotlin.NotImplementedError;

public enum ActivityTransitionType {
    Fade,
    SlideLeft,
    SlideRight;

    public int getEnterAnimation() {
        switch (this) {
            case Fade: return R.anim.fade_in;
            case SlideLeft: return R.anim.slide_from_right;
            case SlideRight: return R.anim.slide_from_left;
            default: throw new NotImplementedError("No open animation for transition: " + this);
        }
    }

    public int getExitAnimation() {
        switch (this) {
            case Fade: return R.anim.fade_none;
            case SlideLeft: return R.anim.slide_to_left;
            case SlideRight: return R.anim.slide_to_right;
            default: throw new NotImplementedError("No close animation for transition: " + this);
        }

    }
}
