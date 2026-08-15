package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
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
        InteractionTarget target = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                ));
        Character contextCharacter = Character.create(CharacterType.Archer, TeamColor.Black);
        InteractionResult result = new InteractionResult(InteractionResultType.SelectableCharacterCardChosen,
                new InteractionContext(contextCharacter), target);

        assertEquals(InteractionResultType.SelectableCharacterCardChosen, result.getResultType());
        assertSame(target, result.getChosenTarget());
    }
}
