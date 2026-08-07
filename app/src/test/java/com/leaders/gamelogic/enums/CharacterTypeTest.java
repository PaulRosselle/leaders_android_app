package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class CharacterTypeTest {

    @Test
    public void getCharacterCard_shouldReturnMatchingCard() {
        assertEquals(CharacterCard.Acrobat,
                CharacterType.Acrobat.getCharacterCard());
        assertEquals(CharacterCard.Archer,
                CharacterType.Archer.getCharacterCard());
        assertEquals(CharacterCard.Assassin,
                CharacterType.Assassin.getCharacterCard());
        assertEquals(CharacterCard.Brewmaster,
                CharacterType.Brewmaster.getCharacterCard());
        assertEquals(CharacterCard.Bruiser,
                CharacterType.Bruiser.getCharacterCard());
        assertEquals(CharacterCard.ClawLauncher,
                CharacterType.ClawLauncher.getCharacterCard());
        assertEquals(CharacterCard.Illusionist,
                CharacterType.Illusionist.getCharacterCard());
        assertEquals(CharacterCard.Jailer,
                CharacterType.Jailer.getCharacterCard());
        assertEquals(CharacterCard.LeaderKing,
                CharacterType.LeaderKing.getCharacterCard());
        assertEquals(CharacterCard.LeaderQueen,
                CharacterType.LeaderQueen.getCharacterCard());
        assertEquals(CharacterCard.Manipulator,
                CharacterType.Manipulator.getCharacterCard());
        assertEquals(CharacterCard.Nemesis,
                CharacterType.Nemesis.getCharacterCard());
        assertEquals(CharacterCard.Protector,
                CharacterType.Protector.getCharacterCard());
        assertEquals(CharacterCard.Rider,
                CharacterType.Rider.getCharacterCard());
        assertEquals(CharacterCard.RoyalGuard,
                CharacterType.RoyalGuard.getCharacterCard());
        assertEquals(CharacterCard.Vizier,
                CharacterType.Vizier.getCharacterCard());
        assertEquals(CharacterCard.Wanderer,
                CharacterType.Wanderer.getCharacterCard());
    }

    @Test
    public void getCharacterCard_shouldMapHermitAndCubToSameCard() {
        assertEquals(
                CharacterCard.HermitAndCub,
                CharacterType.Hermit.getCharacterCard()
        );

        assertEquals(
                CharacterCard.HermitAndCub,
                CharacterType.Cub.getCharacterCard()
        );
    }

    @Test
    public void getCharacterTypesMatchingCard_shouldReturnSingleTypeForSingleTypeCards() {
        assertEquals(
                Collections.singletonList(CharacterType.Acrobat),
                CharacterType.getCharacterTypesMatchingCard(CharacterCard.Acrobat)
        );

        assertEquals(
                Collections.singletonList(CharacterType.LeaderKing),
                CharacterType.getCharacterTypesMatchingCard(CharacterCard.LeaderKing)
        );

        assertEquals(
                Collections.singletonList(CharacterType.Nemesis),
                CharacterType.getCharacterTypesMatchingCard(CharacterCard.Nemesis)
        );
    }

    @Test
    public void getCharacterTypesMatchingCard_shouldReturnBothTypesForHermitAndCub() {
        List<CharacterType> expected = Arrays.asList(
                CharacterType.Cub,
                CharacterType.Hermit
        );

        assertEquals(
                expected,
                CharacterType.getCharacterTypesMatchingCard(
                        CharacterCard.HermitAndCub
                )
        );
    }
}