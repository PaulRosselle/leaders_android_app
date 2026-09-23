package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public class CharacterSkinView extends ConstraintLayout {
    private final MaterialButton btnOpenForm;

    public CharacterSkinView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_skin, this);

        setBackgroundResource(R.drawable.round_rect);

        btnOpenForm = findViewById(R.id.btnOpenForm_vwCharacterSkin);
    }

    public void setOnOpenFormClickListener(View.OnClickListener onOpenFormClickListener) {
        btnOpenForm.setOnClickListener(onOpenFormClickListener);
    }
}
