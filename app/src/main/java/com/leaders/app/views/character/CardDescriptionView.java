package com.leaders.app.views.character;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

public class CardDescriptionView extends ConstraintLayout {
    private PortraitView ptvPortrait;
    private TextView txvTitle, txvInfo;

    private CharacterCard card;

    public CardDescriptionView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CardDescriptionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardDescriptionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_card_description, this);

        ptvPortrait = findViewById(R.id.ptvPortrait_vwCardDescription);
        txvTitle = findViewById(R.id.txvTitle_vwCardDescription);
        txvInfo = findViewById(R.id.txvInfo_vwCardDescription);

        card = null;
    }

    public void setCard(@Nullable CharacterCard card, @NonNull CharacterSkins characterSkins) {
        this.card = card;
        if (card == null) {
            return;
        }

        ptvPortrait.setPortraitCard(card);
        ptvPortrait.setPortraitSkin(characterSkins.getCharacterSkin(card));
        txvTitle.setText(CharacterCardUtils.getFormattedNameId(card));
        txvInfo.setText(CharacterCardUtils.getDescriptionId(card));
    }

    @Nullable
    public CharacterCard getCard() {
        return card;
    }
}
