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
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.GameActionUtils;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReplayControlsView extends ConstraintLayout {
    public interface ReplayControlsListener {

        void onReplayLoaded(@NonNull Board board);
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

    private static final int NO_ACTION_INDEX = -1;

    private final SeekBar skbReplay;
    private final MaterialButton btnPlayPause;
    private final MaterialButton btnNextAction;
    private final MaterialButton btnPreviousAction;
    private final MaterialButton btnNextTurn;
    private final MaterialButton btnPreviousTurn;

    @NonNull
    private final List<IGameAction> actions;
    private final List<TeamColor> actionsTeamColors;
    private int lastActionIndex;
    private Integer pendingJumpActionIndex;
    private Game game;
    private GameHistory startHistory;

    @NonNull
    private ActionPlayMode playMode;
    private ActionPlayDirection playDirection;
    private boolean actionInProgress;


    @Nullable
    private ReplayControlsListener controlsListener;

    public ReplayControlsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        actions = new ArrayList<>();
        actionsTeamColors = new ArrayList<>();

        inflate(context, R.layout.view_replay_controls, this);

        skbReplay = findViewById(R.id.skbReplay_vwReplayControls);
        btnPlayPause = findViewById(R.id.btnPlayPause_vwReplayControls);
        btnNextAction = findViewById(R.id.btnNextAction_vwReplayControls);
        btnPreviousAction = findViewById(R.id.btnPreviousAction_vwReplayControls);
        btnNextTurn = findViewById(R.id.btnNextTurn_vwReplayControls);
        btnPreviousTurn = findViewById(R.id.btnPreviousTurn_vwReplayControls);

        initListeners();

        doPause();
        actionInProgress = false;
        lastActionIndex = NO_ACTION_INDEX;
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

    public void loadReplay(@NonNull ReplaySave replaySave) {
        GameHistory gameHistory = replaySave.getReplayGameHistory();

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

    public Game getReplayGame() {
        return new Game(game);
    }

    private void resetReplay() {
        lastActionIndex = NO_ACTION_INDEX;
        doPause();
        actionInProgress = false;
        pendingJumpActionIndex = null;
    }

    private void loadActions(@NonNull GameHistory gameHistory) {
        actions.clear();
        actionsTeamColors.clear();

        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            if (historyEntry instanceof Turn) {
                for (TurnPhase phase : ((Turn) historyEntry).getSubPhasesInOrder()) {
                    addPhaseActions(phase, historyEntry.getTeamColor());
                }
            } else if (historyEntry instanceof BanishmentPhase) {
                addPhaseActions((BanishmentPhase) historyEntry, historyEntry.getTeamColor());
            }
        }

        skbReplay.setProgress(0);
        skbReplay.setMax(actions.size());
    }

    private void addPhaseActions(@NonNull IPhase phase, @NonNull TeamColor teamColor) {
        for (IGameAction action : phase.getActions()) {
            if (GameActionUtils.isAnimatable(action)) {
                actions.add(action);
                actionsTeamColors.add(teamColor);
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
        if (!hasNextAction()) {
            jumpToAction(NO_ACTION_INDEX);
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
        ButtonUtils.setEnabled(btnNextTurn, hasNextAction(),
                enabledStrokeColor, disabledStrokeColor,
                enabledBackgroundColor, disabledBackgroundColor
        );
        ButtonUtils.setEnabled(btnPreviousTurn, hasPreviousAction(),
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

    private boolean isTurnEnd() {
        return !hasNextAction() ||
                (lastActionIndex == NO_ACTION_INDEX ||
                        actionsTeamColors.get(lastActionIndex) != actionsTeamColors.get(lastActionIndex + 1));
    }

    private boolean isTurnStart() {
        return !hasPreviousAction() ||
                actionsTeamColors.get(lastActionIndex) != actionsTeamColors.get(lastActionIndex - 1);
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

        boolean keepPlaying;
        boolean mustPause;

        boolean playForward = playDirection == ActionPlayDirection.Forward;
        if (playMode == ActionPlayMode.SingleTurn) {
            keepPlaying = (!playForward && !isTurnStart()) || (playForward && !isTurnEnd());
            mustPause = !keepPlaying;
        } else {
            keepPlaying = playMode == ActionPlayMode.Playing;
            mustPause = playMode == ActionPlayMode.SingleAction;
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

        if (!hasNextAction()) {
            doPause();
            return;
        }
        int nextActionIndex = lastActionIndex + 1;

        actionInProgress = true;
        playDirection = ActionPlayDirection.Forward;

        IGameAction actionToPlay = actions.get(nextActionIndex);
        GameActionHandlerFactory.create(game, actionToPlay).doAction();
        setLastActionIndex(nextActionIndex);

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToPlay, false, this::doOnActionEnd);
    }

    private void playPreviousAction() {
        if (actionInProgress) {
            return;
        }

        if (!hasPreviousAction()) {
            doPause();
            return;
        }

        int previousActionIndex = lastActionIndex - 1;

        actionInProgress = true;
        playDirection = ActionPlayDirection.Backward;

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
        if (actionInProgress) {
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
