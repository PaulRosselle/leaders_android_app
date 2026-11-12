package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.enums.WarningType;
import com.leaders.gamelogic.handlers.CharacterActionHandler;
import com.leaders.gamelogic.handlers.RecruitmentActionHandler;
import com.leaders.gamelogic.handlers.WarningActionHandler;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class ActionReversalUtilsTest {

    private static final Position POS_A = new Position(3, 3);
    private static final Position POS_B = new Position(3, 4);
    private static final Position POS_C = new Position(3, 5);

    //region Helpers

    @NonNull
    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class), // playerWarnings
                new HashMap<>() // charactersTransformedState
        );
    }

    @NonNull
    private Character createCharacter(@NonNull CharacterType type, @NonNull TeamColor team) {
        return Character.create(type, team);
    }

    @NonNull
    private CharacterActionMotion motion(@NonNull CharacterMotionType type,
                                         @NonNull CharacterActionTarget... targets) {
        return new CharacterActionMotion(type, List.of(targets));
    }

    @NonNull
    private CharacterActionTarget target(@NonNull Character character,
                                         Position origin,
                                         Position destination) {
        return new CharacterActionTarget(character, origin, destination);
    }

    @NonNull
    private CharacterAction characterAction(@NonNull Character source,
                                            @NonNull CharacterActionMotion... motions) {
        return new CharacterAction(source, List.of(motions));
    }

    @NonNull
    private RecruitmentActionMotion recruitmentMotion(@NonNull RecruitmentMotionType type,
                                                      @NonNull Character character,
                                                      @NonNull Position position) {
        return new RecruitmentActionMotion(type, character, position);
    }

    @NonNull
    private CharacterAction reverseCharacter(@NonNull CharacterAction action) {
        return (CharacterAction) ActionReversalUtils.reverse(action);
    }

    @NonNull
    private RecruitmentAction reverseRecruitment(@NonNull RecruitmentAction action) {
        return (RecruitmentAction) ActionReversalUtils.reverse(action);
    }

    @NonNull
    private WarningAction reverseWarning(@NonNull WarningAction action) {
        return (WarningAction) ActionReversalUtils.reverse(action);
    }

    private void assertMotionTypeReversal(@NonNull CharacterMotionType expected,
                                          @NonNull CharacterMotionType original) {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction action = characterAction(character,
                motion(original, target(character, POS_A, POS_B)));

        CharacterAction reversed = reverseCharacter(action);

        assertEquals(expected, reversed.getMotions().get(0).getMotionType());
    }

    @NonNull
    private BanishmentAction createBanishmentAction() {
        return new BanishmentAction(CharacterCard.Archer, TeamColor.Black);
    }

    @NonNull
    private TransitionAction createTransitionAction() {
        // Enum constants are not relied upon so that the test does not depend on game-flow details.
        return new TransitionAction(TransitionType.values()[0], TransitionTarget.values()[0]);
    }

    //endregion

    //region isReversible

    @Test
    public void isReversible_shouldReturnTrueForCharacterAction() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        IGameAction action = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)));

        assertTrue(ActionReversalUtils.isReversible(action));
    }

    @Test
    public void isReversible_shouldReturnTrueForRecruitmentAction() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        IGameAction action = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Add, character, POS_A)));

        assertTrue(ActionReversalUtils.isReversible(action));
    }

    @Test
    public void isReversible_shouldReturnTrueForWarningAction() {
        IGameAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);

        assertTrue(ActionReversalUtils.isReversible(action));
    }

    @Test
    public void isReversible_shouldReturnFalseForBanishmentAction() {
        assertFalse(ActionReversalUtils.isReversible(createBanishmentAction()));
    }

    @Test
    public void isReversible_shouldReturnFalseForTransitionAction() {
        assertFalse(ActionReversalUtils.isReversible(createTransitionAction()));
    }

    //endregion

    //region Unsupported actions

    @Test
    public void reverse_shouldThrowForBanishmentAction() {
        assertThrows(IllegalArgumentException.class,
                () -> ActionReversalUtils.reverse(createBanishmentAction()));
    }

    @Test
    public void reverse_shouldThrowForTransitionAction() {
        assertThrows(IllegalArgumentException.class,
                () -> ActionReversalUtils.reverse(createTransitionAction()));
    }

    //endregion

    //region WarningAction

    @Test
    public void reverse_warningAction_shouldNegatePositiveCountChange() {
        WarningAction reversed = reverseWarning(new WarningAction(WarningType.Barrage, TeamColor.Black, 2));

        assertEquals(-2, reversed.getCountChange());
    }

    @Test
    public void reverse_warningAction_shouldNegateNegativeCountChange() {
        WarningAction reversed = reverseWarning(new WarningAction(WarningType.Barrage, TeamColor.Black, -3));

        assertEquals(3, reversed.getCountChange());
    }

    @Test
    public void reverse_warningAction_shouldKeepZeroCountChange() {
        WarningAction reversed = reverseWarning(new WarningAction(WarningType.Barrage, TeamColor.Black, 0));

        assertEquals(0, reversed.getCountChange());
    }

    @Test
    public void reverse_warningAction_shouldKeepWarningTypeAndTeamColor() {
        WarningAction reversed = reverseWarning(new WarningAction(WarningType.Barrage, TeamColor.White, 1));

        assertEquals(GameActionType.Warning, reversed.getActionType());
        assertEquals(WarningType.Barrage, reversed.getWarningType());
        assertEquals(TeamColor.White, reversed.getTeamColor());
    }

    @Test
    public void reverse_warningAction_shouldReturnNewInstanceAndNotModifyOriginal() {
        WarningAction original = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);

        WarningAction reversed = reverseWarning(original);

        assertNotSame(original, reversed);
        assertEquals(1, original.getCountChange());
    }

    @Test
    public void reverse_warningAction_reversingTwiceShouldGiveBackOriginalCountChange() {
        WarningAction original = new WarningAction(WarningType.Barrage, TeamColor.Black, 4);

        WarningAction twice = reverseWarning(reverseWarning(original));

        assertEquals(original.getCountChange(), twice.getCountChange());
        assertEquals(original.getWarningType(), twice.getWarningType());
        assertEquals(original.getTeamColor(), twice.getTeamColor());
    }

    //endregion

    //region RecruitmentAction

    @Test
    public void reverse_recruitmentAction_shouldTurnAddIntoRemove() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Add, character, POS_A)));

        RecruitmentAction reversed = reverseRecruitment(action);

        assertEquals(1, reversed.getMotions().size());
        assertEquals(RecruitmentMotionType.Remove, reversed.getMotions().get(0).getMotionType());
    }

    @Test
    public void reverse_recruitmentAction_shouldTurnRemoveIntoAdd() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Remove, character, POS_A)));

        RecruitmentAction reversed = reverseRecruitment(action);

        assertEquals(RecruitmentMotionType.Add, reversed.getMotions().get(0).getMotionType());
    }

    @Test
    public void reverse_recruitmentAction_shouldKeepCharacterAndPosition() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Add, character, POS_A)));

        RecruitmentActionMotion reversedMotion = reverseRecruitment(action).getMotions().get(0);

        assertSame(character, reversedMotion.getCharacter());
        assertSame(POS_A, reversedMotion.getPosition());
    }

    @Test
    public void reverse_recruitmentAction_shouldReverseMotionOrder() {
        Character first = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.Black);
        Character third = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(List.of(
                recruitmentMotion(RecruitmentMotionType.Add, first, POS_A),
                recruitmentMotion(RecruitmentMotionType.Add, second, POS_B),
                recruitmentMotion(RecruitmentMotionType.Add, third, POS_C)
        ));

        List<RecruitmentActionMotion> reversedMotions = reverseRecruitment(action).getMotions();

        assertEquals(3, reversedMotions.size());
        assertSame(third, reversedMotions.get(0).getCharacter());
        assertSame(second, reversedMotions.get(1).getCharacter());
        assertSame(first, reversedMotions.get(2).getCharacter());
    }

    @Test
    public void reverse_recruitmentAction_shouldHandleMixedMotionTypes() {
        Character first = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(List.of(
                recruitmentMotion(RecruitmentMotionType.Add, first, POS_A),
                recruitmentMotion(RecruitmentMotionType.Remove, second, POS_B)
        ));

        List<RecruitmentActionMotion> reversedMotions = reverseRecruitment(action).getMotions();

        assertSame(second, reversedMotions.get(0).getCharacter());
        assertEquals(RecruitmentMotionType.Add, reversedMotions.get(0).getMotionType());
        assertSame(first, reversedMotions.get(1).getCharacter());
        assertEquals(RecruitmentMotionType.Remove, reversedMotions.get(1).getMotionType());
    }

    @Test
    public void reverse_recruitmentAction_shouldHandleEmptyMotionList() {
        RecruitmentAction reversed = reverseRecruitment(new RecruitmentAction(new ArrayList<>()));

        assertEquals(GameActionType.Recruitment, reversed.getActionType());
        assertTrue(reversed.getMotions().isEmpty());
    }

    @Test
    public void reverse_recruitmentAction_shouldReturnNewInstanceAndNotModifyOriginal() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction original = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Add, character, POS_A)));

        RecruitmentAction reversed = reverseRecruitment(original);

        assertNotSame(original, reversed);
        assertEquals(RecruitmentMotionType.Add, original.getMotions().get(0).getMotionType());
    }

    @Test
    public void reverse_recruitmentAction_reversingTwiceShouldGiveBackOriginalMotions() {
        Character first = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.Black);
        RecruitmentAction original = new RecruitmentAction(List.of(
                recruitmentMotion(RecruitmentMotionType.Add, first, POS_A),
                recruitmentMotion(RecruitmentMotionType.Remove, second, POS_B)
        ));

        List<RecruitmentActionMotion> twice = reverseRecruitment(reverseRecruitment(original)).getMotions();

        assertEquals(2, twice.size());
        assertSame(first, twice.get(0).getCharacter());
        assertEquals(RecruitmentMotionType.Add, twice.get(0).getMotionType());
        assertSame(POS_A, twice.get(0).getPosition());
        assertSame(second, twice.get(1).getCharacter());
        assertEquals(RecruitmentMotionType.Remove, twice.get(1).getMotionType());
        assertSame(POS_B, twice.get(1).getPosition());
    }

    //endregion

    //region CharacterAction - motion types

    @Test
    public void reverse_characterAction_pushShouldBecomeMove() {
        assertMotionTypeReversal(CharacterMotionType.Move, CharacterMotionType.Push);
    }

    @Test
    public void reverse_characterAction_addShouldBecomeRemove() {
        assertMotionTypeReversal(CharacterMotionType.Remove, CharacterMotionType.Add);
    }

    @Test
    public void reverse_characterAction_removeShouldBecomeAdd() {
        assertMotionTypeReversal(CharacterMotionType.Add, CharacterMotionType.Remove);
    }

    @Test
    public void reverse_characterAction_transformShouldBecomeUntransform() {
        assertMotionTypeReversal(CharacterMotionType.Untransform, CharacterMotionType.Transform);
    }

    @Test
    public void reverse_characterAction_untransformShouldBecomeTransform() {
        assertMotionTypeReversal(CharacterMotionType.Transform, CharacterMotionType.Untransform);
    }

    @Test
    public void reverse_characterAction_moveShouldStayMove() {
        assertMotionTypeReversal(CharacterMotionType.Move, CharacterMotionType.Move);
    }

    @Test
    public void reverse_characterAction_swapShouldStaySwap() {
        assertMotionTypeReversal(CharacterMotionType.Swap, CharacterMotionType.Swap);
    }

    @Test
    public void reverse_characterAction_teleportShouldStayTeleport() {
        assertMotionTypeReversal(CharacterMotionType.Teleport, CharacterMotionType.Teleport);
    }

    //endregion

    //region CharacterAction - structure

    @Test
    public void reverse_characterAction_shouldKeepSourceCharacter() {
        Character source = createCharacter(CharacterType.Illusionist, TeamColor.Black);
        CharacterAction action = characterAction(source,
                motion(CharacterMotionType.Move, target(source, POS_A, POS_B)));

        CharacterAction reversed = reverseCharacter(action);

        assertEquals(GameActionType.CharacterAction, reversed.getActionType());
        assertSame(source, reversed.getSrcCharacter());
    }

    @Test
    public void reverse_characterAction_shouldSwapOriginAndDestinationPositions() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)));

        CharacterActionTarget reversedTarget =
                reverseCharacter(action).getMotions().get(0).getTargets().get(0);

        assertSame(character, reversedTarget.getCharacter());
        assertSame(POS_B, reversedTarget.getOriginPos());
        assertSame(POS_A, reversedTarget.getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldSwapNullOriginIntoNullDestination() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        // Add: no origin, destination on the board.
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Add, target(character, null, POS_A)));

        CharacterActionTarget reversedTarget =
                reverseCharacter(action).getMotions().get(0).getTargets().get(0);

        assertSame(POS_A, reversedTarget.getOriginPos());
        assertNull(reversedTarget.getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldSwapNullDestinationIntoNullOrigin() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        // Remove: origin on the board, no destination.
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Remove, target(character, POS_A, null)));

        CharacterActionTarget reversedTarget =
                reverseCharacter(action).getMotions().get(0).getTargets().get(0);

        assertNull(reversedTarget.getOriginPos());
        assertSame(POS_A, reversedTarget.getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldKeepNullPositionsForTargetingOnlyMotion() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Teleport, target(character, null, null)));

        CharacterActionTarget reversedTarget =
                reverseCharacter(action).getMotions().get(0).getTargets().get(0);

        assertNull(reversedTarget.getOriginPos());
        assertNull(reversedTarget.getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldReverseTargetOrderWithinMotion() {
        Character first = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.White);
        // Push: the pushed character moves first, then the pusher takes its place.
        CharacterAction action = characterAction(first,
                motion(CharacterMotionType.Push,
                        target(second, POS_B, POS_C),
                        target(first, POS_A, POS_B)));

        List<CharacterActionTarget> reversedTargets =
                reverseCharacter(action).getMotions().get(0).getTargets();

        assertEquals(2, reversedTargets.size());
        // The pusher goes back first...
        assertSame(first, reversedTargets.get(0).getCharacter());
        assertSame(POS_B, reversedTargets.get(0).getOriginPos());
        assertSame(POS_A, reversedTargets.get(0).getDestPos());
        // ...then the pushed character goes back.
        assertSame(second, reversedTargets.get(1).getCharacter());
        assertSame(POS_C, reversedTargets.get(1).getOriginPos());
        assertSame(POS_B, reversedTargets.get(1).getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldReverseMotionOrder() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)),
                motion(CharacterMotionType.Move, target(character, POS_B, POS_C)),
                motion(CharacterMotionType.Remove, target(character, POS_C, null)));

        List<CharacterActionMotion> reversedMotions = reverseCharacter(action).getMotions();

        assertEquals(3, reversedMotions.size());

        // Last motion (Remove) is undone first, as an Add.
        assertEquals(CharacterMotionType.Add, reversedMotions.get(0).getMotionType());
        assertNull(reversedMotions.get(0).getTargets().get(0).getOriginPos());
        assertSame(POS_C, reversedMotions.get(0).getTargets().get(0).getDestPos());

        assertEquals(CharacterMotionType.Move, reversedMotions.get(1).getMotionType());
        assertSame(POS_C, reversedMotions.get(1).getTargets().get(0).getOriginPos());
        assertSame(POS_B, reversedMotions.get(1).getTargets().get(0).getDestPos());

        // First motion is undone last.
        assertEquals(CharacterMotionType.Move, reversedMotions.get(2).getMotionType());
        assertSame(POS_B, reversedMotions.get(2).getTargets().get(0).getOriginPos());
        assertSame(POS_A, reversedMotions.get(2).getTargets().get(0).getDestPos());
    }

    @Test
    public void reverse_characterAction_shouldHandleEmptyMotionList() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);

        CharacterAction reversed = reverseCharacter(new CharacterAction(character, new ArrayList<>()));

        assertSame(character, reversed.getSrcCharacter());
        assertTrue(reversed.getMotions().isEmpty());
    }

    @Test
    public void reverse_characterAction_shouldReturnNewInstanceAndNotModifyOriginal() {
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction original = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)));

        CharacterAction reversed = reverseCharacter(original);

        assertNotSame(original, reversed);
        CharacterActionTarget originalTarget = original.getMotions().get(0).getTargets().get(0);
        assertSame(POS_A, originalTarget.getOriginPos());
        assertSame(POS_B, originalTarget.getDestPos());
    }

    @Test
    public void reverse_characterAction_reversingTwiceShouldGiveBackOriginalStructure() {
        Character first = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.White);
        CharacterAction original = characterAction(first,
                motion(CharacterMotionType.Swap,
                        target(first, POS_A, POS_B),
                        target(second, POS_B, POS_A)),
                motion(CharacterMotionType.Remove, target(second, POS_A, null)));

        CharacterAction twice = reverseCharacter(reverseCharacter(original));

        assertSame(first, twice.getSrcCharacter());
        assertEquals(2, twice.getMotions().size());

        CharacterActionMotion swap = twice.getMotions().get(0);
        assertEquals(CharacterMotionType.Swap, swap.getMotionType());
        assertEquals(2, swap.getTargets().size());
        assertSame(first, swap.getTargets().get(0).getCharacter());
        assertSame(POS_A, swap.getTargets().get(0).getOriginPos());
        assertSame(POS_B, swap.getTargets().get(0).getDestPos());
        assertSame(second, swap.getTargets().get(1).getCharacter());
        assertSame(POS_B, swap.getTargets().get(1).getOriginPos());
        assertSame(POS_A, swap.getTargets().get(1).getDestPos());

        CharacterActionMotion remove = twice.getMotions().get(1);
        assertEquals(CharacterMotionType.Remove, remove.getMotionType());
        assertSame(second, remove.getTargets().get(0).getCharacter());
        assertSame(POS_A, remove.getTargets().get(0).getOriginPos());
        assertNull(remove.getTargets().get(0).getDestPos());
    }

    @Test
    public void reverse_characterAction_transformShouldReverseTargetsOrder() {
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = Character.transform(before, CharacterType.Frog, before.getTeamColor());
        CharacterAction action = characterAction(before,
                motion(CharacterMotionType.Transform,
                        target(before, POS_A, POS_A),
                        target(after, POS_A, POS_A)));

        CharacterActionMotion reversedMotion = reverseCharacter(action).getMotions().get(0);

        assertEquals(CharacterMotionType.Untransform, reversedMotion.getMotionType());
        assertSame(after, reversedMotion.getTargets().get(0).getCharacter());
        assertSame(before, reversedMotion.getTargets().get(1).getCharacter());
    }

    //endregion

    //region Reversal applied through handlers (end-to-end)

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoMove() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(POS_A).setCharacter(character);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)));

        new CharacterActionHandler(game, action).doAction();
        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertSame(character, game.getBoard().getCell(POS_A).getCharacter());
        assertNull(game.getBoard().getCell(POS_B).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoAdd() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Add, target(character, null, POS_A)));

        new CharacterActionHandler(game, action).doAction();
        assertSame(character, game.getBoard().getCell(POS_A).getCharacter());

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertNull(game.getBoard().getCell(POS_A).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoRemove() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(POS_A).setCharacter(character);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Remove, target(character, POS_A, null)));

        new CharacterActionHandler(game, action).doAction();
        assertNull(game.getBoard().getCell(POS_A).getCharacter());

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertSame(character, game.getBoard().getCell(POS_A).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoSwap() {
        Game game = createTestGame();
        Character first = createCharacter(CharacterType.Illusionist, TeamColor.Black);
        Character second = createCharacter(CharacterType.Assassin, TeamColor.Black);
        game.getBoard().getCell(POS_A).setCharacter(first);
        game.getBoard().getCell(POS_C).setCharacter(second);
        CharacterAction action = characterAction(first,
                motion(CharacterMotionType.Swap,
                        target(first, POS_A, POS_C),
                        target(second, POS_C, POS_A)));

        new CharacterActionHandler(game, action).doAction();
        assertSame(second, game.getBoard().getCell(POS_A).getCharacter());
        assertSame(first, game.getBoard().getCell(POS_C).getCharacter());

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertSame(first, game.getBoard().getCell(POS_A).getCharacter());
        assertSame(second, game.getBoard().getCell(POS_C).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoPush() {
        Game game = createTestGame();
        Character pusher = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character pushed = createCharacter(CharacterType.Assassin, TeamColor.White);
        game.getBoard().getCell(POS_A).setCharacter(pusher);
        game.getBoard().getCell(POS_B).setCharacter(pushed);
        CharacterAction action = characterAction(pusher,
                motion(CharacterMotionType.Push,
                        target(pushed, POS_B, POS_C),
                        target(pusher, POS_A, POS_B)));

        new CharacterActionHandler(game, action).doAction();
        assertNull(game.getBoard().getCell(POS_A).getCharacter());
        assertSame(pusher, game.getBoard().getCell(POS_B).getCharacter());
        assertSame(pushed, game.getBoard().getCell(POS_C).getCharacter());

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertSame(pusher, game.getBoard().getCell(POS_A).getCharacter());
        assertSame(pushed, game.getBoard().getCell(POS_B).getCharacter());
        assertNull(game.getBoard().getCell(POS_C).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoSeveralMotions() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(POS_A).setCharacter(character);
        CharacterAction action = characterAction(character,
                motion(CharacterMotionType.Move, target(character, POS_A, POS_B)),
                motion(CharacterMotionType.Move, target(character, POS_B, POS_C)),
                motion(CharacterMotionType.Remove, target(character, POS_C, null)));

        new CharacterActionHandler(game, action).doAction();
        assertNull(game.getBoard().getCell(POS_A).getCharacter());
        assertNull(game.getBoard().getCell(POS_B).getCharacter());
        assertNull(game.getBoard().getCell(POS_C).getCharacter());

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertSame(character, game.getBoard().getCell(POS_A).getCharacter());
        assertNull(game.getBoard().getCell(POS_B).getCharacter());
        assertNull(game.getBoard().getCell(POS_C).getCharacter());
    }

    @Test
    public void reverse_characterAction_applyingReverseShouldUndoTransform() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = Character.transform(before, CharacterType.Frog, before.getTeamColor());
        game.getBoard().getCell(POS_A).setCharacter(before);
        CharacterAction action = characterAction(before,
                motion(CharacterMotionType.Transform,
                        target(before, POS_A, POS_A),
                        target(after, POS_A, POS_A)));

        new CharacterActionHandler(game, action).doAction();
        assertNotNull(game.getTransformedState(before.getId()));

        new CharacterActionHandler(game, reverseCharacter(action)).doAction();

        assertNull(game.getTransformedState(before.getId()));
    }

    @Test
    public void reverse_recruitmentAction_applyingReverseShouldUndoRecruitment() {
        Game game = createTestGame();
        game.getRecruitableCards().add(CharacterCard.Archer);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = new RecruitmentAction(
                List.of(recruitmentMotion(RecruitmentMotionType.Add, character, POS_A)));

        new RecruitmentActionHandler(game, action).doAction();
        assertSame(character, game.getBoard().getCell(POS_A).getCharacter());
        assertTrue(game.getRecruitableCards().isEmpty());

        new RecruitmentActionHandler(game, reverseRecruitment(action)).doAction();

        assertNull(game.getBoard().getCell(POS_A).getCharacter());
        assertTrue(game.getRecruitedCards(TeamColor.Black).isEmpty());
        assertEquals(List.of(CharacterCard.Archer), game.getRecruitableCards());
    }

    @Test
    public void reverse_warningAction_applyingReverseShouldRestoreWarningCount() {
        Game game = createTestGame();
        game.setPlayerWarningCount(TeamColor.Black, WarningType.Barrage, 2);
        WarningAction action = new WarningAction(WarningType.Barrage, TeamColor.Black, 1);

        new WarningActionHandler(game, action).doAction();
        assertEquals(3, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));

        new WarningActionHandler(game, reverseWarning(action)).doAction();

        assertEquals(2, game.getPlayerWarningCount(TeamColor.Black, WarningType.Barrage));
    }

    //endregion
}