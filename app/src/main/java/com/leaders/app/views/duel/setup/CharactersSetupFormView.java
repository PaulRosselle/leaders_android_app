package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class CharactersSetupFormView extends ConstraintLayout {
    public CharactersSetupFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_characters_setup_form, this);
    }
}
