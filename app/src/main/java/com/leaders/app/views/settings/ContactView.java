package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public class ContactView extends ConstraintLayout {
    private final MaterialButton btnOpenForm;

    public ContactView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_contact, this);

        setBackgroundResource(R.drawable.round_rect);

        btnOpenForm = findViewById(R.id.btnOpenForm_vwContact);
    }

    public void setOnOpenFormClickListener(OnClickListener onOpenFormClickListener) {
        btnOpenForm.setOnClickListener(onOpenFormClickListener);
    }
}
