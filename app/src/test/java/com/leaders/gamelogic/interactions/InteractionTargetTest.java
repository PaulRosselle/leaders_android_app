package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class InteractionTargetTest {

    @Test
    public void constructor_shouldSetCardTarget() {
        SelectableCharacterCard expectedCard = new SelectableCharacterCard(
                CharacterCard.Archer,
                CharacterCardSelectionStatus.Recruitable
        );

        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentCard, expectedCard);

        assertEquals(TargetCategory.RecruitmentCard, target.getCategory());
        assertEquals(expectedCard, target.getChosenSelectableCharacterCard());
        assertNull(target.getChosenPlayableCharacter());
        assertNull(target.getChosenPosition());
    }

    @Test
    public void constructor_shouldSetCharacterTarget() {
        PlayableCharacter playableCharacter = new PlayableCharacter(
                Character.create(CharacterType.LeaderKing, TeamColor.Black),
                new Position(3, 3),
                false,
                false
        );
        InteractionTarget target = new InteractionTarget(TargetCategory.PlayableCharacter, playableCharacter);

        assertEquals(TargetCategory.PlayableCharacter, target.getCategory());
        assertNull(target.getChosenSelectableCharacterCard());
        assertSame(playableCharacter, target.getChosenPlayableCharacter());
        assertNull(target.getChosenPosition());
    }

    @Test
    public void constructor_shouldSetPositionTarget() {
        Position position = new Position(3, 3);

        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentDestination, position);

        assertEquals(TargetCategory.RecruitmentDestination, target.getCategory());
        assertNull(target.getChosenSelectableCharacterCard());
        assertNull(target.getChosenPlayableCharacter());
        assertSame(position, target.getChosenPosition());
    }

    @Test
    public void equals_shouldReturnTrueForIdenticalCardTargets() {
        InteractionTarget first = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                )
        );
        InteractionTarget second = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                )
        );

        assertEquals(first, second);
    }

    @Test
    public void equals_shouldReturnTrueForIdenticalCharacterTargets() {
        PlayableCharacter playableCharacter = new PlayableCharacter(
                Character.create(CharacterType.Bruiser, TeamColor.Black),
                new Position(3, 3),
                false,
                true
        );

        InteractionTarget first = new InteractionTarget(TargetCategory.PlayableCharacter, playableCharacter);
        InteractionTarget second = new InteractionTarget(TargetCategory.PlayableCharacter, playableCharacter);

        assertEquals(first, second);
    }

    @Test
    public void equals_shouldReturnTrueForIdenticalPositionTargets() {
        InteractionTarget first = new InteractionTarget(
                TargetCategory.RecruitmentDestination,
                new Position(3, 3)
        );
        InteractionTarget second = new InteractionTarget(
                TargetCategory.RecruitmentDestination,
                new Position(3, 3)
        );

        assertEquals(first, second);
    }

    @Test
    public void equals_shouldReturnFalseWhenCategoriesDiffer() {
        InteractionTarget first = new InteractionTarget(TargetCategory.RecruitmentDestination, new Position(3, 3));
        InteractionTarget second = new InteractionTarget(TargetCategory.MovementDestination, new Position(3, 3));

        assertNotEquals(first, second);
    }

    @Test
    public void equals_shouldReturnFalseWhenChosenDataDiffer() {
        InteractionTarget first = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                )
        );
        InteractionTarget second = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Assassin,
                        CharacterCardSelectionStatus.Recruitable
                )
        );

        assertNotEquals(first, second);
    }

    @Test
    public void equals_shouldReturnFalseWhenTargetDataTypesDiffer() {
        InteractionTarget cardTarget = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                )
        );
        InteractionTarget characterTarget = new InteractionTarget(
                TargetCategory.PlayableCharacter,
                new PlayableCharacter(
                        Character.create(CharacterType.Nemesis, TeamColor.Black),
                        new Position(3, 3),
                        true,
                        false
                )
        );

        assertNotEquals(cardTarget, characterTarget);
    }

    @Test
    public void equals_shouldReturnFalseForNull() {
        InteractionTarget target = new InteractionTarget(
                TargetCategory.RecruitmentCard,
                new SelectableCharacterCard(
                        CharacterCard.Archer,
                        CharacterCardSelectionStatus.Recruitable
                )
        );

        assertNotNull(target);
    }

    @Test
    public void hashCode_shouldBeEqualForEqualTargets() {
        InteractionTarget first = new InteractionTarget(
                TargetCategory.RecruitmentDestination,
                new Position(3, 3)
        );
        InteractionTarget second = new InteractionTarget(
                TargetCategory.RecruitmentDestination,
                new Position(3, 3)
        );

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toString_shouldContainTargetInformation() {
        InteractionTarget target = new InteractionTarget(
                TargetCategory.PlayableCharacter,
                new PlayableCharacter(
                        Character.create(CharacterType.Archer, TeamColor.Black),
                        new Position(3, 3),
                        false,
                        false
                )
        );

        assertEquals(
                "InteractionTarget{category=PlayableCharacter, character=" +
                        target.getChosenPlayableCharacter() + "}",
                target.toString()
        );
    }
}