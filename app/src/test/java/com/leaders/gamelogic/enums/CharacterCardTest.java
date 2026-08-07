package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CharacterCardTest {

    @Test
    public void getAbilityTypes_shouldReturnActiveForActiveCards() {
        CharacterCard[] cards = {
                CharacterCard.Acrobat,
                CharacterCard.Brewmaster,
                CharacterCard.Bruiser,
                CharacterCard.ClawLauncher,
                CharacterCard.Illusionist,
                CharacterCard.Manipulator,
                CharacterCard.Rider,
                CharacterCard.RoyalGuard,
                CharacterCard.Wanderer
        };

        for (CharacterCard card : cards) {
            assertArrayEquals(
                    new AbilityType[]{AbilityType.Active},
                    card.getAbilityTypes()
            );
        }
    }

    @Test
    public void getAbilityTypes_shouldReturnPassiveForPassiveCards() {
        CharacterCard[] cards = {
                CharacterCard.Archer,
                CharacterCard.Assassin,
                CharacterCard.Jailer,
                CharacterCard.Protector,
                CharacterCard.Vizier
        };

        for (CharacterCard card : cards) {
            assertArrayEquals(
                    new AbilityType[]{AbilityType.Passive},
                    card.getAbilityTypes()
            );
        }
    }

    @Test
    public void getAbilityTypes_shouldReturnSpecialForSpecialCards() {
        CharacterCard[] cards = {
                CharacterCard.HermitAndCub,
                CharacterCard.Nemesis
        };

        for (CharacterCard card : cards) {
            assertArrayEquals(
                    new AbilityType[]{AbilityType.Special},
                    card.getAbilityTypes()
            );
        }
    }

    @Test
    public void getAbilityTypes_shouldReturnEmptyForLeaders() {
        assertArrayEquals(
                new AbilityType[0],
                CharacterCard.LeaderKing.getAbilityTypes()
        );

        assertArrayEquals(
                new AbilityType[0],
                CharacterCard.LeaderQueen.getAbilityTypes()
        );
    }

    @Test
    public void isLeader_shouldReturnTrueForLeaders() {
        assertTrue(CharacterCard.LeaderKing.isLeader());
        assertTrue(CharacterCard.LeaderQueen.isLeader());
    }

    @Test
    public void isLeader_shouldReturnFalseForNonLeaders() {
        assertFalse(CharacterCard.Acrobat.isLeader());
        assertFalse(CharacterCard.HermitAndCub.isLeader());
        assertFalse(CharacterCard.Nemesis.isLeader());
    }

    @Test
    public void canBeRecruited_shouldReturnFalseForLeaders() {
        assertFalse(CharacterCard.LeaderKing.canBeRecruited());
        assertFalse(CharacterCard.LeaderQueen.canBeRecruited());
    }

    @Test
    public void canBeRecruited_shouldReturnTrueForNonLeaders() {
        assertTrue(CharacterCard.Acrobat.canBeRecruited());
        assertTrue(CharacterCard.HermitAndCub.canBeRecruited());
        assertTrue(CharacterCard.Nemesis.canBeRecruited());
    }
}