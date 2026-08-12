package com.leaders.gamelogic.resolvers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.interactions.RecruitmentActionBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class RecruitmentActionResolverTest {

    private static final TeamColor TEAM = TeamColor.White;

    private Game game;

    @Before
    public void setUp() {
        game = createTestGame();
        // A leader on the same team is mandatory on the board for action validity checks
        Character leader = Character.create(CharacterType.LeaderKing, TeamColor.White);
        game.getBoard().getCell(new Position(3, 3)).setCharacter(leader);
    }

    @Test
    public void getNextInteraction_returnsRequestForFirstCharacter() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        new ArrayList<>(),
                        new ArrayList<>()
                );

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertEquals(
                com.leaders.gamelogic.interactions.InteractionType.PositionExpected,
                request.getRequestType()
        );
        assertNotNull(request.getContext());
        assertNotNull(request.getContext().getCharacter());
        assertEquals(
                CharacterType.getCharacterTypesMatchingCard(card).get(0),
                request.getContext().getCharacter().getCharacterType()
        );

        assertTrue(
                request.getLegalResults().contains(InteractionResultType.PositionChosen)
        );
        assertTrue(
                request.getLegalResults().contains(InteractionResultType.CancelAction)
        );

        assertTrue(
                request.getLegalTargets().stream()
                        .allMatch(target ->
                                target.getCategory() == TargetCategory.RecruitmentDestination)
        );
    }

    @Test
    public void getNextInteraction_returnsNextCharacterAfterPreviousRecruitment() {
        CharacterCard card = getMultiCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType firstType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);
        Character firstCharacter = Character.create(firstType, TEAM);

        Position position = getFirstLegalRecruitmentPosition();

        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(firstCharacter),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        position
                )
        );

        InteractionFeedback feedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                firstCharacter,
                                position
                        ))
                );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(result),
                        List.of(feedback)
                );

        InteractionRequest request = resolver.getNextInteraction(builder);

        assertNotNull(request);
        assertNotNull(request.getContext());

        CharacterType expectedType =
                CharacterType.getCharacterTypesMatchingCard(card).get(1);

        assertEquals(
                expectedType,
                request.getContext().getCharacter().getCharacterType()
        );
    }

    @Test
    public void getNextInteraction_returnsNullWhenRecruitmentIsComplete() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType characterType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);

        Character character = Character.create(characterType, TEAM);
        Position position = getFirstLegalRecruitmentPosition();

        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        position
                )
        );

        InteractionFeedback feedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                character,
                                position
                        ))
                );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(result),
                        List.of(feedback)
                );

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextInteraction_returnsNullAfterCancellation() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver = new RecruitmentActionResolver(game, card, TEAM);

        Character testCharacter = Character.create(
                CharacterType.getCharacterTypesMatchingCard(card).get(0), TEAM
        );
        InteractionResult cancellation = new InteractionResult(
                InteractionResultType.CancelAction,
                new InteractionContext(testCharacter),
                null
        );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(cancellation),
                        List.of()
                );

        assertNull(resolver.getNextInteraction(builder));
    }

    @Test
    public void getNextFeedback_createsAddMotionFromPositionResult() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType characterType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);

        Character character = Character.create(characterType, TEAM);
        Position position = getFirstLegalRecruitmentPosition();

        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        position
                )
        );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(result),
                        List.of()
                );

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);

        List<RecruitmentActionMotion> motions =
                feedback.getRecruitmentActionMotions();

        assertEquals(1, motions.size());

        RecruitmentActionMotion motion = motions.get(0);

        assertEquals(RecruitmentMotionType.Add, motion.getMotionType());
        assertSame(character, motion.getCharacter());
        assertEquals(position, motion.getPosition());
    }

    @Test
    public void getNextFeedback_returnsNullWhenFeedbackAlreadyGenerated() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType characterType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);

        Character character = Character.create(characterType, TEAM);
        Position position = getFirstLegalRecruitmentPosition();

        InteractionResult result = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        position
                )
        );

        InteractionFeedback feedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                character,
                                position
                        ))
                );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(result),
                        List.of(feedback)
                );

        assertNull(resolver.getNextFeedback(builder));
    }

    @Test
    public void getNextFeedback_cancelWithoutPreviousFeedback_createsEmptyCancellationFeedback() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType characterType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);

        Character character = Character.create(characterType, TEAM);

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(new InteractionResult(
                                InteractionResultType.CancelAction,
                                new InteractionContext(character),
                                null
                        )),
                        List.of()
                );

        InteractionFeedback feedback = resolver.getNextFeedback(builder);

        assertNotNull(feedback);
        assertTrue(feedback.getRecruitmentActionMotions().isEmpty());
    }

    @Test
    public void getNextFeedback_cancelAfterOnePlacement_createsRemoveMotion() {
        CharacterCard card = getSingleCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        CharacterType characterType =
                CharacterType.getCharacterTypesMatchingCard(card).get(0);

        Character character = Character.create(characterType, TEAM);
        Position position = getFirstLegalRecruitmentPosition();

        InteractionResult positionResult = new InteractionResult(
                InteractionResultType.PositionChosen,
                new InteractionContext(character),
                new InteractionTarget(
                        TargetCategory.RecruitmentDestination,
                        position
                )
        );

        InteractionFeedback addFeedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                character,
                                position
                        ))
                );

        InteractionResult cancelResult =
                new InteractionResult(InteractionResultType.CancelAction,
                        new InteractionContext(character), null);

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(positionResult, cancelResult),
                        List.of(addFeedback)
                );

        InteractionFeedback cancellationFeedback =
                resolver.getNextFeedback(builder);

        assertNotNull(cancellationFeedback);

        List<RecruitmentActionMotion> motions =
                cancellationFeedback.getRecruitmentActionMotions();

        assertEquals(1, motions.size());

        RecruitmentActionMotion motion = motions.get(0);

        assertEquals(RecruitmentMotionType.Remove, motion.getMotionType());
        assertSame(character, motion.getCharacter());
        assertEquals(position, motion.getPosition());
    }

    @Test
    public void buildAction_collectsMotionsFromAllFeedbacks() {
        CharacterCard card = getMultiCharacterCard();
        RecruitmentActionResolver resolver =
                new RecruitmentActionResolver(game, card, TEAM);

        List<CharacterType> characterTypes =
                CharacterType.getCharacterTypesMatchingCard(card);

        Character firstCharacter = Character.create(characterTypes.get(0), TEAM);
        Character secondCharacter = Character.create(characterTypes.get(1), TEAM);

        Position firstPosition = getFirstLegalRecruitmentPosition();
        Position secondPosition = getSecondLegalRecruitmentPosition();

        InteractionFeedback firstFeedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                firstCharacter,
                                firstPosition
                        ))
                );

        InteractionFeedback secondFeedback =
                InteractionFeedback.createForRecruitmentAction(
                        List.of(new RecruitmentActionMotion(
                                RecruitmentMotionType.Add,
                                secondCharacter,
                                secondPosition
                        ))
                );

        RecruitmentActionBuilder builder =
                new RecruitmentActionBuilder(
                        card,
                        TEAM,
                        List.of(),
                        List.of(firstFeedback, secondFeedback)
                );

        RecruitmentAction action = resolver.buildAction(builder);

        assertEquals(2, action.getMotions().size());

        assertEquals(
                firstPosition,
                action.getMotions().get(0).getPosition()
        );
        assertEquals(
                secondPosition,
                action.getMotions().get(1).getPosition()
        );
    }

    /*
     * Replace these helpers with the project's existing game/board fixtures.
     */

    @NonNull
    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    @NonNull
    private CharacterCard getSingleCharacterCard() {
        for (CharacterCard card : CharacterCard.values()) {
            if (CharacterType.getCharacterTypesMatchingCard(card).size() == 1) {
                return card;
            }
        }
        throw new IllegalStateException("No single-character card found");
    }

    @NonNull
    private CharacterCard getMultiCharacterCard() {
        for (CharacterCard card : CharacterCard.values()) {
            if (CharacterType.getCharacterTypesMatchingCard(card).size() > 1) {
                return card;
            }
        }
        throw new IllegalStateException("No multi-character card found");
    }

    @NonNull
    private Position getFirstLegalRecruitmentPosition() {
        return com.leaders.gamelogic.queries.BoardQuery
                .getRecruitmentCells(game.getBoard(), TEAM)
                .get(0)
                .getPosition();
    }

    @NonNull
    private Position getSecondLegalRecruitmentPosition() {
        List<Position> positions =
                com.leaders.gamelogic.queries.BoardQuery
                        .getRecruitmentCells(game.getBoard(), TEAM)
                        .stream()
                        .map(Cell::getPosition)
                        .toList();

        if (positions.size() < 2) {
            throw new IllegalStateException(
                    "The test game must provide at least two recruitment positions"
            );
        }

        return positions.get(1);
    }
}