package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class AltCharacterSelectionView extends ConstraintLayout {
    public AltCharacterSelectionView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AltCharacterSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_alt_character_selection, this);
    }
}
