package com.leaders.app.views.replay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.GameActionUtils;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.List;

public class ReplayControlsView extends ConstraintLayout {
    public interface ReplayControlsListener {

        void onReplayLoaded(@NonNull Board board);
        void onActionPlayed(@NonNull IGameAction action, boolean playInReverse, @NonNull Runnable onActionEnd);
    }

    private enum ActionPlayMode {
        Paused,
        Playing,
        SingleAction
    }

    private static final int NO_ACTION_INDEX = -1;

    private final SeekBar skbReplay;
    private final MaterialButton btnPlayPause;
    private final MaterialButton btnNextAction;
    private final MaterialButton btnPreviousAction;

    @NonNull
    private final List<IGameAction> actions;
    private int lastActionIndex;
    private Integer pendingJumpActionIndex;
    private Game game;
    private GameHistory startHistory;

    @NonNull
    private ActionPlayMode playMode;
    private boolean actionIsRunning;


    @Nullable
    private ReplayControlsListener controlsListener;

    public ReplayControlsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        actions = new ArrayList<>();

        inflate(context, R.layout.view_replay_controls, this);

        skbReplay = findViewById(R.id.skbReplay_vwReplayControls);
        btnPlayPause = findViewById(R.id.btnPlayPause_vwReplayControls);
        btnNextAction = findViewById(R.id.btnNextAction_vwReplayControls);
        btnPreviousAction = findViewById(R.id.btnPreviousAction_vwReplayControls);

        initListeners();

