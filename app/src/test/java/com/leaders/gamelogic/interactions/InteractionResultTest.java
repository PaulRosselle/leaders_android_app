package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.enums.CharacterCard;

import org.junit.Test;

public class InteractionResultTest {

    @Test
    public void constructor_shouldSetResultTypeWithoutTarget() {
        InteractionResult result = new InteractionResult(InteractionResultType.NoChoice, null);

        assertEquals(InteractionResultType.NoChoice, result.getResultType());
        assertNull(result.getChosenTarget());
    }

    @Test
    public void constructor_shouldSetResultTypeAndTarget() {
        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);
        InteractionResult result = new InteractionResult(InteractionResultType.CardChosen, target);

        assertEquals(InteractionResultType.CardChosen, result.getResultType());
        assertSame(target, result.getChosenTarget());
    }
}
