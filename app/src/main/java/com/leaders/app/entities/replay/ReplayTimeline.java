package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReplayTimeline {
    @NonNull
    private final List<ReplaySegment> segments;

    @NonNull
    private final List<ReplayStep> steps;

    @NonNull
    private final Map<Integer, ReplaySegment> stepSegments;

    public ReplayTimeline(@NonNull List<ReplaySegment> segments) {
        this.segments = segments;


        steps = new ArrayList<>();
        stepSegments = new HashMap<>();

        for (ReplaySegment segment : segments) {
            for (ReplayStep step : segment.getSteps()) {
                int actionIndex = steps.size();

                steps.add(step);
                stepSegments.put(actionIndex, segment);
            }
        }
    }

    @NonNull
    public List<ReplaySegment> getSegments() {
        return segments;
    }

    @NonNull
    public ReplaySegment getStepSegment(int stepIndex) {
        return Objects.requireNonNull(stepSegments.get(stepIndex),
                "No segment found for step index: " + stepIndex
        );
    }

    @NonNull
    public ReplayStep getStep(int stepIndex) {
        return steps.get(stepIndex);
    }

    public int getStepCount() {
        return steps.size();
    }
}
