package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.enums.FeedbackType;

import java.util.List;
import java.util.NoSuchElementException;

public final class InteractionFeedback {

    @NonNull final FeedbackType feedbackType;

    @Nullable
    private final List<CharacterActionMotion> characterActionMotions;

    @Nullable
    private final List<RecruitmentActionMotion> recruitmentActionMotions;

    private InteractionFeedback(@NonNull FeedbackType feedbackType,
                                @Nullable List<CharacterActionMotion> characterActionMotions,
                                @Nullable List<RecruitmentActionMotion> recruitmentActionMotions) {
        this.feedbackType = feedbackType;
        if ((characterActionMotions != null) == (recruitmentActionMotions != null)) {
            throw new IllegalArgumentException("There can be only one motion data per interaction");
        }
        this.characterActionMotions = characterActionMotions != null ? List.copyOf(characterActionMotions) : null;
        this.recruitmentActionMotions = recruitmentActionMotions != null ? List.copyOf(recruitmentActionMotions) : null;
    }

    @NonNull
    public List<CharacterActionMotion> getCharacterActionMotions() {
        if (characterActionMotions == null) {
            throw new NoSuchElementException("No character action motion in a " + feedbackType + " feedback");
        }
        return characterActionMotions;
    }

    @NonNull
    public List<RecruitmentActionMotion> getRecruitmentActionMotions() {
        if (recruitmentActionMotions == null) {
            throw new NoSuchElementException("No character action motion in a " + feedbackType + " feedback");
        }
        return recruitmentActionMotions;
    }

    public static InteractionFeedback createForCharacterAction(@NonNull List<CharacterActionMotion> characterActionMotions) {
        return new InteractionFeedback(FeedbackType.CharacterAction, characterActionMotions, null);
    }

    public static InteractionFeedback createForRecruitmentAction(@NonNull List<RecruitmentActionMotion> recruitmentActionMotions) {
        return new InteractionFeedback(FeedbackType.RecruitmentAction, null, recruitmentActionMotions);
    }

    @NonNull
    public FeedbackType getFeedbackType() {
        return feedbackType;
    }
}
