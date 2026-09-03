package com.leaders.app.activities.duel;

import android.animation.LayoutTransition;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.EndGameView;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public final class DuelPlayerActivity extends BaseActivity implements
        PlayableBoardView.OnTargetClickListener,
        CharacterCardSelectionView.OnCardSelectedListener,
        GameController.Listener {

    private PlayableBoardView bdvBoard;
    private CharacterCardSelectionView ccsvCardSelector;
    private PlayerBottomView pbvCurrentPlayer;
    private PlayerTopView ptvOpposingPlayer;
    private TextView txvPlayerTurn;

    private CharacterNotificationView cnvCardInfo;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private View vwDialogBg;

    private EndGameView egvEndGame;

    private CharacterDisplay chdNewCharacter;

    private MaterialButton btnCards;
    private MaterialButton btnUndoLastAction;
    private MaterialButton btnNextPhase;
    private HighlightView hlvNextPhase;


    private GameController controller;
    

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actDuelPlayer);
        // Since we're animating a translation on the board,
        // we must disable transitions able to affect children views
        LayoutTransition boardTransition = bdvBoard.getLayoutTransition();
        boardTransition.disableTransitionType(LayoutTransition.APPEARING);
        boardTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        boardTransition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        boardTransition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        boardTransition.enableTransitionType(LayoutTransition.CHANGING);

        ccsvCardSelector = findViewById(R.id.ccsvCardSelector_actDuelPlayer);
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);

        pbvCurrentPlayer = findViewById(R.id.pbvCurrentPlayer_actDuelPlayer);
        ptvOpposingPlayer = findViewById(R.id.ptvOpposingPlayer_actDuelPlayer);
        txvPlayerTurn = findViewById(R.id.txvPlayerTurn_actDuelPlayer);

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actDuelPlayer);

        btnActions = findViewById(R.id.btnActions_actDuelPlayer);
        amvActions = findViewById(R.id.amvActions_actDuelPlayer);
        amvActions.addActionButton(R.drawable.icon_position, R.string.board_coordinates, 0, this::onDisplayCellPositionClick);
        vwDialogBg = findViewById(R.id.vwDialogBg_actDuelPlayer);

        egvEndGame = findViewById(R.id.egvEndGame_actDuelPlayer);

        btnCards = findViewById(R.id.btnCards_actDuelPlayer);
        btnUndoLastAction = findViewById(R.id.btnUndoLastAction_actDuelPlayer);
        btnNextPhase = findViewById(R.id.btnNextPhase_actDuelPlayer);
        hlvNextPhase = findViewById(R.id.hlvNextPhase_actDuelPlayer);
    }

    @Override
    protected void initListeners() {
        super.initListeners();


        // Non interactive element listeners
        (findViewById(R.id.clyMain_actDuelPlayer)).setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnScrollViewClickListener(this::onNonInteractiveElementClick);

        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onBoardCharacterLongClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

        cnvCardInfo.setOnClickListener(v -> cnvCardInfo.hide());

        btnActions.setOnClickListener(this::onActionsClick);
        vwDialogBg.setOnClickListener(this::vwDialogBgClick);

        egvEndGame.setOnClickListener(view -> egvEndGame.hide());

        btnCards.setOnClickListener(this::onCardsClick);
        btnUndoLastAction.setOnClickListener(this::onUndoLastActionClick);
        btnNextPhase.setOnClickListener(this::onNextPhaseClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        String gameDatas = getIntent().getStringExtra(ExtraUtils.EXTRA_DUEL_GAME_DATAS);
        if (gameDatas == null || gameDatas.isEmpty()) {
            throw new IllegalStateException("Invalid duel game datas: missing datas");
        }

        GameHistory gameHistory;
        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            JSONObject joGameDatas = new JSONObject(gameDatas);
            gameHistory = serializer.getFromJson(joGameDatas, new SerializationContext());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        controller = new GameController(this);
        controller.startGame(gameHistory);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_duel_player;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actDuelPlayer;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actDuelPlayer;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return true;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.DuelPlayer;
    }

    //endregion

    //region VIEW LISTENER METHODS

    public void onNonInteractiveElementClick(View v) {
        controller.cancelAction();
    }

    private void onCardsClick(View v) {
        setCardSelectorVisible(ccsvCardSelector.getVisibility() != View.VISIBLE);
    }

    private void onUndoLastActionClick(View v) {
        controller.undoLastAction();
    }

    private void onNextPhaseClick(View v) {
        GameContext gameContext = controller.getCurrentContext();

        if (gameContext.getGamePhase().getPhaseType() == GamePhaseType.Banishment) {
            controller.selectTarget(ccsvCardSelector.getSelectedTarget());
        } else {
            controller.endPhase();
        }
    }

    private void onActionsClick(View v) {
        setActionsMenuVisible(true);
    }

    private boolean onBoardCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        showCardDescriptionNotification(characterType.getCharacterCard());
        return true;
    }

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((CharacterCardPortraitView) v).getPortraitCard());
        return true;
    }

    //endregion

    //region ACTIONS METHODS

    private void vwDialogBgClick(View v) {
        setActionsMenuVisible(false);
    }

    private void onDisplayCellPositionClick(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());

        setActionsMenuVisible(false);
    }

    private void setActionsMenuVisible(boolean visible) {
        amvActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion

    //region INTERACTION METHODS

    @Override
    public void onEmptyClick() {
        controller.cancelAction();
    }

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }

    public void onRecruitmentCardSelected(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }
    public void onBanishmentCardSelected() {
        if (!btnNextPhase.isEnabled()) {
            setBtnNextPhaseEnabled(true, true);
        }
    }

    @Override
    public void onNotSelectableCardClick() {
        controller.cancelAction();
    }

    //endregion

    //region UI STATE METHODS

    private void showCardDescriptionNotification(@NonNull CharacterCard characterCard) {
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

    private void setCardSelectorVisible(boolean visible) {
        // When recruiting, we display the cardSelector view below the current player view.
        // The layout transition is animated for both the board and current player view
        bdvBoard.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        pbvCurrentPlayer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvCurrentPlayer.getLayoutParams();
        // When recruiting, every view is aligned on top of each other
        if (visible) {
            boardParams.verticalBias = 0f;
            playerViewParams.verticalBias = 0f;
            float dpRatio = getResources().getDisplayMetrics().density;
            int boardHeight = bdvBoard.getMeasuredHeight();
            float playerHeaderHeight = boardHeight * (72f / 1177f);
            int boardMargin = 16;
            int playerViewMargin = boardMargin + 8;

            boardParams.topMargin = (int) (playerHeaderHeight + boardMargin * dpRatio);
            playerViewParams.topMargin = (int) (boardHeight - pbvCurrentPlayer.getMeasuredHeight() +
                    playerHeaderHeight * 2 + playerViewMargin * dpRatio);
            ccsvCardSelector.show(true);

            // By default, each playerView is on a vertical extremity while the board is centered
        } else {
            boardParams.verticalBias = 0.5f;
            playerViewParams.verticalBias = 1f;
            boardParams.topMargin = 0;
            playerViewParams.topMargin = 0;
            ccsvCardSelector.hide();
        }
        bdvBoard.setLayoutParams(boardParams);
        pbvCurrentPlayer.setLayoutParams(playerViewParams);

        // The requestLayout calls start the layout transition animation
        bdvBoard.requestLayout();
        pbvCurrentPlayer.requestLayout();
    }

    private void showEndGame(@NonNull GameContext gameContext, @NonNull Player winner) {
        TeamColor winnerColor = winner.getTeamColor();
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(gameContext.getBoard(), winnerColor),
                "No leader found for team: " + winnerColor
        );
        String loserName = "";
        for (Player player : List.of(gameContext.getCurrentPlayer(), gameContext.getOpposingPlayer())) {
            if (player.getTeamColor() != winnerColor) {
                loserName = player.getName();
            }
        }

        egvEndGame.update(
                EndGameType.Victory,
                LeaderType.getFromCharacter(leaderCell.getCharacter()),
                getString(R.string.duel_over),
                String.format(getString(R.string.player_defeated_player), winner.getName(), loserName)
        );
        egvEndGame.show();
    }

    private void setBtnNextPhaseEnabled(boolean enabled, boolean highlight) {
        ButtonUtils.setEnabled(btnNextPhase, enabled);
        hlvNextPhase.setVisibility(highlight ? View.VISIBLE : View.GONE);
        if (highlight) {
            hlvNextPhase.startAnimation();
        } else {
            hlvNextPhase.stopAnimation();
        }
    }

    private LeaderType getPlayerLeaderType(@NonNull Player player, @NonNull Board board) {
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(board, player.getTeamColor()),
                "No leader found for player: " + player
        );
        return LeaderType.getFromCharacter(leaderCell.getCharacter());
    }

    private void highlightPlayableCharacters(@NonNull GameContext gameContext,
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

        if (request.getLegalTargets().stream()
                .anyMatch(target -> target.getCategory() == TargetCategory.RecruitmentDestination)) {
            bdvBoard.startRecruitmentCellsAnimation();
        } else {
            bdvBoard.stopRecruitmentCellsAnimation();
        }
    }

    private void highlightSelectableCards(@NonNull GameContext gameContext,
                                          @NonNull InteractionRequest request) {
        GamePhase gamePhase = gameContext.getGamePhase();

        boolean isValidPhase = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;
        boolean selectableCardRequest = request.getRequestType() == InteractionType.SelectableCharacterCardExpected;

        if (isValidPhase && selectableCardRequest) {
            ccsvCardSelector.startShineAnimation();
        } else {
            ccsvCardSelector.stopShineAnimation();
        }
    }

    private void applyPlayerChange(@NonNull GameContext gameContext) {
        Player currentPlayer = gameContext.getCurrentPlayer();

        bdvBoard.setOrientation(currentPlayer.getTeamColor() == TeamColor.Black ?
                BoardOrientation.Default : BoardOrientation.Rotated);

        Board board = gameContext.getBoard();
        pbvCurrentPlayer.setPlayer(currentPlayer, getPlayerLeaderType(currentPlayer, board));
        Player opposingPlayer = gameContext.getOpposingPlayer();
        ptvOpposingPlayer.setPlayer(opposingPlayer, getPlayerLeaderType(opposingPlayer, board));
    }

    private boolean isPlayerNameFirstCharVowel(@NonNull String playerName) {
        final String vowels = "aeiouAEIOU";
        return !playerName.isEmpty() && (vowels.indexOf(playerName.charAt(0)) != -1);
    }

    private void applyPhaseChange(@NonNull GamePhase gamePhase,
                                  boolean canChangeSelectableCardsVisibility) {
        String playerName = gamePhase.getPhasePlayer().getName();
        int playerTurnFormat = isPlayerNameFirstCharVowel(playerName) ? R.string.player_turn_vowel : R.string.player_turn_consonant;
        txvPlayerTurn.setText(String.format(getString(playerTurnFormat), playerName));

        boolean lockSelectableCardsView = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;
        ButtonUtils.setEnabled(btnCards, !lockSelectableCardsView);
        if (canChangeSelectableCardsVisibility) {
            setCardSelectorVisible(lockSelectableCardsView);
        }
    }

    private void setNewCharacterVisible(@Nullable Character newCharacter, boolean visible) {
        chdNewCharacter.getCharacterView().setVisibility(visible ? View.VISIBLE : View.GONE);
        chdNewCharacter.setIsHighlighted(visible, false);

        if (visible) {
            int size = bdvBoard.getCharacterDisplaySize();
            chdNewCharacter.setSize(size);
            chdNewCharacter.setPosition(
                    (ccsvCardSelector.getWidth() - size) / 2f,
                    (ccsvCardSelector.getHeight() - size) / 2f
            );

            if (newCharacter != null) {
                chdNewCharacter.getCharacterView().setCharacter(newCharacter);
            }
            chdNewCharacter.startHighlightAnimation();
        } else {
            chdNewCharacter.stopHighlightAnimation();
        }
    }

    //endregion

    //region INTERACTION METHODS

    private void clearInteractionUI(@NonNull GameContext gameContext) {
        bdvBoard.clearTargets();
        ccsvCardSelector.applyCards(gameContext.getAvailableCharacterCards());

        ccsvCardSelector.setPortraitsVisible(true);
        setNewCharacterVisible(null, false);

        ButtonUtils.setEnabled(btnUndoLastAction, false);
        setBtnNextPhaseEnabled(false, false);
    }

    private void updateInteractionUI(@NonNull GameContext gameContext,
                                     @NonNull InteractionRequest request) {
        highlightPlayableCharacters(gameContext, request);
        highlightSelectableCards(gameContext, request);

        ButtonUtils.setEnabled(btnUndoLastAction, controller.canUndoLastAction());
        setBtnNextPhaseEnabled(controller.canEndPhaseAction(), request.getLegalTargets().isEmpty());

        if (request.getRequestType() == InteractionType.PositionExpected &&
                gameContext.getGamePhase().getPhaseType() == GamePhaseType.Recruitment) {
            ccsvCardSelector.setPortraitsVisible(false);
            setNewCharacterVisible(request.getContext().getCharacter(), true);
        }

        applyPlayerChange(gameContext);

        applyPhaseChange(gameContext.getGamePhase(), false);
    }

    //endregion

    //region GAME CONTROLLER METHODS

    @Override
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();

            bdvBoard.setBoard(game.getBoard());
            ccsvCardSelector.applyGameModeParams(gameContext.getGameMode());

            clearInteractionUI(gameContext);
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        GameContext gameContext = controller.getCurrentContext();

        clearInteractionUI(gameContext);
        ButtonUtils.setEnabled(btnCards, true);
        showEndGame(gameContext, winner);
    }

    @Override
    public void onActionUndone(@NonNull Game game) {
        runOnUiThread(() -> bdvBoard.setBoard(game.getBoard()));
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();
            switch (request.getRequestType()) {
                case NoTargetExpected: // Only require to choose a result within legalResults
                    break;
                case SelectableCharacterCardExpected:
                    ccsvCardSelector.applyTargets(request.getLegalTargets());
                    break;
                case PlayableCharacterExpected:
                case PositionExpected: {
                    bdvBoard.applyTargets(request.getLegalTargets(), request.getContext(), gameContext.getBoard());
                } break;
                default:
                    throw new IllegalStateException("Unexpected request type: " + request.getRequestType());
            }

            updateInteractionUI(gameContext, request);
        });
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();

            clearInteractionUI(gameContext);

            applyPlayerChange(gameContext);

            applyPhaseChange(phase, true);
        });
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

    //enregion
}