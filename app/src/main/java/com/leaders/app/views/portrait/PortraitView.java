package com.leaders.app.views.portrait;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionTarget;

public final class PortraitView extends AppCompatImageView {
    @NonNull
    private PortraitInfo info;

    public PortraitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setAdjustViewBounds(true);

        PortraitDisplayMode displayMode;
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.PortraitView)) {
            int displayModeOrd = customAttrs.getInteger(
                    R.styleable.PortraitView_displayMode,
                    PortraitDisplayMode.Default.ordinal()
            );
            displayMode = PortraitDisplayMode.values()[displayModeOrd];
        }

        info = new PortraitInfo(CharacterCard.LeaderQueen, CharacterSkinType.Default, false, displayMode);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        updateDisplay();
    }

    public void setInfo(@NonNull PortraitInfo info) {
        this.info = info;
        updateDisplay();
    }

    public void setDisplayMode(@NonNull PortraitDisplayMode displayMode) {
        info.setDisplayMode(displayMode);
        updateDisplay();
    }

    public void setPortraitCard(@NonNull CharacterCard portraitCard) {
        info.setCard(portraitCard);
        updateDisplay();
    }

    public void setPortraitSkin(@NonNull CharacterSkinType portraitSkin) {
        info.setSkinType(portraitSkin);
        updateDisplay();
    }

    private void updateDisplay() {
        Drawable teamDrawable = getTeamDrawable();
        Drawable characterDrawable = getCharacterDrawable();

        if (teamDrawable != null) {
            setImageDrawable(new LayerDrawable(new Drawable[]{characterDrawable, teamDrawable}));
        } else {
            setImageDrawable(characterDrawable);
        }
    }

    @Nullable
    private Drawable getCharacterDrawable() {
        int characterResId;

        switch (info.getDisplayMode()) {
            case Default: characterResId = getPortraitDrawableId(); break;
            case Hexagonal: characterResId = getPortraitHexagonalDrawableId(); break;
            default: throw new IllegalStateException("Unexpected display mode: " + info.getDisplayMode());
        }

        return AppCompatResources.getDrawable(getContext(), characterResId);
    }

    @Nullable
    private Drawable getTeamDrawable() {
        if (!info.isBanned() ||
                info.getTeamColor() == null) {
            return null;
        }

        int foregroundResId;
        boolean isHex = info.getDisplayMode() == PortraitDisplayMode.Hexagonal;
        if (info.getTeamColor() == TeamColor.Black) {
            foregroundResId = isHex ? R.drawable.card_hex_portrait_team_b : R.drawable.card_portrait_team_b;
        } else {
            foregroundResId = isHex ? R.drawable.card_hex_portrait_team_w : R.drawable.card_portrait_team_w;
        }

        return AppCompatResources.getDrawable(getContext(), foregroundResId);
    }

    private int getPortraitDrawableId() {
        if (info.isBanned()) {
            return getPortraitBannedDrawableId();
        }

        switch (info.getCard()) {
            case Acrobat: return R.drawable.card_portrait_acrobat;
            case Archer: return R.drawable.card_portrait_archer;
            case Assassin: return R.drawable.card_portrait_assassin;
            case Brewmaster: return R.drawable.card_portrait_brewmaster;
            case Bruiser: return R.drawable.card_portrait_bruiser;
            case ClawLauncher: return R.drawable.card_portrait_claw_launcher;
            case HermitAndCub: return R.drawable.card_portrait_hermit_and_cub;
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_illusionist_lms;
                }
                return R.drawable.card_portrait_illusionist;
            }
            case Jailer: return R.drawable.card_portrait_jailer;
            case LeaderKing: return R.drawable.card_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_portrait_leader_queen;
            case Manipulator: return R.drawable.card_portrait_manipulator;
            case Nemesis: return R.drawable.card_portrait_nemesis;
            case Protector: return R.drawable.card_portrait_protector;
            case Rider: return R.drawable.card_portrait_rider;
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_royal_guard_lms;
                }
                return R.drawable.card_portrait_royal_guard;
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_vizier_lms;
                }
                return R.drawable.card_portrait_vizier;
            }
            case Wanderer: return R.drawable.card_portrait_wanderer;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + info.getCard());
        }
    }

    private int getPortraitBannedDrawableId() {
        switch (info.getCard()) {
            case Acrobat: return R.drawable.card_portrait_banned_acrobat;
            case Archer: return R.drawable.card_portrait_banned_archer;
            case Assassin: return R.drawable.card_portrait_banned_assassin;
            case Brewmaster: return R.drawable.card_portrait_banned_brewmaster;
            case Bruiser: return R.drawable.card_portrait_banned_bruiser;
            case ClawLauncher: return R.drawable.card_portrait_banned_claw_launcher;
            case HermitAndCub: return R.drawable.card_portrait_banned_hermit_and_cub;
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_illusionist_lms;
                }
                return R.drawable.card_portrait_banned_illusionist;
            }
            case Jailer: return R.drawable.card_portrait_banned_jailer;
            case Manipulator: return R.drawable.card_portrait_banned_manipulator;
            case Nemesis: return R.drawable.card_portrait_banned_nemesis;
            case Protector: return R.drawable.card_portrait_banned_protector;
            case Rider: return R.drawable.card_portrait_banned_rider;
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_royal_guard_lms;
                }
                return R.drawable.card_portrait_banned_royal_guard;
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_portrait_banned_vizier_lms;
                }
                return R.drawable.card_portrait_banned_vizier;
            }
            case Wanderer: return R.drawable.card_portrait_banned_wanderer;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + info.getCard());
        }
    }

    private int getPortraitHexagonalDrawableId() {
        if (info.isBanned()) {
            return getPortraitHexagonalBannedDrawableId();
        }

        switch (info.getCard()) {
            case Acrobat: return R.drawable.card_hex_portrait_acrobat;
            case Archer: return R.drawable.card_hex_portrait_archer;
            case Assassin: return R.drawable.card_hex_portrait_assassin;
            case Brewmaster: return R.drawable.card_hex_portrait_brewmaster;
            case Bruiser: return R.drawable.card_hex_portrait_bruiser;
            case ClawLauncher: return R.drawable.card_hex_portrait_claw_launcher;
            case HermitAndCub: return R.drawable.card_hex_portrait_hermit_and_cub;
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_illusionist_lms;
                }
                return R.drawable.card_hex_portrait_illusionist;
            }
            case Jailer: return R.drawable.card_hex_portrait_jailer;
            case LeaderKing: return R.drawable.card_hex_portrait_leader_king;
            case LeaderQueen: return R.drawable.card_hex_portrait_leader_queen;
            case Manipulator: return R.drawable.card_hex_portrait_manipulator;
            case Nemesis: return R.drawable.card_hex_portrait_nemesis;
            case Protector: return R.drawable.card_hex_portrait_protector;
            case Rider: return R.drawable.card_hex_portrait_rider;
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_royal_guard_lms;
                }
                return R.drawable.card_hex_portrait_royal_guard;
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_vizier_lms;
                }
                return R.drawable.card_hex_portrait_vizier;
            }
            case Wanderer: return R.drawable.card_hex_portrait_wanderer;
            default: throw new IllegalArgumentException("No portrait drawable for card: " + info.getCard());
        }
    }

    private int getPortraitHexagonalBannedDrawableId() {
        switch (info.getCard()) {
            case Acrobat: return R.drawable.card_hex_portrait_banned_acrobat;
            case Archer: return R.drawable.card_hex_portrait_banned_archer;
            case Assassin: return R.drawable.card_hex_portrait_banned_assassin;
            case Brewmaster: return R.drawable.card_hex_portrait_banned_brewmaster;
            case Bruiser: return R.drawable.card_hex_portrait_banned_bruiser;
            case ClawLauncher: return R.drawable.card_hex_portrait_banned_claw_launcher;
            case HermitAndCub: return R.drawable.card_hex_portrait_banned_hermit_and_cub;
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_illusionist_lms;
                }
                return R.drawable.card_hex_portrait_banned_illusionist;
            }
            case Jailer: return R.drawable.card_hex_portrait_banned_jailer;
            case Manipulator: return R.drawable.card_hex_portrait_banned_manipulator;
            case Nemesis: return R.drawable.card_hex_portrait_banned_nemesis;
            case Protector: return R.drawable.card_hex_portrait_banned_protector;
            case Rider: return R.drawable.card_hex_portrait_banned_rider;
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_royal_guard_lms;
                }
                return R.drawable.card_hex_portrait_banned_royal_guard;
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return R.drawable.card_hex_portrait_banned_vizier_lms;
                }
                return R.drawable.card_hex_portrait_banned_vizier;
            }
            case Wanderer: return R.drawable.card_hex_portrait_banned_wanderer;
            default: throw new IllegalArgumentException("No hex portrait drawable for card: " + info.getCard());
        }
    }

    @NonNull
    public CharacterCard getPortraitCard() {
        return info.getCard();
    }

    @Nullable
    public InteractionTarget getTarget() {
        return info.getTarget();
    }

    public void setTarget(@Nullable InteractionTarget target) {
        info.setTarget(target);
    }

    public void setUseBannedDisplay(boolean useBannedDisplay) {
        info.setBanned(useBannedDisplay);
        updateDisplay();
    }
}
