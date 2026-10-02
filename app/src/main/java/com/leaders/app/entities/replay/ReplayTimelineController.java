package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;

import java.util.ArrayList;
import java.util.List;

public final class ReplayTimelineController {

    public static final int START_INDEX = -1;

    @NonNull
    private final ReplayTimeline timeline;

    private int currentStepIndex = START_INDEX;


    public ReplayTimelineController(@NonNull GameHistory gameHistory) {
        List<ReplaySegment> segments = new ArrayList<>();

        for (IHistoryEntry historyEntry : gameHistory.getEntries()) {
            segments.add(new ReplaySegment(historyEntry));
        }

        timeline = new ReplayTimeline(segments);
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    @NonNull
    public ReplayStep getStep(int stepIndex) {
        return timeline.getStep(stepIndex);
    }

    public int getStepCount() {
        return timeline.getStepCount();
    }

    public boolean hasNextStep() {
        return currentStepIndex < timeline.getStepCount() - 1;
    }

    public boolean hasPreviousStep() {
        return currentStepIndex >= 0;
    }

    @NonNull
    public ReplayStep moveToNextStep() {
        if (!hasNextStep()) {
            throw new IllegalStateException("No next step");
        }

        currentStepIndex++;

        return timeline.getStep(currentStepIndex);
    }

    @NonNull
    public ReplayStep moveToPreviousStep() {
        if (!hasPreviousStep()) {
            throw new IllegalStateException("No previous step");
        }

        ReplayStep step = timeline.getStep(currentStepIndex);
        currentStepIndex--;

        return step;
    }

    public void jumpTo(int stepIndex) {
        if (isOutOfBounds(stepIndex)) {
            throw new IllegalArgumentException("Invalid step index: " + stepIndex);
        }

        currentStepIndex = stepIndex;
    }

    public boolean isOutOfBounds(int stepIndex) {
        return stepIndex < START_INDEX || stepIndex >= timeline.getStepCount();
    }

    public boolean isAtTurnEnd(int stepIndex) {
        if (stepIndex == START_INDEX) {
            return false;
        }

        if (stepIndex >= timeline.getStepCount() - 1) {
            return true;
        }

        return !isSameSegment(stepIndex, stepIndex + 1);
    }

    public boolean isAtTurnEnd() {
        return isAtTurnEnd(currentStepIndex);
    }

    public boolean isAtTurnStart() {
        if (currentStepIndex == START_INDEX || currentStepIndex < 1) {
            return true;
        }

        return !isSameSegment(currentStepIndex, currentStepIndex - 1);
    }

    private boolean isSameSegment(int firstIndex, int secondIndex) {
        return timeline.getStepSegment(firstIndex) == timeline.getStepSegment(secondIndex);
    }

    @NonNull
    public ReplaySegment getStepSegment(int stepIndex) {
        if (isOutOfBounds(stepIndex)) {
            throw new IllegalArgumentException("Invalid step index: " + stepIndex);
        }
        return timeline.getStepSegment(stepIndex);
    }

    public void reset() {
        currentStepIndex = START_INDEX;
    }
}
