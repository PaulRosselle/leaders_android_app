package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

public class InteractionTargetTest {

    @Test
    public void constructor_shouldSetCardTarget() {
        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);

        assertEquals(TargetCategory.RecruitmentCard, target.getCategory());
        assertEquals(CharacterCard.Archer, target.getChosenCard());
        assertNull(target.getChosenCharacterPlayableState());
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
        assertNull(target.getChosenCard());
        assertSame(playableCharacter, target.getChosenCharacterPlayableState());
        assertNull(target.getChosenPosition());
    }

    @Test
    public void constructor_shouldSetPositionTarget() {
        Position position = new Position(3, 3);

        InteractionTarget target = new InteractionTarget(TargetCategory.RecruitmentDestination, position);

        assertEquals(TargetCategory.RecruitmentDestination, target.getCategory());
        assertNull(target.getChosenCard());
        assertNull(target.getChosenCharacterPlayableState());
        assertSame(position, target.getChosenPosition());
    }

    @Test
    public void equals_shouldReturnTrueForIdenticalCardTargets() {
        InteractionTarget first = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);
        InteractionTarget second = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);

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
        InteractionTarget first = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);
        InteractionTarget second = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Assassin);

        assertNotEquals(first, second);
    }

    @Test
    public void equals_shouldReturnFalseWhenTargetDataTypesDiffer() {
        InteractionTarget cardTarget = new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer);
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
                CharacterCard.Archer
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
                        target.getChosenCharacterPlayableState() + "}",
                target.toString()
        );
    }
}