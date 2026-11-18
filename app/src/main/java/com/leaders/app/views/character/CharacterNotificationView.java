package com.leaders.app.views.character;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.leaders.R;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;

public final class CharacterNotificationView extends ConstraintLayout {
    private static final int VISIBILITY_ANIMATION_DURATION = 200;
    private static final int HIDE_DELAY = 5000;

    private final Runnable hideRunnable;

    private final CardDescriptionView cdvMain;
    private final CardDescriptionView cdvAdditional;
    private final View vwSeparator;

    private int statusBarOffset;
    @NonNull
    private CharacterSkins characterSkins;


    public CharacterNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        hideRunnable = this::hide;

        inflate(context, R.layout.view_character_notification, this);

        cdvMain = findViewById(R.id.cdvMain_vwCharacterNotification);
        vwSeparator = findViewById(R.id.vwSeparator_vwCharacterNotification);
        cdvAdditional = findViewById(R.id.cdvAdditional_vwCharacterNotification);

        statusBarOffset = 0;
        characterSkins = new CharacterSkins();

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

    private void setMainCard(@Nullable CharacterCard characterCard) {
        cdvMain.setCard(characterCard, characterSkins);
    }

    private void setAdditionalCard(@Nullable CharacterCard characterCard) {
        cdvAdditional.setCard(characterCard, characterSkins);
        int visibility = characterCard != null ? VISIBLE : GONE;
        cdvAdditional.setVisibility(visibility);
        vwSeparator.setVisibility(visibility);
    }

    public void setCharacterSkins(@NonNull CharacterSkins characterSkins) {
        this.characterSkins = characterSkins;
    }

    private float getHiddenPosY() {
        return - getHeight() - statusBarOffset - ((MarginLayoutParams) getLayoutParams()).topMargin;
    }

    @Nullable
    private CharacterCard getCard(@Nullable CharacterType characterType) {
        return characterType != null ? characterType.getCharacterCard() : null;
    }

    public void toggle(@Nullable CharacterType mainCharacterType,
                       @Nullable CharacterType additionalCharacterType) {
        toggle(getCard(mainCharacterType), getCard(additionalCharacterType));
    }

    public void toggle(@Nullable CharacterCard mainCard, @Nullable CharacterCard additionalCard) {
        if (cdvMain.getCard() == mainCard && cdvAdditional.getCard() == additionalCard) {
            hide();
        } else {
            show(mainCard, additionalCard);
        }
    }

    public void show(@Nullable CharacterType mainCharacterType,
                     @Nullable CharacterType additionalCharacterType) {
        show(getCard(mainCharacterType), getCard(additionalCharacterType));
    }

    public void show(@Nullable CharacterCard mainCard, @Nullable CharacterCard additionalCard) {
        if (mainCard == null && additionalCard != null) {
            throw new IllegalStateException("Main card should not be null with an additional card");
        }

        setMainCard(mainCard);
        setAdditionalCard(additionalCard);
        show();
    }

    private void show() {
        if (getVisibility() != VISIBLE) {
            setTranslationY(getHiddenPosY());
            setVisibility(VISIBLE);
            animate().translationY(0).setDuration(VISIBILITY_ANIMATION_DURATION).start();
        }

        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, HIDE_DELAY);
    }

    public void hide() {
        setMainCard(null);

        if (getVisibility() != GONE) {
            animate().translationY(getHiddenPosY()).setDuration(VISIBILITY_ANIMATION_DURATION)
                    .withEndAction(() -> {
                        setVisibility(GONE);
                        setTranslationY(0f);
                    }).start();
        }

        removeCallbacks(hideRunnable);
    }
}
