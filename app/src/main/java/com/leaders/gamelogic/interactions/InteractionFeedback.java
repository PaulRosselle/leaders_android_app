package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.enums.FeedbackType;

import java.util.NoSuchElementException;

public final class InteractionFeedback {

    @NonNull final FeedbackType feedbackType;

    @Nullable
    private final CharacterActionMotion characterActionMotion;

    @Nullable
    private final RecruitmentActionMotion recruitmentActionMotion;

    private InteractionFeedback(@NonNull FeedbackType feedbackType,
                               @Nullable CharacterActionMotion characterActionMotion,
                               @Nullable RecruitmentActionMotion recruitmentActionMotion) {
        this.feedbackType = feedbackType;
        if ((characterActionMotion != null) == (recruitmentActionMotion != null)) {
            throw new IllegalArgumentException("There can be only one motion data per interaction");
        }
        this.characterActionMotion = characterActionMotion;
        this.recruitmentActionMotion = recruitmentActionMotion;
    }

    public InteractionFeedback(@NonNull CharacterActionMotion characterActionMotion) {
        this.feedbackType = FeedbackType.CharacterAction;
        this.characterActionMotion = characterActionMotion;
        this.recruitmentActionMotion = null;
    }


    public InteractionFeedback(@NonNull RecruitmentActionMotion recruitmentActionMotion) {
        this.feedbackType = FeedbackType.RecruitmentAction;
        this.characterActionMotion = null;
        this.recruitmentActionMotion = recruitmentActionMotion;
    }

    @NonNull
    public CharacterActionMotion getCharacterActionMotion() {
        if (characterActionMotion == null) {
            throw new NoSuchElementException("No character action motion in a " + feedbackType + " feedback");
        }
        return characterActionMotion;
    }

    @NonNull
    public RecruitmentActionMotion getRecruitmentActionMotion() {
        if (recruitmentActionMotion == null) {
            throw new NoSuchElementException("No character action motion in a " + feedbackType + " feedback");
        }
        return recruitmentActionMotion;
    }
}
