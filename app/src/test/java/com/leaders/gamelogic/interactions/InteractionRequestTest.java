package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InteractionRequestTest {

    @Test
    public void constructor_shouldSetValues() {
        InteractionType type = InteractionType.CharacterCardExpected;
        List<InteractionTarget> legalTargets = List.of(
                new InteractionTarget(TargetCategory.RecruitmentCard, CharacterCard.Archer));
        List<InteractionResultType> legalResults = List.of(InteractionResultType.CardChosen);

        Character contextCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        InteractionRequest request = new InteractionRequest(type,
                new InteractionContext(contextCharacter),
                legalTargets, legalResults
        );

        assertEquals(type, request.getRequestType());
        assertEquals(legalTargets, request.getLegalTargets());
        assertEquals(legalResults, request.getLegalResults());
    }

    @Test
    public void constructor_shouldDefensivelyCopyCollections() {
        List<InteractionTarget> legalTargets = new ArrayList<>();
        List<InteractionResultType> legalResults = new ArrayList<>();
        Character contextCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        InteractionRequest request = new InteractionRequest(InteractionType.PlayableCharacterExpected,
                new InteractionContext(contextCharacter),
                legalTargets, legalResults
        );


        legalTargets.add(new InteractionTarget(
                TargetCategory.PlayableCharacter,
                new PlayableCharacter(
                        Character.create(CharacterType.LeaderKing, TeamColor.Black),
                        new Position(3, 3),
                        false,
                        false
                ))
        );
        legalResults.add(InteractionResultType.PlayableCharacterChosen);

        assertTrue(request.getLegalTargets().isEmpty());
        assertTrue(request.getLegalResults().isEmpty());
    }

    @Test
    public void getLegalTargets_shouldReturnUnmodifiableList() {
        Character contextCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        InteractionRequest request = new InteractionRequest(
                InteractionType.PositionExpected,
                new InteractionContext(contextCharacter),
                List.of(new InteractionTarget(TargetCategory.RecruitmentDestination, new Position(3, 3))),
                List.of()
        );

        assertThrows(UnsupportedOperationException.class, () -> request.getLegalTargets().clear());
    }

    @Test
    public void getLegalResults_shouldReturnUnmodifiableList() {
        Character contextCharacter = Character.create(CharacterType.LeaderQueen, TeamColor.Black);
        InteractionRequest request = new InteractionRequest(
                InteractionType.CharacterCardExpected,
                new InteractionContext(contextCharacter),
                List.of(),
                List.of(InteractionResultType.CardChosen)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> request.getLegalResults().clear()
        );
    }
}