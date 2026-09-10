package com.leaders.app.activities;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.views.EndGameView;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.Objects;

public abstract class PlayableActivity extends BaseActivity
        implements PlayableBoardView.OnTargetClickListener, GameController.Listener {

    protected PlayableBoardView bdvBoard;

    protected MaterialButton btnUndoLastAction;

    protected CharacterNotificationView cnvCardInfo;

    protected EndGameView egvEndGame;

    protected GameController controller;


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

        return false;
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
                request.getContext().getCharacter(),
                gameContext.getBoard()
        );

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
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(characterCard);

            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
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
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();
            clearInteractionUI(gameContext);
            bdvBoard.setBoard(gameContext.getBoard());
        });
    }

    @Override
    public void onActionUndone(@NonNull Game game) {
        runOnUiThread(() -> bdvBoard.setBoard(game.getBoard()));
    }

    @Override
    public void onFeedback(@NonNull InteractionFeedback feedback,
                           @NonNull GameController.InteractionCompletion completion) {
        runOnUiThread(() -> bdvBoard.animateFeedback(feedback, completion::complete));
    }

    @Override
    public void onInteractionCleared() {
        runOnUiThread(() -> clearInteractionUI(controller.getCurrentContext()));
    }


    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();
            applyInteractionTargets(gameContext, request);
            updateInteractionUI(gameContext, request);
        });
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
