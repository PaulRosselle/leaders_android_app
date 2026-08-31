package com.leaders.app.views.character;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.Objects;

public final class CharacterCardPortraitView extends AppCompatImageView {
    public enum DisplayMode {
        Default,
        Hexagonal
    }

    @NonNull
    private CharacterCard portraitCard;
    @NonNull
    private DisplayMode displayMode;
    private boolean useBannedDisplay;

    @Nullable
    private InteractionTarget target;

    public CharacterCardPortraitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        target = null;
        useBannedDisplay = false;

        setAdjustViewBounds(true);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.CharacterCardPortraitView)) {
            int displayModeOrd = customAttrs.getInteger(R.styleable.CharacterCardPortraitView_displayMode, DisplayMode.Default.ordinal());
            displayMode = DisplayMode.values()[displayModeOrd];
        }

        portraitCard = CharacterCard.LeaderQueen;
        updateDisplay();
    }

    public void setDisplayMode(@NonNull DisplayMode displayMode) {
        this.displayMode = displayMode;
        updateDisplay();
    }

    public void setPortraitCard(@NonNull CharacterCard portraitCard) {
        this.portraitCard = portraitCard;
        updateDisplay();
    }

    private void updateDisplay() {
        int resId;
        switch (displayMode) {
            case Default: resId = getPortraitDrawableId(); break;
            case Hexagonal: resId = getPortraitHexagonalDrawableId(); break;
            default: throw new IllegalStateException("Unexpected display mode: " + displayMode);
        }
        setImageResource(resId);
    }

    private int getPortraitDrawableId() {
        if (useBannedDisplay) {
            return getPortraitBannedDrawableId();
        }

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

    private int getPortraitBannedDrawableId() {
        throw new IllegalArgumentException("No portrait banned drawable for card: " + portraitCard);
    }

    private int getPortraitHexagonalDrawableId() {
        switch (portraitCard) {
            case LeaderKing: return R.drawable.card_hex_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_hex_portrait_leader_queen;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + portraitCard);
        }
    }

    @NonNull
    public CharacterCard getPortraitCard() {
        return portraitCard;
    }

    @Nullable
    public InteractionTarget getTarget() {
        return target;
    }

    public void setTarget(@Nullable InteractionTarget target) {
        this.target = target;

        boolean isBannedTarget = target != null &&
                target.getCategory().getResultType() == InteractionResultType.SelectableCharacterCardChosen &&
                Objects.requireNonNull(target.getChosenSelectableCharacterCard(),
                        "Invalid portrait target: selectable character card missing"
                ).getSelectionStatus() == CharacterCardSelectionStatus.AlreadyBanned;

        setUseBannedDisplay(isBannedTarget);
    }

    public void setUseBannedDisplay(boolean useBannedDisplay) {
        this.useBannedDisplay = useBannedDisplay;
        updateDisplay();
    }
}
