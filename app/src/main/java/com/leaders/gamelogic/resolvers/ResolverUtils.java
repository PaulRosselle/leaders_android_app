package com.leaders.gamelogic.resolvers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

import java.util.List;
import java.util.Objects;

final class ResolverUtils {
    private ResolverUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean isPositionResult(@NonNull InteractionResult result, @NonNull TargetCategory category) {
        InteractionTarget target = result.getChosenTarget();
        return result.getResultType() == InteractionResultType.PositionChosen &&
                target != null && target.getCategory() == category;
    }

    @NonNull
    public static Position getChosenPosition(@NonNull InteractionResult result) {
        InteractionTarget target = Objects.requireNonNull(
                result.getChosenTarget(),
                "No target in result: " + result
        );
        return Objects.requireNonNull(target.getChosenPosition(), "No position in result: " + result);
    }

    @NonNull
    public static InteractionRequest createPositionRequest(@NonNull InteractionContext context,
                                                           @NonNull List<InteractionTarget> targets) {
        return new InteractionRequest(InteractionType.PositionExpected, context, targets,
                List.of(InteractionResultType.PositionChosen, InteractionResultType.CancelAction)
        );
    }

    @NonNull
    public static InteractionFeedback createSingleMotionFeedback(@NonNull CharacterMotionType type,
                                                                 @NonNull Character character,
                                                                 @NonNull Position from,
                                                                 @NonNull Position to) {
        return InteractionFeedback.createForCharacterAction(List.of(
                new CharacterActionMotion(type, List.of(new CharacterActionTarget(character, from, to)))
        ));
    }
}