        doPause();
        actionIsRunning = false;
        lastActionIndex = NO_ACTION_INDEX;
        pendingJumpActionIndex = null;
    }

    private void initListeners() {
        btnPlayPause.setOnClickListener(this::onPlayPauseClick);
        btnNextAction.setOnClickListener(this::onNextActionClick);
        btnPreviousAction.setOnClickListener(this::onPreviousActionClick);

        skbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    jumpToAction(progress - 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No treatment here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No treatment here
            }
        });
    }

    public void setControlsListener(@Nullable ReplayControlsListener controlsListener) {
        this.controlsListener = controlsListener;
    }

    public void loadReplay(@NonNull ReplaySave replaySave) {
        GameHistory gameHistory = replaySave.getPuzzleGameHistory();

        loadActions(gameHistory);
        // Replays are loaded at the beginning of the game
        startHistory = getStartHistory(gameHistory);
        game = GameFactory.create(startHistory);

        resetReplay();

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required during replay loading");
        }
        controlsListener.onReplayLoaded(game.getBoard());
    }

    private void resetReplay() {
        lastActionIndex = NO_ACTION_INDEX;
        doPause();
        actionIsRunning = false;
        pendingJumpActionIndex = null;
    }

    private void loadActions(@NonNull GameHistory gameHistory) {
        actions.clear();

        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            if (historyEntry instanceof Turn) {
                for (TurnPhase phase : ((Turn) historyEntry).getSubPhasesInOrder()) {
                    addPhaseActions(phase);
                }
            } else if (historyEntry instanceof BanishmentPhase) {
                addPhaseActions((BanishmentPhase) historyEntry);
            }
        }

        skbReplay.setProgress(0);
        skbReplay.setMax(actions.size());
    }

    private void addPhaseActions(IPhase phase) {
        for (IGameAction action : phase.getActions()) {
            if (GameActionUtils.isAnimatable(action)) {
                actions.add(action);
            }
        }
    }

    private GameHistory getStartHistory(@NonNull GameHistory gameHistory) {
        return new GameHistory(gameHistory.getConfig(), new ArrayList<>());
    }

    //region VIEW LISTENER METHODS

    private void onPlayPauseClick(View v) {
        if (playMode == ActionPlayMode.Playing) {
            doPause();
        } else {
            doPlay();
        }
    }

    private void onPreviousActionClick(View v) {
        if (actionIsRunning) {
            return;
        }

        playMode = ActionPlayMode.SingleAction;
        playPreviousAction();
    }

    private void onNextActionClick(View v) {
        if (actionIsRunning) {
            return;
        }

        playMode = ActionPlayMode.SingleAction;
        playNextAction();
    }

    //endregion

    private void doPlay() {
        if (!hasNextAction()) {
            jumpToAction(NO_ACTION_INDEX);
        }

        playMode = ActionPlayMode.Playing;
        playNextAction();
    }

    private void doPause() {
        playMode = ActionPlayMode.Paused;
        updateControlsState();
    }

    private void updateControlsState() {
        int enabledStrokeColor = R.color.font;
        int disabledStrokeColor = R.color.darker_font;
        int enabledBackgroundColor = R.color.ultra_dark_background;
        int disabledBackgroundColor = R.color.darker_background;

        ButtonUtils.setEnabled(btnNextAction, hasNextAction(),
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );
        ButtonUtils.setEnabled(btnPreviousAction, hasPreviousAction(),
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );


        int playPauseResId;
        if (playMode == ActionPlayMode.Playing) {
            playPauseResId = R.drawable.icon_pause;
        } else if (hasNextAction()) {
            playPauseResId = R.drawable.icon_play;
        } else {
            playPauseResId = R.drawable.icon_restart;
        }
        btnPlayPause.setIconResource(playPauseResId);
    }

    private boolean hasNextAction() {
        return lastActionIndex < actions.size() - 1;
    }

    private boolean hasPreviousAction() {
        return lastActionIndex >= 0;
    }

    private void doOnActionEnd() {
        if (!actionIsRunning) {
            return;
        }

        actionIsRunning = false;

        if (pendingJumpActionIndex != null) {
            int jumpActionIndex = pendingJumpActionIndex;
            pendingJumpActionIndex = null;

            doPause();
            doJumpToAction(jumpActionIndex);
            return;
        }

        if (playMode == ActionPlayMode.Playing) {
            playNextAction();
        } else if (playMode == ActionPlayMode.SingleAction) {
            doPause();
        }
    }

    //region ACTION PLAYER METHODS

    private void playNextAction() {
        if (actionIsRunning) {
            return;
        }

        if (!hasNextAction()) {
            doPause();
            return;
        }
        int nextActionIndex = lastActionIndex + 1;

        actionIsRunning = true;

        IGameAction actionToPlay = actions.get(nextActionIndex);
        GameActionHandlerFactory.create(game, actionToPlay).doAction();
        setLastActionIndex(nextActionIndex);

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToPlay, false, this::doOnActionEnd);
    }

    private void playPreviousAction() {
        if (actionIsRunning) {
            return;
        }

        if (!hasPreviousAction()) {
            doPause();
            return;
        }

        int previousActionIndex = lastActionIndex - 1;

        actionIsRunning = true;

        IGameAction actionToReverse = actions.get(lastActionIndex);
        GameActionHandlerFactory.create(game, actionToReverse).undoAction();
        setLastActionIndex(previousActionIndex);

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToReverse, true, this::doOnActionEnd);
    }

    private void setLastActionIndex(int lastActionIndex) {
        this.lastActionIndex = lastActionIndex;
        skbReplay.setProgress(lastActionIndex + 1);

        updateControlsState();
    }

    private void jumpToAction(int jumpActionIndex) {
        if (actionIsRunning) {
            pendingJumpActionIndex = jumpActionIndex;
            return;
        }

        doJumpToAction(jumpActionIndex);
    }

    private void doJumpToAction(int jumpActionIndex) {
        if (jumpActionIndex == lastActionIndex) {
            return;
        }

        Game jumpGame = GameFactory.create(startHistory);

        if (jumpActionIndex != NO_ACTION_INDEX) {
            for (int i = 0; i <= jumpActionIndex; i++) {
                IGameAction action = actions.get(i);
                GameActionHandler handler = GameActionHandlerFactory.create(jumpGame, action);
                handler.doAction();
            }
        }

        game = jumpGame;
        setLastActionIndex(jumpActionIndex);

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required during replay jump");
        }

        controlsListener.onReplayLoaded(game.getBoard());
    }

    //endregion
}
