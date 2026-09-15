package com.leaders.app.views.credits;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.Contributor;
import com.leaders.app.views.character.CharacterView;

public class ContributorView extends LinearLayout {
    private final CharacterView chvCharacter;
    private final TextView txvName;
    private final TextView txvAlpha;
    private final TextView txvBeta;

    public ContributorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_contributor, this);

        chvCharacter = findViewById(R.id.chvCharacter_vwContributor);
        txvName = findViewById(R.id.txvName_vwContributor);
        txvAlpha = findViewById(R.id.txvAlpha_vwContributor);
        txvBeta = findViewById(R.id.txvBeta_vwContributor);
    }

    public ContributorView(@NonNull Context context, @NonNull Contributor contributor) {
        this(context, (AttributeSet) null);

        chvCharacter.setCharacter(contributor.getCharacterType(), contributor.getTeamColor());
        txvName.setText(contributor.getName());
        txvAlpha.setVisibility(contributor.hasContributedToAlpha() ? View.VISIBLE : View.INVISIBLE);
        txvBeta.setVisibility(contributor.hasContributedToBeta() ? View.VISIBLE : View.INVISIBLE);
    }
}
