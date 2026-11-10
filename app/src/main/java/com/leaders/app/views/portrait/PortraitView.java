package com.leaders.app.views.portrait;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.app.entities.PortraitInfo;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.PortraitDisplayMode;
import com.leaders.app.utilities.CharacterCardUtils;
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
            case Default: characterResId = getCharacterDrawableResId(); break;
            case Hexagonal: characterResId = getCharacterHexagonalDrawableResId(); break;
            default: throw new IllegalStateException("Unexpected display mode: " + info.getDisplayMode());
        }

        return AppCompatResources.getDrawable(getContext(), characterResId);
    }

    @DrawableRes
    private int getCharacterDrawableResId() {
        return info.isBanned() ?
                CharacterCardUtils.getPortraitBannedDrawableId(info.getCard(), info.getSkinType()) :
                CharacterCardUtils.getPortraitDrawableId(info.getCard(), info.getSkinType());
    }

    @DrawableRes
    private int getCharacterHexagonalDrawableResId() {
        return info.isBanned() ?
                CharacterCardUtils.getPortraitHexagonalBannedDrawableId(info.getCard(), info.getSkinType()) :
                CharacterCardUtils.getPortraitHexagonalDrawableId(info.getCard(), info.getSkinType());
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
