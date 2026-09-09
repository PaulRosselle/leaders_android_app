package com.leaders.app.views.replay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.entities.replay.ReplayTimelineController;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReplayControlsView extends ConstraintLayout {
    public interface ReplayControlsListener {

        void onReplayLoaded(@NonNull Game game);
        void onActionPlayed(@NonNull IGameAction action, boolean playInReverse, @NonNull Runnable onActionEnd);
    }

    private enum ActionPlayMode {
        Paused,
        Playing,
        SingleAction,
        SingleTurn
    }

    private enum ActionPlayDirection {
        Forward,
        Backward
    }

    private final SeekBar skbReplay;
    private final MaterialButton btnPlayPause;
    private final MaterialButton btnNextAction;
    private final MaterialButton btnPreviousAction;
    private final MaterialButton btnNextTurn;
    private final MaterialButton btnPreviousTurn;

    private ReplayTimelineController timelineController;

    private Game game;
    private GameHistory startHistory;

    @Nullable
    private Integer pendingJumpActionIndex;
    @NonNull
    private ActionPlayMode playMode;
    @Nullable
    private ActionPlayDirection playDirection;
    private boolean actionInProgress;


    @Nullable
    private ReplayControlsListener controlsListener;

    public ReplayControlsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_replay_controls, this);

        skbReplay = findViewById(R.id.skbReplay_vwReplayControls);
        btnPlayPause = findViewById(R.id.btnPlayPause_vwReplayControls);
        btnNextAction = findViewById(R.id.btnNextAction_vwReplayControls);
        btnPreviousAction = findViewById(R.id.btnPreviousAction_vwReplayControls);
        btnNextTurn = findViewById(R.id.btnNextTurn_vwReplayControls);
        btnPreviousTurn = findViewById(R.id.btnPreviousTurn_vwReplayControls);

        initListeners();

        playMode = ActionPlayMode.Paused;
        playDirection = null;
        actionInProgress = false;
        pendingJumpActionIndex = null;
    }

    private void initListeners() {
        btnPlayPause.setOnClickListener(this::onPlayPauseClick);
        btnNextAction.setOnClickListener(this::onNextActionClick);
        btnPreviousAction.setOnClickListener(this::onPreviousActionClick);
        btnNextTurn.setOnClickListener(this::onNextTurnClick);
        btnPreviousTurn.setOnClickListener(this::onPreviousTurnClick);

        btnNextAction.setOnLongClickListener(this::onButtonLongClick);
        btnPreviousAction.setOnLongClickListener(this::onButtonLongClick);
        btnNextTurn.setOnLongClickListener(this::onButtonLongClick);
        btnPreviousTurn.setOnLongClickListener(this::onButtonLongClick);

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


    @NonNull
    private ReplayTimelineController getTimelineController() {
        if (timelineController == null) {
            throw new IllegalStateException("No replay loaded");
        }

        return timelineController;
    }

    @NonNull
    private Game getGame() {
        if (game == null) {
            throw new IllegalStateException("No replay loaded");
        }

        return game;
    }

    @NonNull
    private GameHistory getStartHistory() {
        if (startHistory == null) {
            throw new IllegalStateException("No replay loaded");
        }

        return startHistory;
    }

    public Game getReplayGame() {
        return getGame();
    }

    public void loadReplay(@NonNull ReplaySave replaySave) {
        GameHistory gameHistory = replaySave.getReplayGameHistory();

        timelineController = new ReplayTimelineController(gameHistory);

        startHistory = getStartHistory(gameHistory);
        game = GameFactory.create(startHistory);

        resetReplay();

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required during replay loading");
        }
        controlsListener.onReplayLoaded(getReplayGame());
    }

    private void resetReplay() {
        getTimelineController().reset();

        playMode = ActionPlayMode.Paused;
        playDirection = null;
        actionInProgress = false;
        pendingJumpActionIndex = null;

        skbReplay.setProgress(0);
        skbReplay.setMax(timelineController.getActionCount());

        updateControlsState();
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
        playMode = ActionPlayMode.SingleAction;
        playPreviousAction();
    }

    private void onNextActionClick(View v) {
        playMode = ActionPlayMode.SingleAction;
        if (!actionInProgress) {
            playNextAction();
        } else {
            playDirection = ActionPlayDirection.Forward;
        }
    }

    private void onPreviousTurnClick(View v) {
        playMode = ActionPlayMode.SingleTurn;
        if (!actionInProgress) {
            playPreviousAction();
        } else {
            playDirection = ActionPlayDirection.Backward;
        }
    }

    private void onNextTurnClick(View v) {
        playMode = ActionPlayMode.SingleTurn;
        playNextAction();
    }

    private boolean onButtonLongClick(View v) {
        Map<View, Integer> buttonToasts = new HashMap<>();

        buttonToasts.put(btnPreviousAction, R.string.rewind_previous_action);
        buttonToasts.put(btnPreviousTurn, R.string.rewind_until_previous_turn);
        buttonToasts.put(btnNextAction, R.string.play_next_action);
        buttonToasts.put(btnNextTurn, R.string.play_until_next_turn);

        int buttonToast = Objects.requireNonNull(buttonToasts.get(v), "Button not found");

        Toast.makeText(getContext(), buttonToast, Toast.LENGTH_SHORT).show();

        return true;
    }

    //endregion

    private void doPlay() {
        ReplayTimelineController controller = getTimelineController();

        if (!controller.hasNextAction()) {
            doJumpToAction(ReplayTimelineController.START_INDEX);
        }

        playMode = ActionPlayMode.Playing;
        playDirection = ActionPlayDirection.Forward;

        playNextAction();
    }

    public void doPause() {
        playMode = ActionPlayMode.Paused;
        playDirection = null;

        updateControlsState();
    }

    private void updateControlsState() {
        ReplayTimelineController controller = getTimelineController();

        boolean hasNextAction = controller.hasNextAction();
        boolean hasPreviousAction = controller.hasPreviousAction();

        int enabledStrokeColor = R.color.font;
        int disabledStrokeColor = R.color.darker_font;
        int enabledBackgroundColor = R.color.ultra_dark_background;
        int disabledBackgroundColor = R.color.darker_background;

        ButtonUtils.setEnabled(btnNextAction, hasNextAction,
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );
        ButtonUtils.setEnabled(btnPreviousAction, hasPreviousAction,
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );
        ButtonUtils.setEnabled(btnNextTurn, hasNextAction,
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );
        ButtonUtils.setEnabled(btnPreviousTurn, hasPreviousAction,
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );


        int playPauseResId;
        if (playMode == ActionPlayMode.Playing) {
            playPauseResId = R.drawable.icon_pause;
        } else if (hasNextAction) {
            playPauseResId = R.drawable.icon_play;
        } else {
            playPauseResId = R.drawable.icon_restart;
        }
        btnPlayPause.setIconResource(playPauseResId);
    }

    private void notifyTimelinePositionChanged() {
        ReplayTimelineController controller = getTimelineController();

        skbReplay.setProgress(controller.getCurrentActionIndex() + 1);

        updateControlsState();
    }

    private void doOnActionEnd() {
        if (!actionInProgress) {
            return;
        }

        actionInProgress = false;

        if (pendingJumpActionIndex != null) {
            int jumpActionIndex = pendingJumpActionIndex;
            pendingJumpActionIndex = null;

            doPause();
            doJumpToAction(jumpActionIndex);
            return;
        }

        ReplayTimelineController controller = getTimelineController();

        boolean mustPause;
        boolean keepPlaying;

        boolean playForward = playDirection == ActionPlayDirection.Forward;
        if (playMode == ActionPlayMode.SingleTurn) {
            mustPause = playForward ? controller.isAtTurnEnd() : controller.isAtTurnStart();
            keepPlaying = !mustPause;
        } else {
            keepPlaying = playMode == ActionPlayMode.Playing;
            mustPause = !keepPlaying && playMode == ActionPlayMode.SingleAction;
        }

        if (keepPlaying) {
            if (playForward) {
                playNextAction();
            } else {
                playPreviousAction();
            }
        } else if (mustPause) {
            doPause();
        }
    }

    //region ACTION PLAYER METHODS

    private void playNextAction() {
        if (actionInProgress) {
            return;
        }

        ReplayTimelineController controller = getTimelineController();

        if (!controller.hasNextAction()) {
            doPause();
            return;
        }

        actionInProgress = true;
        playDirection = ActionPlayDirection.Forward;

        IGameAction actionToPlay = controller.moveToNextAction();
        GameActionHandlerFactory.create(game, actionToPlay).doAction();

        for (WarningAction warningAction : controller.getWarningActions()) {
            GameActionHandlerFactory.create(game, warningAction).doAction();
        }

        notifyTimelinePositionChanged();

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToPlay, false, this::doOnActionEnd);
    }

    private void playPreviousAction() {
        if (actionInProgress) {
            return;
        }

        ReplayTimelineController controller = getTimelineController();

        if (!controller.hasPreviousAction()) {
            doPause();
            return;
        }

        actionInProgress = true;
        playDirection = ActionPlayDirection.Backward;

        // We undo warning actions before moving to the previous one
        for (WarningAction warningAction : controller.getWarningActions()) {
            GameActionHandlerFactory.create(game, warningAction).undoAction();
        }

        IGameAction actionToReverse = controller.moveToPreviousAction();
        GameActionHandlerFactory.create(game, actionToReverse).undoAction();

        notifyTimelinePositionChanged();

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToReverse, true, this::doOnActionEnd);
    }

    private void jumpToAction(int jumpActionIndex) {
        if (actionInProgress) {
            pendingJumpActionIndex = jumpActionIndex;
            return;
        }

        doJumpToAction(jumpActionIndex);
    }

    private void doJumpToAction(int jumpActionIndex) {
        ReplayTimelineController controller = getTimelineController();

        if (jumpActionIndex == controller.getCurrentActionIndex()) {
            return;
        }

        Game jumpGame = GameFactory.create(getStartHistory());

        for (int i = 0; i <= jumpActionIndex; i++) {
            IGameAction action = controller.getAction(i);
            GameActionHandlerFactory.create(jumpGame, action).doAction();

            List<WarningAction> warningActions = controller.getWarningActions(i);
            for (WarningAction warningAction : warningActions) {
                GameActionHandlerFactory.create(jumpGame, warningAction).doAction();
            }
        }

        game = jumpGame;

        controller.jumpTo(jumpActionIndex);
        notifyTimelinePositionChanged();

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required during replay jump");
        }

        controlsListener.onReplayLoaded(getReplayGame());
    }

    //endregion
}
