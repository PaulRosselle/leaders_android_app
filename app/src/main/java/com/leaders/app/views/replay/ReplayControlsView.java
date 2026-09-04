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
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
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
        void onActionPlayed(@NonNull IGameAction action, @NonNull Runnable onActionEnd);
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
    private Game game;

    @NonNull
    private ActionPlayMode playMode;
    private boolean waitingForActionEnd;


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
        waitingForActionEnd = false;
    }

    private void initListeners() {
        btnPlayPause.setOnClickListener(this::onPlayPauseClick);
        btnNextAction.setOnClickListener(this::onNextActionClick);
        btnPreviousAction.setOnClickListener(this::onPreviousActionClick);
        // TODO - handle seekbar listener
    }

    public void loadReplay(@NonNull ReplaySave replaySave) {
        GameHistory gameHistory = replaySave.getPuzzleGameHistory();

        loadActions(gameHistory);
        // Replays are loaded at the beginning of the game
        GameHistory startHistory = getStartHistory(gameHistory);
        game = GameFactory.create(startHistory);

        playMode = ActionPlayMode.Paused;
        lastActionIndex = NO_ACTION_INDEX;
        waitingForActionEnd = false;


        if (controlsListener == null) {
            throw new IllegalStateException("Listener required during replay loading");
        }
        controlsListener.onReplayLoaded(game.getBoard());
    }

    public void setControlsListener(@Nullable ReplayControlsListener controlsListener) {
        this.controlsListener = controlsListener;
    }

    private void loadActions(@NonNull GameHistory gameHistory) {
        actions.clear();

        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            if (historyEntry instanceof Turn) {
                for (TurnPhase phase : ((Turn) historyEntry).getSubPhasesInOrder()) {
                    addPhaseActions(phase);
                }
            } else if (historyEntry instanceof BanishmentPhase) {
                addPhaseActions(((BanishmentPhase) historyEntry));
            }
        }

        skbReplay.setProgress(0);
        skbReplay.setMax(actions.size());
    }

    private void addPhaseActions(IPhase phase) {
        for (IGameAction action : phase.getActions()) {
            if (isPlayable(action)) {
                actions.add(action);
            }
        }
    }

    private boolean isPlayable(IGameAction action) {
        return List.of(
                GameActionType.CharacterAction,
                GameActionType.Recruitment
        ).contains(action.getActionType());
    }

    private GameHistory getStartHistory(@NonNull GameHistory gameHistory) {
        return new GameHistory(gameHistory.getConfig(), new ArrayList<>());
    }


    private void onPlayPauseClick(View v) {
        if (playMode == ActionPlayMode.Playing) {
            doPause();
        } else {
            doPlay();
        }
    }

    private void doPlay() {
        playMode = ActionPlayMode.Playing;
        btnPlayPause.setIconResource(R.drawable.icon_pause);

        playNextAction();
    }

    // TODO - make public ? Can be useful
    private void doPause() {
        playMode = ActionPlayMode.Paused;
        btnPlayPause.setIconResource(R.drawable.icon_play);
    }

    private void onPreviousActionClick(View v) {
        if (waitingForActionEnd) {
            return;
        }

        // TODO - play only the previous action
    }

    private void onNextActionClick(View v) {
        if (waitingForActionEnd) {
            return;
        }

        // TODO - play only the next action
    }

    private void doOnActionEnd() {
        if (!waitingForActionEnd) {
            return;
        }

        waitingForActionEnd = false;

        if (playMode == ActionPlayMode.Playing) {
            playNextAction();
        } else if (playMode == ActionPlayMode.SingleAction) {
            doPause();
        }
    }

    private int getNextPlayableActionIndex() {
        int index = lastActionIndex + 1;

        while (index < actions.size()) {
            IGameAction action = actions.get(index);

            if (isPlayable(action)) {
                return index;
            }

            index++;
        }

        return NO_ACTION_INDEX;
    }

    private void playNextAction() {
        if (waitingForActionEnd) {
            return;
        }

        waitingForActionEnd = true;

        int nextPlayableActionIndex = getNextPlayableActionIndex();
        if (nextPlayableActionIndex == NO_ACTION_INDEX) {
            doPause();
            return;
        }

        IGameAction actionToPlay = actions.get(nextPlayableActionIndex);
        GameActionHandlerFactory.create(game, actionToPlay).doAction();
        lastActionIndex = nextPlayableActionIndex;
        skbReplay.setProgress(lastActionIndex + 1);

        if (controlsListener == null) {
            throw new IllegalStateException("Listener required to play actions");
        }
        controlsListener.onActionPlayed(actionToPlay, this::doOnActionEnd);
    }

    private void jumpToAction(int jumpActionIndex) {

        // TODO - do every action between this.actionIndex and jumpActionIndex

        // TODO - jump to action (will be similar to onReplayLoaded)
    }
}
