package com.leaders.app.activities.duel;

import android.animation.LayoutTransition;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.TeamColorUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.replay.ReplaySaveView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.PlayerWarningState;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class DuelPlayerActivity extends PlayableActivity implements
        CharacterCardSelectionView.OnCardSelectedListener {

    private enum DuelAction {
        SaveAsReplay,
        DisplayCellPositions;

        private int getIconResId() {
            switch (this) {
                case SaveAsReplay: return R.drawable.icon_save;
                case DisplayCellPositions: return R.drawable.icon_position;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case SaveAsReplay: return R.string.record_game;
                case DisplayCellPositions: return R.string.board_coordinates;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(@NonNull DuelPlayerActivity activity) {
            switch (this) {
                case SaveAsReplay: return activity::onSaveAsReplay;
                case DisplayCellPositions: return activity::onDisplayCellPosition;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private CharacterCardSelectionView ccsvCardSelector;
    private PlayerBottomView pbvCurrentPlayer;
    private PlayerTopView ptvOpposingPlayer;
    private TextView txvPlayerTurn;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private ReplaySaveView rsvReplaySave;
    private View vwDialogBg;

    private CharacterDisplay chdNewCharacter;

    private MaterialButton btnCards;
    private MaterialButton btnNextPhase;
    private HighlightView hlvNextPhase;
    

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        ccsvCardSelector = findViewById(R.id.ccsvCardSelector_actDuelPlayer);
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);

        pbvCurrentPlayer = findViewById(R.id.pbvCurrentPlayer_actDuelPlayer);
        ptvOpposingPlayer = findViewById(R.id.ptvOpposingPlayer_actDuelPlayer);
        txvPlayerTurn = findViewById(R.id.txvPlayerTurn_actDuelPlayer);

        btnActions = findViewById(R.id.btnActions_actDuelPlayer);
        amvActions = findViewById(R.id.amvActions_actDuelPlayer);
        for (DuelAction action : DuelAction.values()) {
            amvActions.addActionButton(action.getIconResId(), action.getTextResId(),
                    action.ordinal(), action.getOnClickListener(this));
        }
        rsvReplaySave = findViewById(R.id.rsvReplaySave_actDuelPlayer);
        vwDialogBg = findViewById(R.id.vwDialogBg_actDuelPlayer);

        btnCards = findViewById(R.id.btnCards_actDuelPlayer);
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

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

        btnActions.setOnClickListener(this::onActionsClick);
        rsvReplaySave.setOnSaveClick(this::onSaveAsReplayConfirmed);
        rsvReplaySave.setOnCancelClick(this::onSaveAsReplayCancelled);
        vwDialogBg.setOnClickListener(this::vwDialogBgClick);

        btnCards.setOnClickListener(this::onCardsClick);
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
    protected int getBoardViewId() {
        return R.id.bdvBoard_actDuelPlayer;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actDuelPlayer;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actDuelPlayer;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actDuelPlayer;
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

    private void onCardsClick(View v) {
        setCardSelectorVisible(ccsvCardSelector.getVisibility() != View.VISIBLE);
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

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((PortraitView) v).getPortraitCard());
        return true;
    }

    //endregion

    //region ACTIONS METHODS

    private void vwDialogBgClick(View v) {
        if (rsvReplaySave.getVisibility() == View.VISIBLE) {
            setReplaySaveVisible(false);
        }
        if (amvActions.getVisibility() == View.VISIBLE) {
            setActionsMenuVisible(false);
        }
    }

    private void onSaveAsReplay(View v) {
        GameContext gameContext = controller.getCurrentContext();

        rsvReplaySave.setDefaultName(String.format("%s vs %s",
                gameContext.getCurrentPlayer().getName(),
                gameContext.getOpposingPlayer().getName())
        );
        rsvReplaySave.setName("");
        rsvReplaySave.setDate(LocalDate.now());

        amvActions.setVisibility(View.GONE);
        setReplaySaveVisible(true);
    }

    private void onSaveAsReplayCancelled(View v) {
        setReplaySaveVisible(false);
    }

    private void onSaveAsReplayConfirmed(View v) {
        ReplaySave newReplay = new ReplaySave(
                rsvReplaySave.getName(),
                rsvReplaySave.getDate(),
                controller.getHistory()
        );

        List<ReplaySave> replays = JsonUtils.loadReplays(this);
        replays.add(newReplay);
        JsonUtils.saveReplays(this, replays);

        Toast.makeText(this, R.string.saved_replay, Toast.LENGTH_SHORT).show();

        setReplaySaveVisible(false);
    }

    private void onDisplayCellPosition(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());

        setActionsMenuVisible(false);
    }

    private void setActionsMenuVisible(boolean visible) {
        amvActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setReplaySaveVisible(boolean visible) {
        rsvReplaySave.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion

    //region INTERACTION METHODS

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

        String loserName = "";
        for (Player player : List.of(gameContext.getCurrentPlayer(), gameContext.getOpposingPlayer())) {
            if (player.getTeamColor() != winnerColor) {
                loserName = player.getName();
            }
        }

        showEndGame(gameContext,
                winnerColor,
                EndGameType.Victory,
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

    @Override
    protected void highlightPlayableCharacters(@NonNull GameContext gameContext,
                                             @NonNull InteractionRequest request) {
        super.highlightPlayableCharacters(gameContext, request);

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

        bdvBoard.setOrientation(TeamColorUtils.getOrientation(currentPlayer.getTeamColor()));

        List<PlayerWarningState> warningStates = gameContext.getPlayerWarningStates();

        Board board = gameContext.getBoard();
        pbvCurrentPlayer.setPlayer(currentPlayer, getPlayerLeaderType(currentPlayer, board));
        pbvCurrentPlayer.setWarningVisible(playerHasWarnings(currentPlayer, warningStates));

        Player opposingPlayer = gameContext.getOpposingPlayer();

        ptvOpposingPlayer.setPlayer(opposingPlayer, getPlayerLeaderType(opposingPlayer, board));
        ptvOpposingPlayer.setWarningVisible(playerHasWarnings(opposingPlayer, warningStates));
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

    private boolean playerHasWarnings(@NonNull Player player,
                                      @NonNull List<PlayerWarningState> warningStates) {
        for (PlayerWarningState warningState : warningStates) {
            if (warningState.getPlayerTeamColor() == player.getTeamColor() &&
                    warningState.getWarningCount() > 0) {
                return true;
            }
        }

        return false;
    }

    //endregion

    //region INTERACTION METHODS

    @Override
    protected void clearInteractionUI(@NonNull GameContext gameContext) {
        super.clearInteractionUI(gameContext);

        ccsvCardSelector.applyCards(gameContext.getAvailableCharacterCards());

        ccsvCardSelector.setPortraitsVisible(true);
        setNewCharacterVisible(null, false);

        setBtnNextPhaseEnabled(false, false);
    }

    @Override
    protected void applyInteractionTargets(@NonNull GameContext gameContext,
                                           @NonNull InteractionRequest request) {
        switch (request.getRequestType()) {
            case NoTargetExpected:
                clearInteractionUI(gameContext); // Only require to choose a result within legalResults
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
    }

    @Override
    protected void updateInteractionUI(@NonNull GameContext gameContext,
                                     @NonNull InteractionRequest request) {
        super.updateInteractionUI(gameContext, request);

        highlightSelectableCards(gameContext, request);

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
    public void onPhaseChanged(@NonNull GamePhase phase) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();

            clearInteractionUI(gameContext);

            applyPlayerChange(gameContext);

            applyPhaseChange(phase, true);
        });
    }

    //enregion
}