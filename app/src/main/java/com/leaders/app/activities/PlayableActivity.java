package com.leaders.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.views.EndGameView;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Objects;

public abstract class PlayableActivity extends BaseActivity
        implements PlayableBoardView.OnTargetClickListener, GameController.Listener {
    private static final String LOG_TAG = "PlayableActivity";

    private static final String STATE_ANIMATION_SPEED = "PLAYABLE_ACTIVITY.ANIMATION_SPEED";
    private static final String STATE_ANIMATE_PLAYABLE_ITEMS = "PLAYABLE_ACTIVITY.ANIMATE_PLAYABLE_CHARACTER";
    private static final String STATE_CELL_POSITIONS = "PLAYABLE_ACTIVITY.CELL_POSITIONS";

    protected PlayableBoardView bdvBoard;

    protected MaterialButton btnUndoLastAction;

    protected CharacterNotificationView cnvCardInfo;

    protected EndGameView egvEndGame;

    protected AnimationSpeed animationSpeed;
    protected boolean animatePlayableItems;
    protected GameController controller;

    //region GAME SNAPSHOT METHODS

    /**
     * Indicates whether the current game must survive the destruction of the activity by Android
     * (process killed while the app is in background).
     * When enabled, the activity must call {@link #loadGameSnapshot()} where it picks the history
     * to start from, and give priority to its result over the initial history.
     *
     * @return {@code true} if the game history must be snapshotted. Disabled by default.
     */
    protected boolean supportsGameSnapshot() {
        return false;
    }

    @Override
    protected final boolean supportsSnapshot() {
        return supportsGameSnapshot();
    }

    @Nullable
    @Override
    protected final JSONObject createSnapshot() throws JSONException {
        // The controller can be null if the initialization has been interrupted
        return controller != null ? new GameHistorySerializer().getAsJson(controller.getHistory()) : null;
    }

    /**
     * Returns the history of the game as it was when the activity has been destroyed by the system.
     *
     * @return the snapshot, or {@code null} if this is a fresh start (or if the snapshot is
     * unusable): the activity must then start from its usual initial history.
     */
    @Nullable
    protected final GameHistory loadGameSnapshot() {
        JSONObject joSnapshot = loadSnapshot();
        if (joSnapshot == null) {
            return null;
        }

        try {
            return new GameHistorySerializer().getFromJson(joSnapshot, new SerializationContext());
        } catch (JSONException | RuntimeException e) {
            Log.w(LOG_TAG, "Unusable game snapshot, it is discarded", e);
            discardSnapshot();
            return null;
        }
    }

    //endregion

    //region UI OPTIONS STATE METHODS

    // The options changed by the player during the game are kept in the saved instance state:
    // they are tiny, and they do not depend on the snapshot.

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (animationSpeed != null) {
            outState.putString(STATE_ANIMATION_SPEED, animationSpeed.name());
        }
        outState.putBoolean(STATE_ANIMATE_PLAYABLE_ITEMS, animatePlayableItems);
        outState.putBoolean(STATE_CELL_POSITIONS, bdvBoard.isCellPositionVisible());
    }

    private void restoreUiOptions(@NonNull Bundle savedState) {
        String savedSpeed = savedState.getString(STATE_ANIMATION_SPEED);
        if (savedSpeed != null) {
            animationSpeed = AnimationSpeed.valueOf(savedSpeed);
        }

        animatePlayableItems = savedState.getBoolean(STATE_ANIMATE_PLAYABLE_ITEMS, animatePlayableItems);
        bdvBoard.setCellPositionVisible(savedState.getBoolean(STATE_CELL_POSITIONS, bdvBoard.isCellPositionVisible()));
    }

    //endregion

    //region BASE ACTIVITY OVERRIDDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(getBoardViewId());
        btnUndoLastAction = findViewById(getUndoLastActionButtonId());
        cnvCardInfo = findViewById(getCharacterNotificationViewId());
        egvEndGame = findViewById(getEndGameViewId());
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onBoardCharacterLongClick);

        btnUndoLastAction.setOnClickListener(this::onUndoLastActionClick);

        cnvCardInfo.setOnClickListener(this::onCardInfoClick);

        egvEndGame.setOnClickListener(this::onEndGameClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        Settings settings = ((LeadersApplication) getApplication()).getSettings();
        animationSpeed = settings.getAnimationSpeed();
        animatePlayableItems = settings.animatePlayableItems();

        // The options changed during the previous session take priority over the settings
        Bundle savedState = getSavedState();
        if (savedState != null) {
            restoreUiOptions(savedState);
        }
    }

    protected abstract int getBoardViewId();

    protected abstract int getUndoLastActionButtonId();

    protected abstract int getCharacterNotificationViewId();

    protected abstract int getEndGameViewId();

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    protected abstract CharacterSkins getCharacterSkins();

    //endregion

    //region BOARD INTERACTION METHODS

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }

    @Override
    public void onEmptyClick() {
        controller.cancelAction();
    }

    protected boolean onBoardCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(
                ((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized"
        );

        showCardDescriptionNotification(characterType.getCharacterCard());

        return true;
    }

    //endregion

    //region INTERACTION UI METHODS

    protected void clearInteractionUI(@NonNull GameContext gameContext) {
        bdvBoard.clearTargets();
        ButtonUtils.setEnabled(btnUndoLastAction, false);
    }

    protected void highlightPlayableCharacters(@NonNull GameContext gameContext,
                                               @NonNull InteractionRequest request) {

        bdvBoard.highlightPlayableCharacters(
                gameContext.getPlayableCharacters(),
                request.getContext().getTargetCharacter(),
                gameContext.getBoard()
        );

        if (!animatePlayableItems) {
            return;
        }

        if (request.getRequestType() == InteractionType.PlayableCharacterExpected) {
            bdvBoard.startPlayableCharactersShineAnimation();
        } else {
            bdvBoard.stopPlayableCharactersShineAnimation();
        }
    }

    protected void applyInteractionTargets(@NonNull GameContext gameContext,
                                           @NonNull InteractionRequest request) {
        bdvBoard.applyTargets(request.getLegalTargets(), request.getContext(), gameContext.getBoard());
    }

    protected void updateInteractionUI(@NonNull GameContext gameContext,
                                       @NonNull InteractionRequest request) {
        highlightPlayableCharacters(gameContext, request);

        updateUndoButton();
    }

    protected final void updateUndoButton() {
        ButtonUtils.setEnabled(btnUndoLastAction, canUndoLastAction());
    }

    protected boolean canUndoLastAction() {
        return controller.canUndoLastAction();
    }

    //endregion

    //region CHARACTER INFORMATION METHODS

    protected final void showCardDescriptionNotification(@NonNull CharacterCard characterCard) {
        if (cnvCardInfo.getCharacterCard() == characterCard) {
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.show(characterCard);
        }
    }

    //endregion

    //region END GAME METHODS

    protected final LeaderType getLeaderType(@NonNull Board board, @NonNull TeamColor teamColor) {
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(board, teamColor),
                "No leader found for team: " + teamColor
        );

        return LeaderType.getFromCharacter(leaderCell.getCharacter());
    }

    protected final void showEndGame(@NonNull GameContext gameContext,
                                     @NonNull TeamColor winnerColor,
                                     @NonNull EndGameType endGameType,
                                     String title,
                                     String subtitle) {
        clearInteractionUI(gameContext);
        bdvBoard.highlightPlayableCharacters(Collections.emptyList(), null, gameContext.getBoard());

        egvEndGame.update(
                endGameType,
                getLeaderType(gameContext.getBoard(), winnerColor),
                title,
                subtitle
        );

        egvEndGame.show();
    }

    //endregion

    //region GAME CONTROLLER LISTENER METHODS

    @Override
    public void onGameStarted(@NonNull GameContext context) {
        runOnUiThread(() -> {
            clearInteractionUI(context);
            bdvBoard.setBoard(context.getBoard());
        });
    }

    @Override
    public void onActionUndone(@NonNull GameContext context) {
        runOnUiThread(() -> bdvBoard.setBoard(context.getBoard()));
    }

    @Override
    public void onInteractionCleared(@NonNull GameContext context) {
        runOnUiThread(() -> clearInteractionUI(context));
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request, @NonNull GameContext context) {
        runOnUiThread(() -> {
            applyInteractionTargets(context, request);
            updateInteractionUI(context, request);
        });
    }

    @Override
    public void onFeedback(@NonNull InteractionFeedback feedback,
                           @NonNull GameController.InteractionCompletion completion) {
        runOnUiThread(() -> bdvBoard.animateFeedback(
                feedback, animationSpeed, getCharacterSkins(), completion::complete
        ));
    }

    //endregion

    //region VIEW LISTENER METHODS

    protected final void onNonInteractiveElementClick(View v) {
        controller.cancelAction();
    }

    private void onUndoLastActionClick(View v) {
        controller.undoLastAction();
    }

    private void onCardInfoClick(View v) {
        cnvCardInfo.hide();
    }

    private void onEndGameClick(View v) {
        egvEndGame.hide();
    }

    //endregion

    @Override
    protected void onDestroy() {
        if (controller != null) {
            controller.shutdown();
        }

        super.onDestroy();
    }
}