package com.leaders.app.views.credits;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.leaders.R;

public class ContributorView extends LinearLayout {
    public ContributorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_contributor, this);
    }
}
