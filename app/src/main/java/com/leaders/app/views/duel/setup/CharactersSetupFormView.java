package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.CharactersSetup;
import com.leaders.app.enums.CharacterExpansion;
import com.leaders.app.utilities.DuelStartUtils;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CharactersSetupFormView extends ConstraintLayout implements ExpansionCharactersSetupView.Listener {
    public interface Listener {
        void onSaveCharactersSetup(@NonNull CharactersSetup setup);
        void onCancelCharactersSetupChange();
    }


    private TextView txvSelected;
    private TextView txvSave;
    private final List<ExpansionCharactersSetupView> expansionSetupViews = new ArrayList<>();
    private AltCharactersSetupView altCharactersSetupView;

    private CharactersSetup setup = null;

    private Listener listener;
    private OnLongClickListener onPortraitLongClick;

    public CharactersSetupFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //region INITIALIZATION

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_characters_setup_form, this);

        txvSelected = findViewById(R.id.txvSelected_vwCharactersSetupForm);
        txvSave = findViewById(R.id.txvSave_vwCharactersSetupForm);

        findViewById(R.id.txvSave_vwCharactersSetupForm).setOnClickListener(this::onSaveClick);
        findViewById(R.id.txvCancel_vwCharactersSetupForm).setOnClickListener(this::onCancelClick);

        LinearLayout llyCharacters = findViewById(R.id.llyCharacters_vwCharactersSetupForm);
        initExpansions(context, llyCharacters);
        initAltSelections(context, llyCharacters);
    }

    private void initExpansions(@NonNull Context context, @NonNull LinearLayout llyExpansions) {
        for (CharacterExpansion expansion : CharacterExpansion.values()) {
            ExpansionCharactersSetupView expansionSetupView = new ExpansionCharactersSetupView(context);

            expansionSetupView.setExpansion(expansion);
            expansionSetupView.setListener(this);

            expansionSetupViews.add(expansionSetupView);
            llyExpansions.addView(expansionSetupView, getCharactersLayoutParams(8));
        }
    }

    private void initAltSelections(@NonNull Context context, @NonNull LinearLayout llyCharacters) {
        altCharactersSetupView = new AltCharactersSetupView(context);
        altCharactersSetupView.setSelectionListener(this::onCheckedChange);
        llyCharacters.addView(altCharactersSetupView, getCharactersLayoutParams(0));
    }

    @NonNull
    private LinearLayout.LayoutParams getCharactersLayoutParams(int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        layoutParams.bottomMargin = (int) (bottomMargin * getResources().getDisplayMetrics().density);

        return layoutParams;
    }

    //endregion

    public void applySetup(@NonNull CharactersSetup setup) {
        this.setup = new CharactersSetup(setup);

        updateAltSelections();
        updateExpansions();
    }

    public void updateSaveState() {
        int remainingCharactersCount = setup.getRemainingCharacters().size();

        boolean canSave = remainingCharactersCount >= DuelStartUtils.CHARACTERS_START_COUNT;
        int bansCount = remainingCharactersCount - DuelStartUtils.CHARACTERS_START_COUNT;

        Context context = getContext();
        String textPrefix = String.format(
                context.getString(R.string.x_selected_characters),
                remainingCharactersCount
        );
        String selectedText = DuelStartUtils.getCharactersInfo(getContext(), bansCount);

        txvSelected.setText(String.format(
                selectedText.isEmpty() ? "%s." : "%s. %s.",
                textPrefix, selectedText
        ));

        txvSave.setClickable(canSave);
        txvSave.setTextColor(getContext().getColor(canSave ? R.color.app_golden : R.color.selected_golden));
    }

    //region EXPANSION METHODS

    private void updateExpansions() {
        if (setup == null) {
            return;
        }

        for (ExpansionCharactersSetupView expansionSetupView : expansionSetupViews) {
            expansionSetupView.update(
                    setup.getCharactersInExpansion(expansionSetupView.getExpansion()),
                    setup.getBannedCharacters()
            );
        }

        updateSaveState();
    }

    @Override
    public void onPortraitClick(@NonNull CharacterCard card) {
        if (setup == null) {
            return;
        }

        if (!setup.removeBannedCard(card)) {
            setup.addBannedCard(card);
        }
        updateExpansions();
    }

    @Override
    public void onExpansionClick(@NonNull CharacterExpansion expansion) {
        if (setup == null) {
            return;
        }

        List<CharacterCard> expansionCards = setup.getCharactersInExpansion(expansion);
        Set<CharacterCard> bannedCards = setup.getBannedCharacters();

        List<CharacterCard> bannedExpansionCards = expansionCards.stream()
                .filter(bannedCards::contains)
                .collect(Collectors.toList());

        if (bannedExpansionCards.size() < expansionCards.size()) {
            for (CharacterCard card : expansionCards) {
                setup.addBannedCard(card);
            }
        } else {
            for (CharacterCard card : bannedExpansionCards) {
                setup.removeBannedCard(card);
            }
        }
        updateExpansions();
    }

    @Override
    public boolean onPortraitLongClick(View v) {
        return onPortraitLongClick != null && onPortraitLongClick.onLongClick(v);
    }

    //endregion

    //region ALTERNATIVE CHARACTERS

    private void updateAltSelections() {
        altCharactersSetupView.applySetup(setup);
    }

    private void onCheckedChange(@NonNull CharacterCard altCard,
                                 boolean useAltCard) {
        if (useAltCard) {
            setup.selectAlt(altCard);
        } else {
            setup.unselectAlt(altCard);
        }
        updateExpansions();
    }

    //endregion


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void onSaveClick(View v) {
        if (listener != null) {
            listener.onSaveCharactersSetup(setup);
        }
    }

    private void onCancelClick(View v) {
        if (listener != null) {
            listener.onCancelCharactersSetupChange();
        }
    }

    public void setOnPortraitLongClick(OnLongClickListener onPortraitLongClick) {
        this.onPortraitLongClick = onPortraitLongClick;
        altCharactersSetupView.setOnPortraitClickListener(onPortraitLongClick);
    }
}
