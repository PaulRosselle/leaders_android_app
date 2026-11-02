package com.leaders.app.activities.rules.tutorial;

import android.app.AlertDialog;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.FullGameActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.entities.TutorialBot;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.HelpUtils;
import com.leaders.app.utilities.TutorialGameUtils;
import com.leaders.app.views.duel.PlayerHaloView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.rules.TutorialNavigationView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.PlayerWarningState;
import com.leaders.gamelogic.interactions.InteractionRequest;

import java.util.List;

public class RulesTutorialGameActivity extends FullGameActivity implements
        TutorialNavigationView.IRulesNavigation {

    private MaterialButton btnHelp;
    private TutorialNavigationView tnvNavigation;

    private PlayerTopView ptvBot;
    private PlayerHaloView phvBot;

    private MaterialButton btnReset;

    private boolean isGameEnded;
    private GameHistory startHistory;
    private TutorialBot bot;

    //region FULL GAME ACTIVITY METHODS

    @Override
    protected int getMainLayoutId() {
        return R.id.clyMain_actRulesTutorialGame;
    }

    @Override
    protected int getCardSelectorViewId() {
        return R.id.ccsvCardSelector_actRulesTutorialGame;
    }

    @Override
    protected int getBottomPlayerViewId() {
        return R.id.pbvPlayer_actRulesTutorialGame;
    }

    @Override
    protected int getBottomPlayerHaloViewId() {
        return R.id.phvPlayer_actRulesTutorialGame;
    }

    @Override
    protected int getCardsButtonId() {
        return R.id.btnCards_actRulesTutorialGame;
    }

    @Override
    protected int getNextPhaseButtonId() {
        return R.id.btnNextPhase_actRulesTutorialGame;
    }

    @Override
    protected int getNextPhaseHighlightViewId() {
        return R.id.hlvNextPhase_actRulesTutorialGame;
    }

    //endregion

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard.setAlwaysUseDefaultSkins(true);

        btnHelp = findViewById(R.id.btnHelp_actRulesTutorialGame);
        tnvNavigation = findViewById(R.id.tnvNavigation_actRulesTutorialGame);

        ptvBot = findViewById(R.id.ptvBot_actRulesTutorialGame);
        phvBot = findViewById(R.id.phvBot_actRulesTutorialGame);

        btnReset = findViewById(R.id.btnReset_actRulesTutorialGame);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnHelp.setOnClickListener(this::onHelpClick);
        tnvNavigation.setNavigator(this);

        btnReset.setOnClickListener(this::onResetClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        startHistory = TutorialGameUtils.getDefaultHistory(this);
        bot = new TutorialBot();

        // After a destruction by the system, the attempt in progress takes priority over the puzzle start
        GameHistory snapshot = loadGameSnapshot();
        final GameHistory loadedHistory = snapshot != null ? snapshot : new GameHistory(startHistory);

        controller = new GameController(this);
        bdvBoard.post(() -> controller.startGame(loadedHistory));

        if (snapshot == null) {
            showReminderDialog();
        }
    }

    @Override
    protected boolean supportsGameSnapshot() {
        return true;
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
    protected CharacterSkins getCharacterSkins() {
        return new CharacterSkins();
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

    //region FULL GAME ACTIVITY OVERRIDEN METHODS

    @NonNull
    @Override
    protected CharacterSkinType getNewCharacterSkin(@NonNull Character character) {
        // Characters always use their default skin in tutorials
        return CharacterSkinType.Default;
    }

    @Override
    protected boolean canChangeSelectableCardsVisibility(@NonNull GameContext context) {
        return !isBotPlaying(context);
    }

    @Override
    protected void onNextPhaseClick(View v) {
        if (isGameEnded) {
            goToActivity(ActivityType.RulesCharacterMenu);
            return;
        }

        super.onNextPhaseClick(v);
    }

    @Override
    public void onBanishmentCardSelected() {
        throw new IllegalStateException("Banishment is not supported within recruitment tutorial activity");
    }

    @Override
    protected void showEndGame(@NonNull GameContext gameContext, @NonNull Player winner) {
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

    //endregion

    //region UI STATE METHODS

    private void showReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);

        builder.setTitle(R.string.let_the_duel_beggin);
        builder.setMessage(R.string.match_vs_bot_reminder);
        builder.setPositiveButton(R.string.lets_go, null);

        builder.show();
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
        pbvBottomPlayer.setPlayer(player, getPlayerLeaderType(player, board));
        ptvBot.setPlayer(bot, getPlayerLeaderType(bot, board));

        updateCurrentPlayer(isBotPlaying(gameContext));
    }

    @Override
    protected void applyPlayerChange(@NonNull GameContext gameContext) {
        List<PlayerWarningState> warningStates = gameContext.getPlayerWarningStates();

        pbvBottomPlayer.setWarningVisible(playerHasWarnings(getPlayer(gameContext), warningStates));
        ptvBot.setWarningVisible(playerHasWarnings(getBot(gameContext), warningStates));

        updateCurrentPlayer(isBotPlaying(gameContext));
    }

    private void updateCurrentPlayer(boolean isBotPlaying) {
        phvBot.setVisibility(isBotPlaying ? View.VISIBLE : View.GONE);
        phvBottomPlayer.setVisibility(isBotPlaying ? View.INVISIBLE : View.VISIBLE);
    }

    //endregion

    //region GAME CONTROLLER LISTENER METHODS

    @Override
    public void onGameStarted(@NonNull GameContext context) {
        bot.reset();
        super.onGameStarted(context);
    }

    @Override
    protected void onGameStartedUi(@NonNull GameContext context) {
        isGameEnded = false;
        initPlayerViews(context);
    }

    @Override
    protected void onGameEndedUi(@NonNull GameContext context, @NonNull Player winner) {
        isGameEnded = true;
        setBtnNextPhaseEnabled(true, true);
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase, @NonNull GameContext context) {
        bot.reset();
        super.onPhaseChanged(phase, context);
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request, @NonNull GameContext context) {
        if (isBotPlaying(controller.getCurrentContext())) {
            bot.handleRequest(controller, request);
        } else {
            super.onInteractionRequired(request, context);
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

    private void onHelpClick(View v) {
        HelpUtils.getHelpDialog(this).show();
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

    //endregion
}