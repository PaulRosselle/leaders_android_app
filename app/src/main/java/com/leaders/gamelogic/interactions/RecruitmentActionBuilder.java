package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecruitmentActionBuilder {

    @NonNull
    private final CharacterCard recruitedCard;

    @NonNull
    private final TeamColor teamColor;

    @NonNull
    private final List<InteractionResult> interactionResults;

    @NonNull
    private final List<InteractionFeedback> interactionFeedbacks;

    public RecruitmentActionBuilder(@NonNull CharacterCard recruitedCard,
                                    @NonNull TeamColor teamColor,
                                    @NonNull List<InteractionResult> interactionResults,
                                    @NonNull List<InteractionFeedback> interactionFeedbacks) {
        this.recruitedCard = recruitedCard;
        this.teamColor = teamColor;
        this.interactionResults = interactionResults;
        this.interactionFeedbacks = interactionFeedbacks;
    }

    public RecruitmentActionBuilder(@NonNull RecruitmentActionBuilder refBuilder) {
        this(refBuilder.recruitedCard, refBuilder.teamColor,
                new ArrayList<>(refBuilder.interactionResults),
                new ArrayList<>(refBuilder.interactionFeedbacks));
    }

    @NonNull
    public CharacterCard getRecruitedCard() {
        return recruitedCard;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NonNull
    public List<InteractionResult> getInteractionResults() {
        return Collections.unmodifiableList(interactionResults);
    }

    @NonNull
    public List<InteractionFeedback> getInteractionFeedbacks() {
        return Collections.unmodifiableList(interactionFeedbacks);
    }

    public void addResult(@NonNull InteractionResult result) {
        interactionResults.add(result);
    }

    public void addFeedback(@NonNull InteractionFeedback feedback) {
        interactionFeedbacks.add(feedback);
    }
}