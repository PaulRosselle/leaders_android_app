package com.leaders.app.activities.duel;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.FullGameActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.entities.CorruptedDataDialog;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.HelpUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.TeamColorUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.replay.ReplaySaveView;
import com.leaders.app.views.settings.AnimationSpeedView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.PlayerWarningState;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;

public final class DuelPlayerActivity extends FullGameActivity {
    private enum DuelAction {
        Exit,
        SaveAsReplay,
        ChangeAnimationSpeed,
        AnimatePlayableItems,
        DisplayCellPositions,
        AutoRotation,
        Help;

        private int getIconResId() {
            switch (this) {
                case Exit: return R.drawable.icon_exit;
                case SaveAsReplay: return R.drawable.icon_save;
                case ChangeAnimationSpeed: return R.drawable.icon_speed;
                case AnimatePlayableItems: return R.drawable.icon_sparkles;
                case DisplayCellPositions: return R.drawable.icon_position;
                case AutoRotation: return R.drawable.icon_swap;
                case Help: return R.drawable.icon_question;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case Exit: return R.string.exit;
                case SaveAsReplay: return R.string.record_game;
                case ChangeAnimationSpeed: return R.string.animation_speed;
                case AnimatePlayableItems: return R.string.playable_items_animation;
                case DisplayCellPositions: return R.string.board_coordinates;
                case AutoRotation: return R.string.screen_auto_rotation;
                case Help: return R.string.help;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(@NonNull DuelPlayerActivity activity) {
            switch (this) {
                case Exit: return activity::onExitClick;
                case SaveAsReplay: return activity::onSaveAsReplay;
                case ChangeAnimationSpeed: return activity::onChangeAnimationSpeedClick;
                case AnimatePlayableItems: return activity::onAnimatePlayableItems;
                case DisplayCellPositions: return activity::onDisplayCellPosition;
                case AutoRotation: return activity::onAutoRotation;
                case Help: return activity::onHelpClick;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private static final String STATE_AUTO_ROTATE = "DUEL_PLAYER.AUTO_ROTATE";

    private ConstraintLayout clyMain;
    private Guideline gdlTop;
    private Guideline gdlBottom;
    private int statusBarHeight;
    private boolean autoRotateEnabled;

    private PlayerTopView ptvOpposingPlayer;
    private TextView txvPlayerTurn;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private AnimationSpeedView asvAnimationSpeed;
    private ReplaySaveView rsvReplaySave;
    private View vwDialogBg;

    private CharacterSkins characterSkins;

    //region FULL GAME ACTIVITY METHODS

    @Override
    protected int getMainLayoutId() {
        return R.id.clyMain_actDuelPlayer;
    }

    @Override
    protected int getCardSelectorViewId() {
        return R.id.ccsvCardSelector_actDuelPlayer;
    }

    @Override
    protected int getBottomPlayerViewId() {
        return R.id.pbvCurrentPlayer_actDuelPlayer;
    }

    @Override
    protected int getBottomPlayerHaloViewId() {
        return R.id.phvCurrentPlayer_actDuelPlayer;
    }

    @Override
    protected int getCardsButtonId() {
        return R.id.btnCards_actDuelPlayer;
    }

    @Override
    protected int getNextPhaseButtonId() {
        return R.id.btnNextPhase_actDuelPlayer;
    }

    @Override
    protected int getNextPhaseHighlightViewId() {
        return R.id.hlvNextPhase_actDuelPlayer;
    }

    //endregion

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        clyMain = findViewById(R.id.clyMain_actDuelPlayer);
        gdlTop = findViewById(R.id.gdlRoot_actDuelPlayer);
        gdlBottom = findViewById(R.id.gdlBottom_actDuelPlayer);

        ptvOpposingPlayer = findViewById(R.id.ptvOpposingPlayer_actDuelPlayer);
        txvPlayerTurn = findViewById(R.id.txvPlayerTurn_actDuelPlayer);

        btnActions = findViewById(R.id.btnActions_actDuelPlayer);
        amvActions = findViewById(R.id.amvActions_actDuelPlayer);
        for (DuelAction action : DuelAction.values()) {
            amvActions.addActionButton(action.getIconResId(), action.getTextResId(),
                    action.ordinal(), action.getOnClickListener(this));
        }
        asvAnimationSpeed = findViewById(R.id.asvAnimationSpeed_actDuelPlayer);
        rsvReplaySave = findViewById(R.id.rsvReplaySave_actDuelPlayer);
        vwDialogBg = findViewById(R.id.vwDialogBg_actDuelPlayer);

        ViewCompat.setOnApplyWindowInsetsListener(gdlBottom, (v, insets) -> {
            statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            return insets;
        });
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnActions.setOnClickListener(this::onActionsClick);
        rsvReplaySave.setOnSaveClick(this::onSaveAsReplayConfirmed);
        rsvReplaySave.setOnCancelClick(this::onSaveAsReplayCancelled);
        vwDialogBg.setOnClickListener(this::vwDialogBgClick);
        asvAnimationSpeed.setChangeListener(this::onAnimationSpeedChange);
        asvAnimationSpeed.setOnClickListener(this::onAsvAnimationBgClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        Settings settings = ((LeadersApplication) getApplication()).getSettings();
        characterSkins = settings.getCharacterSkins();
        cnvCardInfo.setCharacterSkins(characterSkins);
        ccsvCardSelector.setCharacterSkins(characterSkins);
        autoRotateEnabled = settings.duelScreenAutoRotation();

        // The options changed during the previous session take priority over the settings
        Bundle savedState = getSavedState();
        if (savedState != null) {
            autoRotateEnabled = savedState.getBoolean(STATE_AUTO_ROTATE, autoRotateEnabled);
        }

        updateAnimatePlayableIcon();
        updateAutoRotationIcon();
        updateCellPositionIcon();

        // After a destruction by the system, the game in progress takes priority over the intent
        GameHistory snapshot = loadGameSnapshot();
        final GameHistory loadedHistory = snapshot != null ? snapshot : getInitialGameHistory();

        controller = new GameController(this);
        bdvBoard.post(() -> controller.startGame(loadedHistory));
    }

    @NonNull
    private GameHistory getInitialGameHistory() {
        String gameDatas = getIntent().getStringExtra(ExtraUtils.EXTRA_DUEL_GAME_DATAS);
        if (gameDatas == null || gameDatas.isEmpty()) {
            throw new IllegalStateException("Invalid duel game datas: missing datas");
        }

        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            JSONObject joGameDatas = new JSONObject(gameDatas);
            return serializer.getFromJson(joGameDatas, new SerializationContext());
        } catch (JSONException e) {
            throw new IllegalStateException("Invalid duel game datas: invalid datas", e);
        }
    }

    @Override
    protected boolean supportsGameSnapshot() {
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_AUTO_ROTATE, autoRotateEnabled);
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
    protected CharacterSkins getCharacterSkins() {
        return characterSkins;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_duel_player;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actDuelPlayer;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return null;
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

    //region ACTIONS METHODS

    private void onActionsClick(View v) {
        setActionsMenuVisible(true);
    }

    private void vwDialogBgClick(View v) {
        if (rsvReplaySave.getVisibility() == View.VISIBLE) {
            setReplaySaveVisible(false);
        }
        if (asvAnimationSpeed.getVisibility() == View.VISIBLE) {
            setAnimationSpeedVisible(false);
        }
        if (amvActions.getVisibility() == View.VISIBLE) {
            setActionsMenuVisible(false);
        }
    }

    private void onAsvAnimationBgClick(View v) {
        // Dummy on click listener to prevent a "onDialogBgClick"
    }

    private void onAnimationSpeedChange(@NonNull AnimationSpeed speed) {
        animationSpeed = speed;
    }

    private void onExitClick(View v) {
        doOnBackPressed();
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
        saveReplay(new ReplaySave(
                rsvReplaySave.getName(),
                rsvReplaySave.getDate(),
                controller.getHistory()
        ));
    }

    private void saveReplay(@NonNull ReplaySave newReplay) {
        List<ReplaySave> replays;
        try {
            replays = JsonUtils.loadReplays(this);
        } catch (JsonUtils.CorruptedFileException e) {
            CorruptedDataDialog.show(this,
                    () -> saveReplay(newReplay),
                    () -> {
                        JsonUtils.discardCorruptedFile(this, e);
                        saveReplay(newReplay);
                    },
                    () -> { }); // Cancellation, the save form stays open
            return;
        }

        replays.add(newReplay);
        JsonUtils.saveReplays(this, replays);

        Toast.makeText(this, R.string.saved_replay, Toast.LENGTH_SHORT).show();
        setReplaySaveVisible(false);
    }

    private void onChangeAnimationSpeedClick(View v) {
        amvActions.setVisibility(View.GONE);
        asvAnimationSpeed.setSpeed(animationSpeed);
        setAnimationSpeedVisible(true);
    }

    private void onAnimatePlayableItems(View v) {
        if (animatePlayableItems) {
            bdvBoard.stopPlayableCharactersShineAnimation();
            bdvBoard.stopRecruitmentCellsAnimation();
            ccsvCardSelector.stopShineAnimation();
        } else {
            if (bdvBoard.canShinePlayableCharacters()) {
                bdvBoard.startPlayableCharactersShineAnimation();
            }
            if (bdvBoard.canShineRecruitmentCells()) {
                bdvBoard.startRecruitmentCellsAnimation();
            }
            if (ccsvCardSelector.hasTargets()) {
                ccsvCardSelector.startShineAnimation();
            }
        }

        animatePlayableItems = !animatePlayableItems;
        updateAnimatePlayableIcon();

        setActionsMenuVisible(false);
    }

    private void updateAnimatePlayableIcon() {
        amvActions.setButtonIcon(
                DuelAction.AnimatePlayableItems.ordinal(),
                animatePlayableItems ? R.drawable.icon_sparkles_off : R.drawable.icon_sparkles
        );
    }

    private void onDisplayCellPosition(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());
        updateCellPositionIcon();

        setActionsMenuVisible(false);
    }

    private void updateCellPositionIcon() {
        amvActions.setButtonIcon(
                DuelAction.DisplayCellPositions.ordinal(),
                bdvBoard.isCellPositionVisible() ? R.drawable.icon_position_off : R.drawable.icon_position
        );
    }

    private void updateAutoRotationIcon() {
        amvActions.setButtonIcon(
                DuelAction.AutoRotation.ordinal(),
                autoRotateEnabled ? R.drawable.icon_swap_off : R.drawable.icon_swap
        );
    }

    private void onAutoRotation(View v) {
        autoRotateEnabled = !autoRotateEnabled;
        updateAutoRotationIcon();
        applyScreenRotation();

        setActionsMenuVisible(false);
    }

    private void onHelpClick(View v) {
        HelpUtils.getHelpDialog(this).show();
        setActionsMenuVisible(false);
    }

    private void setAnimationSpeedVisible(boolean visible) {
        asvAnimationSpeed.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
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

    @Override
    public void onBanishmentCardSelected() {
        if (!btnNextPhase.isEnabled()) {
            setBtnNextPhaseEnabled(true, true);
        }
    }

    //endregion

    //region UI STATE METHODS

    @Override
    protected void showEndGame(@NonNull GameContext gameContext, @NonNull Player winner) {
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

    @Override
    protected void applyPlayerChange(@NonNull GameContext gameContext) {
        Player currentPlayer = gameContext.getCurrentPlayer();

        bdvBoard.setOrientation(TeamColorUtils.getOrientation(currentPlayer.getTeamColor()));
        applyScreenRotation();

        List<PlayerWarningState> warningStates = gameContext.getPlayerWarningStates();

        Board board = gameContext.getBoard();
        pbvBottomPlayer.setPlayer(currentPlayer, getPlayerLeaderType(currentPlayer, board));
        pbvBottomPlayer.setWarningVisible(playerHasWarnings(currentPlayer, warningStates));

        Player opposingPlayer = gameContext.getOpposingPlayer();

        ptvOpposingPlayer.setPlayer(opposingPlayer, getPlayerLeaderType(opposingPlayer, board));
        ptvOpposingPlayer.setWarningVisible(playerHasWarnings(opposingPlayer, warningStates));
    }

    private void applyScreenRotation() {
        boolean rotate = autoRotateEnabled &&
                bdvBoard.getOrientation() == BoardOrientation.Rotated &&
                controller.isGameRunning();

        clyMain.setRotation(rotate ? 180f : 0f);
        int defaultMargin = getResources().getDimensionPixelSize(R.dimen.default_layout_margin);
        gdlTop.setGuidelineBegin(rotate ? defaultMargin : statusBarHeight);
        gdlBottom.setGuidelineEnd(rotate ? statusBarHeight : defaultMargin);
    }

    private boolean isPlayerNameFirstCharVowel(@NonNull String playerName) {
        final String vowels = "aeiouAEIOU";
        return !playerName.isEmpty() && (vowels.indexOf(playerName.charAt(0)) != -1);
    }

    @Override
    protected void applyPhaseChange(@NonNull GamePhase gamePhase,
                                    boolean canChangeSelectableCardsVisibility) {
        String playerName = gamePhase.getPhasePlayer().getName();
        int playerTurnFormat = isPlayerNameFirstCharVowel(playerName) ?
                R.string.player_turn_vowel : R.string.player_turn_consonant;
        txvPlayerTurn.setText(String.format(getString(playerTurnFormat), playerName));

        super.applyPhaseChange(gamePhase, canChangeSelectableCardsVisibility);
    }

    //endregion

    //region GAME CONTROLLER METHODS

    @Override
    protected void onGameEndedUi(@NonNull GameContext context, @NonNull Player winner) {
        applyScreenRotation();
    }

    //endregion
}