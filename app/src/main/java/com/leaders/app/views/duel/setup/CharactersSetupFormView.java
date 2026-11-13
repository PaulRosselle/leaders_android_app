package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.CharactersSetup;
import com.leaders.app.enums.CharacterExpansion;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CharactersSetupFormView extends ConstraintLayout implements ExpansionCharactersSetupView.Listener {
    private CharactersSetup setup;
    private final List<ExpansionCharactersSetupView> expansionSetupViews = new ArrayList<>();

    public CharactersSetupFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //region INITIALIZATION

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_characters_setup_form, this);

        initExpansions(context);
    }

    private void initExpansions(@NonNull Context context) {
        LinearLayout llyExpansions = findViewById(R.id.llyExpansions_vwCharactersSetupForm);

        for (CharacterExpansion expansion : CharacterExpansion.values()) {
            ExpansionCharactersSetupView expansionSetupView = new ExpansionCharactersSetupView(context);

            expansionSetupView.setExpansion(expansion);
            expansionSetupView.setListener(this);

            expansionSetupViews.add(expansionSetupView);
            llyExpansions.addView(expansionSetupView, getExpansionLayoutParams());
        }
    }

    @NonNull
    private LinearLayout.LayoutParams getExpansionLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;

        return layoutParams;
    }

    //endregion

    public void applySetup(@NonNull CharactersSetup setup) {
        this.setup = setup;

        for (ExpansionCharactersSetupView expansionSetupView : expansionSetupViews) {
            expansionSetupView.update(
                    setup.getCharactersInExpansion(expansionSetupView.getExpansion()),
                    setup.getBannedCharacters()
            );
        }

        // TODO - apply altSelection
    }

    //region EXPANSION METHODS

    @Override
    public void onPortraitClick(@NonNull CharacterCard card) {
        if (setup.removeBannedCard(card)) {
            setup.addBannedCard(card);
        }
    }

    @Override
    public void onExpansionClick(@NonNull CharacterExpansion expansion) {
        List<CharacterCard> expansionCards = setup.getCharactersInExpansion(expansion);
        Set<CharacterCard> bannedCards = setup.getBannedCharacters();

        List<CharacterCard> bannedExpansionCards = expansionCards.stream()
                .filter(bannedCards::contains)
                .collect(Collectors.toList());

        if (bannedExpansionCards.isEmpty()) {
            for (CharacterCard card : expansionCards) {
                setup.addBannedCard(card);
            }
        } else {
            for (CharacterCard card : bannedExpansionCards) {
                setup.removeBannedCard(card);
            }
        }
    }

    //endregion
}
