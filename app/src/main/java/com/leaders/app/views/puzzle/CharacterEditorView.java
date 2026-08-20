package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.character.CharacterCardPortraitGroupView;
import com.leaders.app.views.character.CharacterHighlightView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CharacterEditorView extends ConstraintLayout {
    private enum EditorMode {
        SelectCardProtrait,
        AddCardCharacters,
        EditCharacter
    }


    private static final int PORTRAITS_PER_GROUP = 6;

    private LinearLayout llyPortraits;


    private CharacterView selectedNewCharacter;
    private List<CharacterView> newCharacterViews;
    private CharacterHighlightView newCharacterHighlight;

    private Group grpEditCharacter;
    private CharacterView crvSwitchColor;
    private MaterialButton btnRemove;



    public CharacterEditorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_editor, this);

        initViews();
    }

    private void initViews() {
        llyPortraits = findViewById(R.id.llyPortraits_vwCharacterEditor);
        initPortraits();

        selectedNewCharacter = null;
        newCharacterViews = new ArrayList<>();
        newCharacterHighlight = new CharacterHighlightView(getContext());
        addView(newCharacterHighlight, getCharacterHighlightLayoutParam());

        grpEditCharacter = findViewById(R.id.grpEditCharacter_vwCharacterEditor);
        crvSwitchColor = findViewById(R.id.crvSwitchColor_vwCharacterEditor);
        btnRemove = findViewById(R.id.btnRemove_vwCharacterEditor);
    }

    private void initPortraits() {
        // We create a portrait for each card
        List<CharacterCard> allCards = new ArrayList<>(Arrays.asList(CharacterCard.values()));
        Context context = getContext();
        CharacterCardUtils.sort(context, allCards);
        while (!allCards.isEmpty()) {
            // We add cards line per line within multiple "PortraitGroupView".
            // For each group, an array is alimented
            int portraitsInLineCount = Math.min(PORTRAITS_PER_GROUP, allCards.size());
            ArrayList<CharacterCard> portraitsCards = new ArrayList<>();
            for (int i = 0; i < portraitsInLineCount; i++) {
                portraitsCards.add(allCards.remove(0));
            }
            CharacterCardPortraitGroupView ptvPortraits =
                    new CharacterCardPortraitGroupView(context, portraitsCards, PORTRAITS_PER_GROUP);
            ptvPortraits.setClickable(false);
            ptvPortraits.setLongClickable(false);
            llyPortraits.addView(ptvPortraits, getPortraitsGroupLayoutParams());
        }
    }

    private LinearLayout.LayoutParams getPortraitsGroupLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
        layoutParams.weight = 1;
        return layoutParams;
    }

    private void updateMode(EditorMode editorMode) {
        setViewVisible(llyPortraits, editorMode == EditorMode.SelectCardProtrait);
        for (CharacterView characterView : newCharacterViews) {
            removeView(characterView);
        }
        newCharacterViews.clear();
        selectedNewCharacter = null;
        setViewVisible(newCharacterHighlight, editorMode == EditorMode.AddCardCharacters);
        setViewVisible(grpEditCharacter, editorMode == EditorMode.EditCharacter);
    }

    public void startSelectCardMode() {
        updateMode(EditorMode.SelectCardProtrait);
    }

    public void startAddCardCharactersMode(@NonNull List<Character> characters,
                                           int characterDisplaySize,
                                           @NonNull OnClickListener onNewCharacterClickListener,
                                           @NonNull OnLongClickListener onNewCharacterLongClickListener) {
        updateMode(EditorMode.AddCardCharacters);

        if (characters.isEmpty()) {
            throw new IllegalArgumentException("AddCardCharacters mode should not be started without characters to add");
        }

        Context context = getContext();
        int charactersCount = characters.size();
        float editorWidth = getWidth();
        float width = charactersCount > 2 ? editorWidth * 0.8f : editorWidth * 0.6f;
        float characterOffset = (editorWidth - width) / 2f;

        for (int i = 0; i < charactersCount; i++) {
            Character character = characters.get(i);
            CharacterView characterView = new CharacterView(context);

            characterView.setOnClickListener(onNewCharacterClickListener);
            characterView.setOnLongClickListener(onNewCharacterLongClickListener);
            characterView.setCharacter(character);

            // We use playable character target to link a character view with a new character
            characterView.setAsPlayableTarget(new InteractionTarget(
                    TargetCategory.PlayableCharacter,
                    new PlayableCharacter(character, new Position(0, 0), false, false)
            ));

            characterView.setX(characterOffset + ((i + 0.5f) * width / charactersCount) - (characterDisplaySize / 2f));

            newCharacterViews.add(characterView);
            addView(characterView, getCharacterLayoutParam(characterDisplaySize));
        }

        updateNewCharacterHighlightSize(characterDisplaySize);
        selectFirstNewCharacter();
    }

    public void startEditCharacterMode(@NonNull Character character) {
        updateMode(EditorMode.EditCharacter);

        crvSwitchColor.setCharacter(character);
    }

    private void setViewVisible(View v, boolean visible) {
        if (visible) {
            v.setVisibility(VISIBLE);
        } else {
            v.setVisibility(GONE);
        }
    }

    public void removeNewCharactersMatching(@Nullable TeamColor teamColor,
                                            @Nullable CharacterType characterType) {
        for (int i = newCharacterViews.size() - 1; i >= 0; i--) {
            CharacterView characterView = newCharacterViews.get(i);
            Character character = getCharacterFromView(characterView);

            if ((teamColor == null || character.getTeamColor() == teamColor) &&
                    (characterType == null || character.getCharacterType() == characterType)) {
                newCharacterViews.remove(i);
                removeView(characterView);
            }
        }

        // TODO - comment
        if (!newCharacterViews.isEmpty()) {
            selectFirstNewCharacter();
        }
    }

    private void selectFirstNewCharacter() {
        if (!newCharacterViews.isEmpty()) {
            selectNewCharacter(newCharacterViews.get(0));
        }
    }

    public void selectNewCharacter(@NonNull CharacterView characterView) {
        if (selectedNewCharacter == characterView) {
            return;
        }

        selectedNewCharacter = characterView;

        newCharacterHighlight.stopAnimation();
        newCharacterHighlight.bringToFront();

        for (CharacterView newCharacterView : newCharacterViews) {
            if (newCharacterView != selectedNewCharacter) {
                newCharacterView.scaleForHighlight(false, true);
            }
        }

        selectedNewCharacter.scaleForHighlight(true, true);

        newCharacterHighlight.setX(selectedNewCharacter.getX());

        newCharacterHighlight.startAnimation();
    }

    public int getNewCharactersCount() {
        return newCharacterViews.size();
    }

    private LayoutParams getCharacterLayoutParam(int characterSize) {
        LayoutParams params = new LayoutParams(characterSize, characterSize);

        params.verticalBias = 0.5f;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        return params;
    }


    private LayoutParams getCharacterHighlightLayoutParam() {
        ConstraintLayout.LayoutParams params = new LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        );

        params.verticalBias = 0.5f;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        return params;
    }

    private void updateNewCharacterHighlightSize(int characterSize) {
        LayoutParams params = (LayoutParams) newCharacterHighlight.getLayoutParams();

        params.height = characterSize;
        params.width = characterSize;

        newCharacterHighlight.setLayoutParams(params);
    }

    public void setOnCardPortraitClick(@Nullable OnClickListener onClickListener) {
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((CharacterCardPortraitGroupView) llyPortraits.getChildAt(i)).setPortraitsClickListener(onClickListener);
        }
    }


    public void setOnCardPortraitLongClick(@Nullable OnLongClickListener onLongClickListener) {
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((CharacterCardPortraitGroupView) llyPortraits.getChildAt(i)).setPortraitsLongClickListener(onLongClickListener);
        }
    }

    public void setOnSwitchColorClick(@Nullable OnClickListener onClickListener) {
        crvSwitchColor.setOnClickListener(onClickListener);
    }

    public void setOnRemoveClick(@Nullable OnClickListener onClickListener) {
        btnRemove.setOnClickListener(onClickListener);
    }

    private static Character getCharacterFromView(@NonNull CharacterView characterView) {
        InteractionTarget target = Objects.requireNonNull(characterView.getTarget(),
                "Target within new character list missing");
        return Objects.requireNonNull(target.getChosenCharacterPlayableState(),
                "Invalid new character target : character missing").getCharacter();
    }

    @Nullable
    public Character getSelectedNewCharacter() {
        if (selectedNewCharacter == null) {
            return null;
        }

        return getCharacterFromView(selectedNewCharacter);
    }
}
