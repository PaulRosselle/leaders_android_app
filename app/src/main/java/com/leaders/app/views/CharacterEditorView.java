package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Guideline;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.board.CharacterHighlightView;
import com.leaders.app.views.board.CharacterView;
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

public class CharacterEditorView extends ConstraintLayout {
    private static final int PORTRAITS_PER_GROUP = 6;

    private LinearLayout llyPortraits;

    private Group grpAddCharacters;
    private LinearLayout llyNewCharacters;
    private CharacterHighlightView chvNewCharacterHighlight;

    private Group grpEditCharacter;
    private CharacterView crvSwitchColor;
    private MaterialButton btnRemove;

    private Guideline gdlLeft, gdlRight;



    public CharacterEditorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_editor, this);

        initViews();
    }

    private void initViews() {
        llyPortraits = findViewById(R.id.llyPortraits_vwCharacterEditor);
        initPortraits();

        grpAddCharacters = findViewById(R.id.grpAddCharacters_vwCharacterEditor);
        llyNewCharacters = findViewById(R.id.llyNewCharacters_vwCharacterEditor);
        chvNewCharacterHighlight = findViewById(R.id.chvNewCharacterHighlight_vwCharacterEditor);
        chvNewCharacterHighlight.startAnimation();

        grpEditCharacter = findViewById(R.id.grpEditCharacter_vwCharacterEditor);
        crvSwitchColor = findViewById(R.id.crvSwitchColor_vwCharacterEditor);
        btnRemove = findViewById(R.id.btnRemove_vwCharacterEditor);

        gdlLeft = findViewById(R.id.gdlLeft_vwCharacterEditor);
        gdlRight = findViewById(R.id.gdlRight_vwCharacterEditor);
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

    public void startSelectCardMode() {
        setViewVisible(llyPortraits, true);
        setViewVisible(grpAddCharacters, false);
        setViewVisible(grpEditCharacter, false);
    }

    public void startAddCharactersMode(@NonNull List<Character> characters,
                                       int characterDisplaySize,
                                       @NonNull OnClickListener onCharacterClickListener) {
        setViewVisible(llyPortraits, false);
        setViewVisible(grpAddCharacters, true);
        setViewVisible(grpEditCharacter, false);

        llyNewCharacters.removeAllViews();

        Context context = getContext();
        for (Character character : characters) {
            CharacterView characterView = new CharacterView(context);
            characterView.setOnClickListener(onCharacterClickListener);
            characterView.setCharacter(character);
            // We use playable character target to link a character view with a new character
            characterView.setAsPlayableTarget(new InteractionTarget(
                    TargetCategory.PlayableCharacter,
                    new PlayableCharacter(character, new Position(0, 0), false, false)
            ));
            llyNewCharacters.addView(characterView, getCharacterLayoutParam(characterDisplaySize));
        }

        adjustGuidelinesPos();
        highlighFirstCharacterView();
    }

    public void startEditCharacterMode(@NonNull Character character) {
        setViewVisible(llyPortraits, false);
        setViewVisible(grpAddCharacters, false);
        setViewVisible(grpEditCharacter, true);

        crvSwitchColor.setCharacter(character);

        adjustGuidelinesPos();
    }

    private void setViewVisible(View v, boolean visible) {
        if (visible) {
            v.setVisibility(VISIBLE);
        } else {
            v.setVisibility(GONE);
        }
    }
    private void adjustGuidelinesPos() {
        if (llyNewCharacters.getVisibility() == VISIBLE && llyNewCharacters.getChildCount() >= 3) {
            gdlLeft.setGuidelinePercent(0.1f);
            gdlRight.setGuidelinePercent(0.9f);
        } else {
            gdlLeft.setGuidelinePercent(0.3f);
            gdlRight.setGuidelinePercent(0.7f);
        }
    }

    public void removeNewCharactersMatching(@Nullable TeamColor teamColor,
                                            @Nullable CharacterType characterType) {
        ArrayList<CharacterView> childrenToRemove = new ArrayList<>();

        for (int i = 0; i < llyNewCharacters.getChildCount(); i++) {
            CharacterView characterView = (CharacterView) llyNewCharacters.getChildAt(i);
            InteractionTarget target = Objects.requireNonNull(characterView.getTarget(),
                    "Target within new character list missing");
            Character character = Objects.requireNonNull(target.getChosenCharacterPlayableState(),
                    "Invalid new character target : character missing").getCharacter();

            if ((teamColor == null || character.getTeamColor() == teamColor) &&
                    (characterType == null) || character.getCharacterType() == characterType) {
                childrenToRemove.add(characterView);
            }
        }

        for (CharacterView childToRemove : childrenToRemove) {
            llyNewCharacters.removeView(childToRemove);
        }

        adjustGuidelinesPos();
    }

    private void highlighFirstCharacterView() {
        if (llyNewCharacters.getChildCount() > 0) {
            highlightCharacterView((CharacterView) llyNewCharacters.getChildAt(0));
        }
    }

    public void highlightCharacterView(@NonNull CharacterView characterView) {
        int[] characterLocation = new int[2];
        int[] editorLocation = new int[2];

        characterView.getLocationOnScreen(characterLocation);
        getLocationOnScreen(editorLocation);

        chvNewCharacterHighlight.setX(characterLocation[0] - editorLocation[0]);
        chvNewCharacterHighlight.setY(characterLocation[1] - editorLocation[1]);
        chvNewCharacterHighlight.getLayoutParams().width = characterView.getWidth();
        chvNewCharacterHighlight.getLayoutParams().height = characterView.getHeight();

        characterView.scaleForHighlight(true, false);

        for (int i = 0; i < llyNewCharacters.getChildCount(); i++) {
            CharacterView childCharacterView = (CharacterView) llyNewCharacters.getChildAt(i);
            if (childCharacterView != characterView) {
                childCharacterView.scaleForHighlight(false, false);
            }
        }
    }

    public int getNewCharactersCount() {
        return llyNewCharacters.getChildCount();
    }

    private LinearLayout.LayoutParams getCharacterLayoutParam(int characterSize) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(characterSize, characterSize);
        layoutParams.weight = 1;
        return layoutParams;
    }

    public void setOnCardPortraitClick(@Nullable OnClickListener onClickListener) {
        for (int i = 0; i < llyPortraits.getChildCount(); i++) {
            ((CharacterCardPortraitGroupView) llyPortraits.getChildAt(i)).setPortraitsClickListener(onClickListener);
        }
    }

    public void setOnSwitchColorClick(@Nullable OnClickListener onClickListener) {
        crvSwitchColor.setOnClickListener(onClickListener);
    }

    public void setOnRemoveClick(@Nullable OnClickListener onClickListener) {
        btnRemove.setOnClickListener(onClickListener);
    }

    public CharacterHighlightView getNewCharacterHighlight() {
        return chvNewCharacterHighlight;
    }
}
