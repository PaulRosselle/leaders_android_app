package com.leaders.app.views.portrait;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        int resId;
        switch (info.getDisplayMode()) {
            case Default: resId = getPortraitDrawableId(); break;
            case Hexagonal: resId = getPortraitHexagonalDrawableId(); break;
            default: throw new IllegalStateException("Unexpected display mode: " + info.getDisplayMode());
        }
        setImageResource(resId);
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
            case Acrobat: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_acrobat,
                    R.drawable.card_portrait_banned_acrobat_b,
                    R.drawable.card_portrait_banned_acrobat_w
            );
            case Archer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_archer,
                    R.drawable.card_portrait_banned_archer_b,
                    R.drawable.card_portrait_banned_archer_w
            );
            case Assassin: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_assassin,
                    R.drawable.card_portrait_banned_assassin_b,
                    R.drawable.card_portrait_banned_assassin_w
            );
            case Brewmaster: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_brewmaster,
                    R.drawable.card_portrait_banned_brewmaster_b,
                    R.drawable.card_portrait_banned_brewmaster_w
            );
            case Bruiser: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_bruiser,
                    R.drawable.card_portrait_banned_bruiser_b,
                    R.drawable.card_portrait_banned_bruiser_w
            );
            case ClawLauncher: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_claw_launcher,
                    R.drawable.card_portrait_banned_claw_launcher_b,
                    R.drawable.card_portrait_banned_claw_launcher_w
            );
            case HermitAndCub: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_hermit_and_cub,
                    R.drawable.card_portrait_banned_hermit_and_cub_b,
                    R.drawable.card_portrait_banned_hermit_and_cub_w
            );
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_portrait_banned_illusionist_lms,
                            R.drawable.card_portrait_banned_illusionist_lms_b,
                            R.drawable.card_portrait_banned_illusionist_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_portrait_banned_illusionist,
                        R.drawable.card_portrait_banned_illusionist_b,
                        R.drawable.card_portrait_banned_illusionist_w
                );
            }
            case Jailer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_jailer,
                    R.drawable.card_portrait_banned_jailer_b,
                    R.drawable.card_portrait_banned_jailer_w
            );
            case Manipulator: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_manipulator,
                    R.drawable.card_portrait_banned_manipulator_b,
                    R.drawable.card_portrait_banned_manipulator_w
            );
            case Nemesis: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_nemesis,
                    R.drawable.card_portrait_banned_nemesis_b,
                    R.drawable.card_portrait_banned_nemesis_w
            );
            case Protector: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_protector,
                    R.drawable.card_portrait_banned_protector_b,
                    R.drawable.card_portrait_banned_protector_w
            );
            case Rider: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_rider,
                    R.drawable.card_portrait_banned_rider_b,
                    R.drawable.card_portrait_banned_rider_w
            );
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_portrait_banned_royal_guard_lms,
                            R.drawable.card_portrait_banned_royal_guard_lms_b,
                            R.drawable.card_portrait_banned_royal_guard_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_portrait_banned_royal_guard,
                        R.drawable.card_portrait_banned_royal_guard_b,
                        R.drawable.card_portrait_banned_royal_guard_w
                );
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_portrait_banned_vizier_lms,
                            R.drawable.card_portrait_banned_vizier_lms_b,
                            R.drawable.card_portrait_banned_vizier_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_portrait_banned_vizier,
                        R.drawable.card_portrait_banned_vizier_b,
                        R.drawable.card_portrait_banned_vizier_w
                );
            }
            case Wanderer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_portrait_banned_wanderer,
                    R.drawable.card_portrait_banned_wanderer_b,
                    R.drawable.card_portrait_banned_wanderer_w
            );
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
            case Acrobat: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_acrobat,
                    R.drawable.card_hex_portrait_banned_acrobat_b,
                    R.drawable.card_hex_portrait_banned_acrobat_w
            );
            case Archer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_archer,
                    R.drawable.card_hex_portrait_banned_archer_b,
                    R.drawable.card_hex_portrait_banned_archer_w
            );
            case Assassin: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_assassin,
                    R.drawable.card_hex_portrait_banned_assassin_b,
                    R.drawable.card_hex_portrait_banned_assassin_w
            );
            case Brewmaster: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_brewmaster,
                    R.drawable.card_hex_portrait_banned_brewmaster_b,
                    R.drawable.card_hex_portrait_banned_brewmaster_w
            );
            case Bruiser: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_bruiser,
                    R.drawable.card_hex_portrait_banned_bruiser_b,
                    R.drawable.card_hex_portrait_banned_bruiser_w
            );
            case ClawLauncher: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_claw_launcher,
                    R.drawable.card_hex_portrait_banned_claw_launcher_b,
                    R.drawable.card_hex_portrait_banned_claw_launcher_w
            );
            case HermitAndCub: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_hermit_and_cub,
                    R.drawable.card_hex_portrait_banned_hermit_and_cub_b,
                    R.drawable.card_hex_portrait_banned_hermit_and_cub_w
            );
            case Illusionist: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_hex_portrait_banned_illusionist_lms,
                            R.drawable.card_hex_portrait_banned_illusionist_lms_b,
                            R.drawable.card_hex_portrait_banned_illusionist_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_hex_portrait_banned_illusionist,
                        R.drawable.card_hex_portrait_banned_illusionist_b,
                        R.drawable.card_hex_portrait_banned_illusionist_w
                );
            }
            case Jailer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_jailer,
                    R.drawable.card_hex_portrait_banned_jailer_b,
                    R.drawable.card_hex_portrait_banned_jailer_w
            );
            case Manipulator: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_manipulator,
                    R.drawable.card_hex_portrait_banned_manipulator_b,
                    R.drawable.card_hex_portrait_banned_manipulator_w
            );
            case Nemesis: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_nemesis,
                    R.drawable.card_hex_portrait_banned_nemesis_b,
                    R.drawable.card_hex_portrait_banned_nemesis_w
            );
            case Protector: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_protector,
                    R.drawable.card_hex_portrait_banned_protector_b,
                    R.drawable.card_hex_portrait_banned_protector_w
            );
            case Rider: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_rider,
                    R.drawable.card_hex_portrait_banned_rider_b,
                    R.drawable.card_hex_portrait_banned_rider_w
            );
            case RoyalGuard: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_hex_portrait_banned_royal_guard_lms,
                            R.drawable.card_hex_portrait_banned_royal_guard_lms_b,
                            R.drawable.card_hex_portrait_banned_royal_guard_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_hex_portrait_banned_royal_guard,
                        R.drawable.card_hex_portrait_banned_royal_guard_b,
                        R.drawable.card_hex_portrait_banned_royal_guard_w
                );
            }
            case Vizier: {
                if (info.getSkinType() == CharacterSkinType.LaughingMoonSage) {
                    return getPortraitFromTeamColor(info.getTeamColor(),
                            R.drawable.card_hex_portrait_banned_vizier_lms,
                            R.drawable.card_hex_portrait_banned_vizier_lms_b,
                            R.drawable.card_hex_portrait_banned_vizier_lms_w
                    );
                }
                return getPortraitFromTeamColor(info.getTeamColor(),
                        R.drawable.card_hex_portrait_banned_vizier,
                        R.drawable.card_hex_portrait_banned_vizier_b,
                        R.drawable.card_hex_portrait_banned_vizier_w
                );
            }
            case Wanderer: return getPortraitFromTeamColor(info.getTeamColor(),
                    R.drawable.card_hex_portrait_banned_wanderer,
                    R.drawable.card_hex_portrait_banned_wanderer_b,
                    R.drawable.card_hex_portrait_banned_wanderer_w
            );
            default: throw new IllegalArgumentException("No hex portrait drawable for card: " + info.getCard());
        }
    }

    private int getPortraitFromTeamColor(@Nullable TeamColor teamColor,
                                         int nullResId, int blackResId, int whiteResId) {
        if (teamColor == null) {
            return nullResId;
        }

        if (teamColor == TeamColor.Black) {
            return blackResId;
        }

        return whiteResId;
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
