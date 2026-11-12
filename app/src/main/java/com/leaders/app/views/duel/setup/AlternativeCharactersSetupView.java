package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class AlternativeCharactersSetupView  extends ConstraintLayout {
    public AlternativeCharactersSetupView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AlternativeCharactersSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_expansion_characters_setup, this);
    }
}
