package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReplayTimelineController {

    private static final int START_INDEX = -1;

    @NonNull
    private final ReplayTimeline timeline;

    private int currentActionIndex = START_INDEX;


    public ReplayTimelineController(@NonNull GameHistory gameHistory) {
        List<ReplaySegment> segments = new ArrayList<>();

        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            if (historyEntry instanceof Turn) {
                segments.add(new ReplaySegment((Turn) historyEntry));
            } else if (historyEntry instanceof BanishmentPhase) {
                segments.add(new ReplaySegment((BanishmentPhase) historyEntry));
            }
        }

        timeline = new ReplayTimeline(segments);
    }

    public int getCurrentActionIndex() {
        return currentActionIndex;
    }

    @NonNull
    public IGameAction getAction(int actionIndex) {
        return timeline.getAction(actionIndex);
    }

    public int getActionCount() {
        return timeline.getActionCount();
    }

    public boolean hasNextAction() {
        return currentActionIndex < timeline.getActionCount() - 1;
    }

    public boolean hasPreviousAction() {
        return currentActionIndex >= 0;
    }

    public List<WarningAction> getWarningActions() {
        return getWarningActions(currentActionIndex);
    }

    public List<WarningAction> getWarningActions(int actionIndex) {
        if (!isAtTurnEnd(actionIndex)) {
            return Collections.emptyList();
        }

        return timeline.getSegmentForAction(actionIndex).getWarningActions();
    }

    @NonNull
    public IGameAction moveToNextAction() {
        if (!hasNextAction()) {
            throw new IllegalStateException("No next action");
        }

        currentActionIndex++;

        return timeline.getAction(currentActionIndex);
    }

    @NonNull
    public IGameAction moveToPreviousAction() {
        if (!hasPreviousAction()) {
            throw new IllegalStateException("No previous action");
        }

        IGameAction action = timeline.getAction(currentActionIndex);
        currentActionIndex--;

        return action;
    }

    public void jumpTo(int actionIndex) {
        if (actionIndex < START_INDEX || actionIndex >= timeline.getActionCount()) {
            throw new IllegalArgumentException(
                    "Invalid action index: " + actionIndex
            );
        }

        currentActionIndex = actionIndex;
    }

    public boolean isAtTurnEnd(int actionIndex) {
        if (actionIndex == START_INDEX) {
            return false;
        }

        if (actionIndex >= timeline.getActionCount()) {
            return true;
        }

        return !isSameSegment(actionIndex, actionIndex + 1);
    }

    public boolean isAtTurnEnd() {
        return isAtTurnEnd(currentActionIndex);
    }

    public boolean isAtTurnStart() {
        if (currentActionIndex == START_INDEX || currentActionIndex < 1) {
            return true;
        }

        return !isSameSegment(currentActionIndex, currentActionIndex - 1);
    }

    private boolean isSameSegment(int firstIndex, int secondIndex) {
        return timeline.getSegmentForAction(firstIndex) == timeline.getSegmentForAction(secondIndex);
    }

    public void reset() {
        currentActionIndex = START_INDEX;
    }
}
