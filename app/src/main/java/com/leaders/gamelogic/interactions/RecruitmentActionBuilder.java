package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.List;

public final class RecruitmentActionBuilder extends GameActionBuilder {

    @NonNull
    private final CharacterCard recruitedCard;

    @NonNull
    private final TeamColor teamColor;

    public RecruitmentActionBuilder(@NonNull CharacterCard recruitedCard,
                                    @NonNull TeamColor teamColor,
                                    @NonNull List<InteractionResult> interactionResults,
                                    @NonNull List<InteractionFeedback> interactionFeedbacks) {
        super(interactionResults, interactionFeedbacks);
        this.recruitedCard = recruitedCard;
        this.teamColor = teamColor;
    }

    public RecruitmentActionBuilder(@NonNull RecruitmentActionBuilder refBuilder) {
        this(refBuilder.recruitedCard, refBuilder.teamColor,
                new ArrayList<>(refBuilder.getResults()),
                new ArrayList<>(refBuilder.getFeedbacks()));
    }

    @NonNull
    public CharacterCard getRecruitedCard() {
        return recruitedCard;
    }

    @NonNull
    public TeamColor getTeamColor() {
        return teamColor;
    }
}