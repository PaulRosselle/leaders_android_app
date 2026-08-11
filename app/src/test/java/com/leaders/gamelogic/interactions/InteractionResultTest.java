package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class InteractionResultTest {

    @Test
    public void constructor_shouldSetResultTypeWithoutTarget() {
        Character contextCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        InteractionResult result = new InteractionResult(InteractionResultType.NoChoice,
                new InteractionContext(contextCharacter), null);

        assertEquals(InteractionResultType.NoChoice, result.getResultType());
        assertNull(result.getChosenTarget());
    }

    @Test
    public void constructor_shouldSetResultTypeAndTarget() {
        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);
        Character contextCharacter = Character.create(CharacterType.Archer, TeamColor.Black);
        InteractionResult result = new InteractionResult(InteractionResultType.CardChosen,
                new InteractionContext(contextCharacter), target);

        assertEquals(InteractionResultType.CardChosen, result.getResultType());
        assertSame(target, result.getChosenTarget());
    }
}
