package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.gamelogic.enums.CharacterCard;

public final class CharacterCardPortraitView extends AppCompatImageView {
    @NonNull
    private CharacterCard portraitCard;

    // PortraitType incoming in a future ticket (Default, Banned, Hexagonal) but not currently needed

    public CharacterCardPortraitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Nemesis is the default portrait (because I really like her design)
        setPortraitCard(CharacterCard.Nemesis);
        setAdjustViewBounds(true);
    }

    public void setPortraitCard(@NonNull CharacterCard portraitCard) {
        this.portraitCard = portraitCard;
        setImageResource(getPortraitDrawableId());
    }

    private int getPortraitDrawableId() {
        switch (portraitCard) {
            case Acrobat: return R.drawable.card_portrait_acrobat;
            case Archer: return R.drawable.card_portrait_archer;
            case Assassin: return R.drawable.card_portrait_assassin;
            case Brewmaster: return R.drawable.card_portrait_brewmaster;
            case Bruiser: return R.drawable.card_portrait_bruiser;
            case ClawLauncher: return R.drawable.card_portrait_claw_launcher;
            case HermitAndCub: return R.drawable.card_portrait_hermit_and_cub;
            case Illusionist: return R.drawable.card_portrait_illusionist;
            case Jailer: return R.drawable.card_portrait_jailer;
            case LeaderKing: return R.drawable.card_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_portrait_leader_queen;
            case Manipulator: return R.drawable.card_portrait_manipulator;
            case Nemesis: return R.drawable.card_portrait_nemesis;
            case Protector: return R.drawable.card_portrait_protector;
            case Rider: return R.drawable.card_portrait_rider;
            case RoyalGuard: return R.drawable.card_portrait_royal_guard;
            case Vizier: return R.drawable.card_portrait_vizier;
            case Wanderer: return R.drawable.card_portrait_wanderer;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + portraitCard);
        }
    }

    @NonNull
    public CharacterCard getPortraitCard() {
        return portraitCard;
    }
}
