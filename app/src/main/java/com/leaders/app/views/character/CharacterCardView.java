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
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.gamelogic.enums.CharacterCard;

public class CharacterCardView extends ConstraintLayout {
    private final ImageView imvCard;
    private final ImageView imvHighlight;
    private final TextView txvCardName;

    @NonNull
    private CharacterCard card;
    @NonNull
    private CharacterSkinType skinType;

    public CharacterCardView(@NonNull Context context) {
        this(context, null);
    }

    public CharacterCardView(@NonNull Context context,
                             @NonNull CharacterCard card,
                             @NonNull CharacterSkinType skinType,
                             boolean highlighted) {
        this(context, null);

        setCard(card, skinType);
        setHighlighted(highlighted);
    }

    public CharacterCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

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
        txvCardName.setText(CharacterCardUtils.getFormattedNameId(card));
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