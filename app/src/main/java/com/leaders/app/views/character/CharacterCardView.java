package com.leaders.app.views.character;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.enums.CharacterCard;

public final class CharacterCardView extends ConstraintLayout {
    private ImageView imvCard;
    private ImageView imvHighlight;
    private TextView txvCardName;

    @NonNull
    private CharacterCard card;
    @NonNull
    private CharacterSkinType skinType;

    public CharacterCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CharacterCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_character_card, this);

        imvCard = findViewById(R.id.imvCard_vwCharacterCard);
        imvHighlight = findViewById(R.id.imvCardHighlight_vwCharacterCard);
        txvCardName = findViewById(R.id.txvCardName_vwCharacterCard);

        card = CharacterCard.RoyalGuard;
        skinType = CharacterSkinType.Default;
    }

    public void setCard(@NonNull CharacterCard card, @NonNull CharacterSkinType skinType) {
        this.card = card;
        this.skinType = skinType;

        imvCard.setImageResource(getCardResId(card, skinType));
        txvCardName.setText(skinType.getNameId());
    }

    public void setHighlighted(boolean highlighted) {
        imvHighlight.setVisibility(highlighted ? VISIBLE : GONE);
    }

    private int getCardResId(@NonNull CharacterCard card, @NonNull CharacterSkinType skinType) {
        switch (card) {
            case Illusionist: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_card_illusionist_lms;
                }
                return R.drawable.character_card_illusionist;
            }
            case RoyalGuard: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_card_royal_guard_lms;
                }
                return R.drawable.character_card_royal_guard;
            }
            case Vizier: {
                if (skinType == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.character_card_vizier_lms;
                }
                return R.drawable.character_card_vizier;
            }
            default: throw new IllegalStateException("No card resource found for card: " + card);
        }
    }

    @NonNull
    public CharacterCard getCard() {
        return card;
    }

    @NonNull
    public CharacterSkinType getSkinType() {
        return skinType;
    }
}