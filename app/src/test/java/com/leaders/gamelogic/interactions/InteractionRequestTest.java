package com.leaders.gamelogic.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.entities.Character;
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

        InteractionRequest request = new InteractionRequest(type, legalTargets, legalResults);

        assertEquals(type, request.getType());
        assertEquals(legalTargets, request.getLegalTargets());
        assertEquals(legalResults, request.getLegalResults());
    }

    @Test
    public void constructor_shouldDefensivelyCopyCollections() {
        List<InteractionTarget> legalTargets = new ArrayList<>();
        List<InteractionResultType> legalResults = new ArrayList<>();

        InteractionRequest request = new InteractionRequest(InteractionType.CharacterExpected, legalTargets, legalResults);

        legalTargets.add(new InteractionTarget(
                TargetCategory.PlayableCharacter,
                Character.create(CharacterType.Archer, TeamColor.Black))
        );
        legalResults.add(InteractionResultType.CharacterChosen);

        assertTrue(request.getLegalTargets().isEmpty());
        assertTrue(request.getLegalResults().isEmpty());
    }

    @Test
    public void getLegalTargets_shouldReturnUnmodifiableList() {
        InteractionRequest request = new InteractionRequest(
                InteractionType.PositionExpected,
                List.of(new InteractionTarget(TargetCategory.RecruitmentDestination, new Position(3, 3))),
                List.of()
        );

        assertThrows(UnsupportedOperationException.class, () -> request.getLegalTargets().clear());
    }

    @Test
    public void getLegalResults_shouldReturnUnmodifiableList() {
        InteractionRequest request = new InteractionRequest(
                InteractionType.CharacterCardExpected,
                List.of(),
                List.of(InteractionResultType.CardChosen)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> request.getLegalResults().clear()
        );
    }
}