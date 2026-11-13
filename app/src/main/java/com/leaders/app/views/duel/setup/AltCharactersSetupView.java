package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.AltCharacterSelection;
import com.leaders.app.entities.CharactersSetup;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;

public class AltCharactersSetupView extends ConstraintLayout {
    List<AltCharacterSelectionView> altSelectionViews = new ArrayList<>();

    public AltCharactersSetupView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AltCharactersSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_alt_characters_setup, this);


        initAltSelections();
    }

    private void initAltSelections() {
        Context context = getContext();

        LinearLayout llyAltSelections = findViewById(R.id.llyAltSelections_vwAltCharactersSetup);
        for (CharacterCard card : CharacterCard.values()) {
            CharacterCard baseCard = CharacterCardUtils.getBaseCharacter(card);
            if (baseCard == null) {
                continue;
            }

            AltCharacterSelectionView altSelectionView = new AltCharacterSelectionView(context);
            altSelectionView.setAltSelection(baseCard, card, false);
            altSelectionViews.add(altSelectionView);
            llyAltSelections.addView(altSelectionView, getSelectionLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getSelectionLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.weight = 1;

        return layoutParams;
    }

    public void applySetup(@NonNull CharactersSetup setup) {
        for (AltCharacterSelectionView altSelectionView : altSelectionViews) {
            AltCharacterSelection altSelection = setup.getNonNullAltSelection(altSelectionView.getAltCharacter());
            altSelectionView.setChecked(altSelection.getSelectedAlternative() != null);
        }
    }

    void setSelectionListener(AltCharacterSelectionView.Listener selectionListener) {
        for (AltCharacterSelectionView altSelectionView : altSelectionViews) {
            altSelectionView.setListener(selectionListener);
        }
    }
}
