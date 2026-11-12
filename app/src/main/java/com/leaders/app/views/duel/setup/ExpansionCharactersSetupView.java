package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.enums.CharacterExpansion;

public class ExpansionCharactersSetupView extends ConstraintLayout {
    private ImageView imvIcon;
    private TextView txvTitle;
    private CharacterExpansion expansion;

    public ExpansionCharactersSetupView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ExpansionCharactersSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_expansion_characters_setup, this);

        imvIcon = findViewById(R.id.imvIcon_vwExpansionCharactersSetup);
        txvTitle = findViewById(R.id.txvTitle_vwExpansionCharactersSetup);
    }

    public void setExpansion(@NonNull CharacterExpansion expansion) {
        this.expansion = expansion;
        updateExpansion();
    }

    public CharacterExpansion getExpansion() {
        return expansion;
    }

    private void updateExpansion() {
        // ICON
        imvIcon.setImageResource(expansion.getIcon());
        imvIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), expansion.getColor()));
        // TITLE
        // TODO - txvTitle -> Base game / Vermilion expansion
        // CHARACTERS
        // TODO - PortraitGroupView -> characters of the expansion (take alternative characters into account)
    }

    // TODO - whole expansion click behavior

    // TODO - characters click behavior
}
