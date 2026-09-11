package com.leaders.app.activities.rules.tutorial;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.TutorialBotUtils;
import com.leaders.app.utilities.TutorialGameUtils;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.app.views.rules.TutorialNavigationView;
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
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.List;
import java.util.Objects;

public class RulesTutorialGameActivity extends PlayableActivity implements
        TutorialNavigationView.IRulesNavigation,
        CharacterCardSelectionView.OnCardSelectedListener {
    private CharacterCardSelectionView ccsvCardSelector;
    private CharacterDisplay chdNewCharacter;

    private MaterialButton btnInfo;
    private TutorialNavigationView tnvNavigation;

    private PlayerBottomView pbvPlayer;
    private PlayerTopView ptvBot;

    private MaterialButton btnReset;
    private MaterialButton btnNextPhase;
    private HighlightView hlvNextPhase;


    private GameHistory startHistory;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        ccsvCardSelector = findViewById(R.id.ccsvCardSelector_actRulesTutorialGame);
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);

        btnInfo = findViewById(R.id.btnInfo_actRulesTutorialGame);
        tnvNavigation = findViewById(R.id.tnvNavigation_actRulesTutorialGame);

        pbvPlayer = findViewById(R.id.pbvPlayer_actRulesTutorialGame);
        ptvBot = findViewById(R.id.ptvBot_actRulesTutorialGame);

        btnReset = findViewById(R.id.btnReset_actRulesTutorialGame);
        btnNextPhase = findViewById(R.id.btnNextPhase_actRulesTutorialGame);
        hlvNextPhase = findViewById(R.id.hlvNextPhase_actRulesTutorialGame);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actRulesTutorialGame)).setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnScrollViewClickListener(this::onNonInteractiveElementClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

        btnInfo.setOnClickListener(this::onInfoClick);
        tnvNavigation.setNavigator(this);

        btnReset.setOnClickListener(this::onResetClick);
        btnNextPhase.setOnClickListener(this::onNextPhaseClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        startHistory = TutorialGameUtils.getDefaultHistory(this);

        controller = new GameController(this);
        controller.restartGame(new GameHistory(startHistory));
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesTutorialGame;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesTutorialGame;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesTutorialGame;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesTutorialGame;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_tutorial_game;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesTutorialGame;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorialGame;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialGame;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesMenu, ActivityTransitionType.SlideLeft);
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

        setBtnNextPhaseEnabled(controller.canEndPhase(), request.getLegalTargets().isEmpty());

        if (request.getRequestType() == InteractionType.PositionExpected &&
                gameContext.getGamePhase().getPhaseType() == GamePhaseType.Recruitment) {
            ccsvCardSelector.setPortraitsVisible(false);
            setNewCharacterVisible(request.getContext().getCharacter(), true);
        }

        applyPlayerChange(gameContext);

        applyPhaseChange(gameContext.getGamePhase(), false);
    }

    public void onRecruitmentCardSelected(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }
    public void onBanishmentCardSelected() {
        throw new IllegalStateException("Banishment is not supported within recruitment tutorial activity");
    }

    @Override
    public void onNotSelectableCardClick() {
        controller.cancelAction();
    }

    //endregion

    //region UI STATE METHODS

    private void setCardSelectorVisible(boolean visible) {
        // When recruiting, we display the cardSelector view below the player view.
        // The layout transition is animated for both the board and player view
        bdvBoard.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        pbvPlayer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvPlayer.getLayoutParams();
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
            playerViewParams.topMargin = (int) (boardHeight - pbvPlayer.getMeasuredHeight() +
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
        pbvPlayer.setLayoutParams(playerViewParams);

        // The requestLayout calls start the layout transition animation
        bdvBoard.requestLayout();
        pbvPlayer.requestLayout();
    }

    private void showEndGame(@NonNull GameContext gameContext, @NonNull Player winner) {
        boolean isVictory = winner.getTeamColor() == TutorialGameUtils.getPlayerTeamColor();
        EndGameType endGameType = isVictory ? EndGameType.Victory : EndGameType.Defeat;

        int titleId = isVictory ? R.string.victory_title : R.string.defeat_title;
        int subtitleId = isVictory ? R.string.victory_subtitle : R.string.defeat_subtitle;

        showEndGame(gameContext,
                winner.getTeamColor(),
                endGameType,
                getString(titleId),
                getString(subtitleId)
        );
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

    private boolean isBotPlaying(@NonNull GameContext gameContext) {
        return gameContext.getCurrentPlayer().getTeamColor() != TutorialGameUtils.getPlayerTeamColor();
    }

    private Player getPlayer(@NonNull GameContext gameContext) {
        return isBotPlaying(gameContext) ? gameContext.getOpposingPlayer() : gameContext.getCurrentPlayer();
    }

    private Player getBot(@NonNull GameContext gameContext) {
        return isBotPlaying(gameContext) ? gameContext.getCurrentPlayer() : gameContext.getOpposingPlayer();
    }

    private void initPlayerViews(@NonNull GameContext gameContext) {
        Player player = getPlayer(gameContext);
        Player bot = getBot(gameContext);

        Board board = gameContext.getBoard();
        pbvPlayer.setPlayer(player, getPlayerLeaderType(player, board));
        ptvBot.setPlayer(bot, getPlayerLeaderType(bot, board));
    }

    private LeaderType getPlayerLeaderType(@NonNull Player player, @NonNull Board board) {
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(board, player.getTeamColor()),
                "No leader found for player: " + player
        );
        return LeaderType.getFromCharacter(leaderCell.getCharacter());
    }

    private void applyPlayerChange(@NonNull GameContext gameContext) {
        List<PlayerWarningState> warningStates = gameContext.getPlayerWarningStates();

        pbvPlayer.setWarningVisible(playerHasWarnings(getPlayer(gameContext), warningStates));
        ptvBot.setWarningVisible(playerHasWarnings(getBot(gameContext), warningStates));
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

    private void applyPhaseChange(@NonNull GamePhase gamePhase,
                                  boolean canChangeSelectableCardsVisibility) {
        boolean lockSelectableCardsView = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;

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

    //region GAME CONTROLLER LISTENER METHODS

    @Override
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();

            initPlayerViews(gameContext);
            bdvBoard.setBoard(game.getBoard());
            ccsvCardSelector.applyGameModeParams(gameContext.getGameMode());

            clearInteractionUI(gameContext);
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        GameContext gameContext = controller.getCurrentContext();

        clearInteractionUI(gameContext);
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

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {
        if (isBotPlaying(controller.getCurrentContext())) {
            TutorialBotUtils.handleRequest(controller, request);
        } else {
            super.onInteractionRequired(request);
        }
    }

    //endregion

    //region CHAPTER NAVIGATION

    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.GameVsBot;
    }

    public void goToChapter(@NonNull TutorialChapter chapter) {
        goToActivity(chapter.getActivityType(), ActivityTransitionType.Fade);
    }

    //endregion

    //region VIEW LISTENER METHODS

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((PortraitView) v).getPortraitCard());
        return true;
    }

    private void onInfoClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);

        builder.setIcon(getChapter().getInfoIconResId());
        builder.setTitle(R.string.extra_infos);
        builder.setMessage(getChapter().getInfoTextResId());
        builder.setPositiveButton(R.string.ok, null);

        builder.show();
    }

    private void onResetClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.new_attempt);
        builder.setMessage(R.string.restart_game_vs_bot);
        builder.setPositiveButton(R.string.start_over, (dialogInterface, i) ->
                controller.restartGame(new GameHistory(startHistory)));
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void onNextPhaseClick(View v) {
        GameContext gameContext = controller.getCurrentContext();

        if (gameContext.getGamePhase().getPhaseType() == GamePhaseType.Banishment) {
            controller.selectTarget(ccsvCardSelector.getSelectedTarget());
        } else {
            controller.endPhase();
        }
    }

    //endregion
}