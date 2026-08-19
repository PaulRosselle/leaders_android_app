package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.leaders.R;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.gamelogic.enums.CharacterCard;

public final class CharacterNotificationView extends ConstraintLayout {
    private static final int VISIBILITY_ANIMATION_DURATION = 200;

    private final CharacterCardPortraitView ptvPortrait;
    private final TextView txvTitle, txvInfo;
    @Nullable
    private CharacterCard characterCard;
    private int statusBarOffset;

    public CharacterNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_notification, this);

        ptvPortrait = findViewById(R.id.ptvPortrait_vwCharacterNotification);
        txvTitle = findViewById(R.id.txvCardTitle_vwCharacterNotification);
        txvInfo = findViewById(R.id.txvCardInfo_vwCharacterNotification);

        characterCard = null;
        statusBarOffset = 0;

        // Added shadow behind the notification
        setBackgroundResource(R.drawable.round_rect_gloden_outline_bg);
        setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setElevation(getResources().getDisplayMetrics().density * 8);

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            statusBarOffset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            return insets;
        });
    }

    public void setCharacterCard(@Nullable CharacterCard characterCard) {
        this.characterCard = characterCard;
        // When no card is displayed, child views aren't updated
        if (characterCard == null) {
            return;
        }

        ptvPortrait.setPortraitCard(this.characterCard);
        txvTitle.setText(CharacterCardUtils.getFormattedNameId(this.characterCard));
        txvInfo.setText(CharacterCardUtils.getDescriptionId(this.characterCard));
    }

    @Nullable
    public CharacterCard getCharacterCard() {
        return characterCard;
    }

    private float getHiddenPosY() {
        return - getHeight() - statusBarOffset - ((MarginLayoutParams) getLayoutParams()).topMargin;
    }

    public void show() {
        setTranslationY(getHiddenPosY());
        setVisibility(VISIBLE);
        animate().translationY(0).setDuration(VISIBILITY_ANIMATION_DURATION).start();
    }

    public void hide() {
        animate().translationY(getHiddenPosY()).setDuration(VISIBILITY_ANIMATION_DURATION)
                .withEndAction(() -> {
            setVisibility(GONE);
            setTranslationY(0f);
        }).start();
    }
}
