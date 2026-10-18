package com.leaders.app.views.puzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.puzzlelogic.entities.blocker.NemesisBlocker;
import com.leaders.puzzlelogic.entities.blocker.NemesisBlockerCallback;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import java.util.Objects;

public class NemesisBlockerView extends ConstraintLayout {
    @Nullable
    private InteractionRequest request;
    private final MaterialButton btnInterrupt;
    private NemesisBlocker nemesisBlocker;

    public NemesisBlockerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_nemesis_blocker, this);

        PortraitView ptvPortrait = findViewById(R.id.ptvPortrait_vwNemesisBlocker);
        ptvPortrait.setPortraitCard(CharacterCard.Nemesis);
        btnInterrupt = findViewById(R.id.btnInterrupt_vwNemesisBlocker);

        // Added shadow behind the notification
        setBackgroundResource(R.drawable.round_rect_gloden_outline_bg);
        setClipToOutline(true);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setElevation(getResources().getDisplayMetrics().density * 8);

        request = null;
        nemesisBlocker = null;

        // TODO - handle resources in xml
        // TODO - handle blocker cancelling
    }

    public boolean canAutoBlock(@NonNull InteractionRequest request) {
        return canAutoSelectNemesis(request) || isEnemyNemesisPlaying(request);
    }

    public boolean canAutoSelectNemesis(@NonNull InteractionRequest request) {
        return request.getRequestType() == InteractionType.PlayableCharacterExpected &&
                request.getLegalTargets().size() == 1 &&
                Objects.requireNonNull(
                        request.getLegalTargets().get(0).getChosenPlayableCharacter(),
                        "Invalid playable character request target"
                ).isMandatory();
    }

    private boolean isEnemyNemesisPlaying(@NonNull InteractionRequest request) {
        Character character;
        return (character = request.getContext().getCharacter()) != null &&
                character.getCharacterType() == CharacterType.Nemesis &&
                character.getTeamColor() != PuzzleEditionUtils.getPuzzlePlayerTeamColor();
    }

    public void clear() {
        request = null;
        nemesisBlocker = null;
    }

    @NonNull
    public NemesisBlocker initBlocker(@NonNull GameHistory gameHistory,
                                      @NonNull InteractionRequest request,
                                      @NonNull NemesisBlockerCallback callback) {
        this.request = request;
        nemesisBlocker = new NemesisBlocker(gameHistory, request, callback);
        return nemesisBlocker;
    }

    @NonNull
    public InteractionRequest getRequest() {
        if (request == null || nemesisBlocker == null) {
            throw new IllegalStateException("Request not initialized");
        }
        return request;
    }

    public void shutdownBlocker() {
        if (nemesisBlocker == null) {
            return;
        }

        if (nemesisBlocker.canBeInterrupted()) {
            nemesisBlocker.cancel();
        }
        clear();
    }

    public void setOnInterruptClickListener(OnClickListener onClickListener) {
        btnInterrupt.setOnClickListener(onClickListener);
    }
}
