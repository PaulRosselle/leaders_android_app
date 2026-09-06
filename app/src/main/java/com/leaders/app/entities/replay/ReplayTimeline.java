package com.leaders.app.entities.replay;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.IGameAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReplayTimeline {
    @NonNull
    private final List<ReplaySegment> segments;

    @NonNull
    private final List<IGameAction> actions;

    @NonNull
    private final Map<Integer, ReplaySegment> actionSegments;

    public ReplayTimeline(@NonNull List<ReplaySegment> segments) {
        this.segments = segments;


        actions = new ArrayList<>();
        actionSegments = new HashMap<>();

        for (ReplaySegment segment : segments) {
            for (IGameAction action : segment.getPlayableActions()) {
                int actionIndex = actions.size();

                actions.add(action);
                actionSegments.put(actionIndex, segment);
            }
        }
    }

    @NonNull
    public List<ReplaySegment> getSegments() {
        return segments;
    }

    @NonNull
    public ReplaySegment getSegmentForAction(int actionIndex) {
        return Objects.requireNonNull(actionSegments.get(actionIndex),
                "No segment found for action index: " + actionIndex );
    }

    @NonNull
    public IGameAction getAction(int actionIndex) {
        return actions.get(actionIndex);
    }

    public int getActionCount() {
        return actions.size();
    }
}
