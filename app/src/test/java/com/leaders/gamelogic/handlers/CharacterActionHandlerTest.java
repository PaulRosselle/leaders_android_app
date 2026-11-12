package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

public class CharacterActionHandlerTest {

    private static final Position POSITION = new Position(3, 3);

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
    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
    }

    @NonNull
    private Character transformCharacter(@NonNull Character before) {
        return Character.transform(before, CharacterType.Frog, before.getTeamColor());
    }

    @NonNull
    private CharacterAction createTransformAction(@NonNull CharacterMotionType motionType,
                                                  @NonNull Character before,
                                                  @NonNull Character after) {
        return new CharacterAction(before,
                List.of(new CharacterActionMotion(motionType,
                        List.of(
                                new CharacterActionTarget(before, POSITION, POSITION),
                                new CharacterActionTarget(after, POSITION, POSITION)
                        )
                ))
        );
    }


    @Test
    public void doAction_shouldMoveCharacter() {
        Game game = createTestGame();
        Position originPosition = new Position(3, 3);
        Position destinationPosition = new Position(3, 4);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(originPosition).setCharacter(character);

        assertNull(game.getBoard().getCell(destinationPosition).getCharacter());
        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(
                                character,
                                originPosition,
                                destinationPosition
                        ))
                ))
        );
        new CharacterActionHandler(game, action).doAction();

        assertNull(game.getBoard().getCell(originPosition).getCharacter());
        assertSame(character, game.getBoard().getCell(destinationPosition).getCharacter());
    }

    @Test
    public void doAction_shouldAddCharacterToBoard() {
        Game game = createTestGame();
        Position destinationPosition = new Position(3, 3);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);

        assertNull(game.getBoard().getCell(destinationPosition).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Add,
                        List.of(new CharacterActionTarget(
                                character,
                                null,
                                destinationPosition
                        ))
                ))
        );

        new CharacterActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(destinationPosition).getCharacter());
    }

    @Test
    public void doAction_shouldRemoveCharacterFromBoard() {
        Game game = createTestGame();
        Position originPosition = new Position(3, 3);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(originPosition).setCharacter(character);

        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Remove,
                        List.of(new CharacterActionTarget(
                                character,
                                originPosition,
                                null
                        ))
                ))
        );
        new CharacterActionHandler(game, action).doAction();

        assertNull(game.getBoard().getCell(originPosition).getCharacter());
    }

    @Test
    public void doAction_shouldNotModifyBoardForTargetingOnlyAction() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(position).setCharacter(character);

        assertSame(character, game.getBoard().getCell(position).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Teleport,
                        List.of(new CharacterActionTarget(
                                character,
                                null,
                                null
                        ))
                ))
        );

        new CharacterActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(position).getCharacter());
    }

    @Test
    public void doAction_shouldHandleCharacterSwap() {
        Game game = createTestGame();
        Position firstPosition = new Position(3, 6);
        Position secondPosition = new Position(3, 0);

        Character firstCharacter = createCharacter(CharacterType.Illusionist, TeamColor.Black);
        Character secondCharacter = createCharacter(CharacterType.Assassin, TeamColor.Black);

        game.getBoard().getCell(firstPosition).setCharacter(firstCharacter);
        game.getBoard().getCell(secondPosition).setCharacter(secondCharacter);

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());

        List<CharacterActionTarget> targets = List.of(
                new CharacterActionTarget(
                        firstCharacter,
                        firstPosition,
                        secondPosition
                ),
                new CharacterActionTarget(
                        secondCharacter,
                        secondPosition,
                        firstPosition
                )
        );

        CharacterAction action = new CharacterAction(firstCharacter,
                List.of(new CharacterActionMotion(CharacterMotionType.Swap, targets)));

        new CharacterActionHandler(game, action).doAction();

        assertSame(secondCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(firstCharacter, game.getBoard().getCell(secondPosition).getCharacter());
    }

    @Test
    public void doActionThenUndoAction_shouldHandleMultipleMovements() {
        Game game = createTestGame();

        Position firstPosition = new Position(3, 3);
        Position secondPosition = new Position(3, 4);
        Position thirdPosition = new Position(3, 5);

        Character firstCharacter = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character secondCharacter = createCharacter(CharacterType.Assassin, TeamColor.White);

        game.getBoard().getCell(firstPosition).setCharacter(firstCharacter);
        game.getBoard().getCell(secondPosition).setCharacter(secondCharacter);

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());
        assertNull(game.getBoard().getCell(thirdPosition).getCharacter());

        CharacterAction action = new CharacterAction(firstCharacter,
                List.of(new CharacterActionMotion(CharacterMotionType.Push,
                        List.of(
                                new CharacterActionTarget(
                                        secondCharacter,
                                        secondPosition,
                                        thirdPosition
                                ),
                                new CharacterActionTarget(
                                        firstCharacter,
                                        firstPosition,
                                        secondPosition
                                )
                        )
                ))
        );

        CharacterActionHandler handler = new CharacterActionHandler(game, action);

        handler.doAction();

        assertNull(game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(firstCharacter, game.getBoard().getCell(secondPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(thirdPosition).getCharacter());
    }

    @Test
    public void undoAction_shouldRestoreCharacterAfterMove() {
        Game game = createTestGame();
        Position originPosition = new Position(3, 3);
        Position destinationPosition = new Position(3, 4);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(originPosition).setCharacter(character);

        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());
        assertNull(game.getBoard().getCell(destinationPosition).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(
                                character,
                                originPosition,
                                destinationPosition
                        ))
                ))
        );

        CharacterActionHandler handler = new CharacterActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());
        assertNull(game.getBoard().getCell(destinationPosition).getCharacter());
    }

    @Test
    public void undoAction_shouldRestoreRemovedCharacter() {
        Game game = createTestGame();
        Position originPosition = new Position(3, 3);
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(originPosition).setCharacter(character);

        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Remove,
                        List.of(new CharacterActionTarget(
                                character,
                                originPosition,
                                null
                        ))
                ))
        );

        CharacterActionHandler handler = new CharacterActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertSame(character, game.getBoard().getCell(originPosition).getCharacter());
    }

    @Test
    public void undoAction_shouldRestoreCharacterSwap() {
        Game game = createTestGame();
        Position firstPosition = new Position(3, 6);
        Position secondPosition = new Position(3, 0);

        Character firstCharacter = createCharacter(CharacterType.Illusionist, TeamColor.Black);
        Character secondCharacter = createCharacter(CharacterType.Assassin, TeamColor.Black);

        game.getBoard().getCell(firstPosition).setCharacter(firstCharacter);
        game.getBoard().getCell(secondPosition).setCharacter(secondCharacter);

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());

        List<CharacterActionTarget> targets = List.of(
                new CharacterActionTarget(
                        firstCharacter,
                        firstPosition,
                        secondPosition
                ),
                new CharacterActionTarget(
                        secondCharacter,
                        secondPosition,
                        firstPosition
                )
        );

        CharacterAction action = new CharacterAction(firstCharacter,
                List.of(new CharacterActionMotion(CharacterMotionType.Swap, targets)));

        CharacterActionHandler handler = new CharacterActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();

        Position firstPosition = new Position(3, 3);
        Position secondPosition = new Position(3, 4);
        Position thirdPosition = new Position(3, 5);

        Character firstCharacter = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character secondCharacter = createCharacter(CharacterType.Assassin, TeamColor.White);

        game.getBoard().getCell(firstPosition).setCharacter(firstCharacter);
        game.getBoard().getCell(secondPosition).setCharacter(secondCharacter);

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());
        assertNull(game.getBoard().getCell(thirdPosition).getCharacter());

        CharacterAction action = new CharacterAction(firstCharacter,
                List.of(new CharacterActionMotion(CharacterMotionType.Remove,
                        List.of(
                                new CharacterActionTarget(
                                        secondCharacter,
                                        secondPosition,
                                        thirdPosition
                                ),
                                new CharacterActionTarget(
                                        firstCharacter,
                                        firstPosition,
                                        secondPosition
                                )
                        )
                ))
        );

        CharacterActionHandler handler = new CharacterActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertSame(firstCharacter, game.getBoard().getCell(firstPosition).getCharacter());
        assertSame(secondCharacter, game.getBoard().getCell(secondPosition).getCharacter());
        assertNull(game.getBoard().getCell(thirdPosition).getCharacter());
    }

    //region TRANSFORMATION

    @Test
    public void doAction_shouldAddTransformedStateOnTransform() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);
        game.getBoard().getCell(POSITION).setCharacter(before);

        assertNull(game.getTransformedState(before.getId()));

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Transform, before, after)).doAction();

        assertNotNull(game.getTransformedState(before.getId()));
    }

    @Test
    public void doAction_shouldRemoveTransformedStateOnUntransform() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);
        game.getBoard().getCell(POSITION).setCharacter(after);
        game.addTransformedState(before, after);

        assertNotNull(game.getTransformedState(before.getId()));

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Untransform, after, before)).doAction();

        assertNull(game.getTransformedState(before.getId()));
    }

    public void doAction_shouldThrowWhenTransformHasMoreThanOneMotion() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);

        CharacterActionMotion transformMotion = new CharacterActionMotion(CharacterMotionType.Transform,
                List.of(
                        new CharacterActionTarget(before, POSITION, POSITION),
                        new CharacterActionTarget(after, POSITION, POSITION)
                ));
        CharacterActionMotion otherMotion = new CharacterActionMotion(CharacterMotionType.Move,
                List.of(new CharacterActionTarget(before, POSITION, new Position(3, 4))));

        new CharacterActionHandler(game,
                new CharacterAction(before, List.of(transformMotion, otherMotion))).doAction();

        assertNotNull(game.getTransformedState(before.getId()));
    }

    @Test(expected = IllegalStateException.class)
    public void doAction_shouldThrowWhenTransformingAnAlreadyTransformedCharacter() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);
        game.addTransformedState(before, after);

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Transform, before, after)).doAction();
    }

    @Test(expected = IllegalStateException.class)
    public void doAction_shouldThrowWhenUntransformingANonTransformedCharacter() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Untransform, after, before)).doAction();
    }

    @Test(expected = IllegalStateException.class)
    public void doAction_shouldThrowWhenTransformDoesNotHaveExactlyTwoTargets() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);

        CharacterAction action = new CharacterAction(before,
                List.of(new CharacterActionMotion(CharacterMotionType.Transform,
                        List.of(new CharacterActionTarget(before, POSITION, POSITION)))));

        new CharacterActionHandler(game, action).doAction();
    }

    @Test(expected = IllegalStateException.class)
    public void doAction_shouldThrowWhenTransformTargetsAreDifferentCharacters() {
        Game game = createTestGame();
        Character first = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character second = createCharacter(CharacterType.Bruiser, TeamColor.Black); // id différent

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Transform, first, second)).doAction();
    }

    @Test
    public void doAction_shouldNotTouchTransformedStateForNonTransformMotion() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        Position destination = new Position(3, 4);
        game.getBoard().getCell(POSITION).setCharacter(character);

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(character, POSITION, destination)))));

        new CharacterActionHandler(game, action).doAction();

        assertNull(game.getTransformedState(character.getId()));
        assertSame(character, game.getBoard().getCell(destination).getCharacter());
    }

    @Test
    public void undoAction_shouldRemoveTransformedStateAfterTransform() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);
        game.getBoard().getCell(POSITION).setCharacter(before);

        CharacterActionHandler handler = new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Transform, before, after));

        handler.doAction();
        assertNotNull(game.getTransformedState(before.getId()));

        handler.undoAction();
        assertNull(game.getTransformedState(before.getId()));
    }

    @Test(expected = IllegalStateException.class)
    public void undoAction_shouldThrowWhenCharacterIsNotTransformed() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);

        new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Transform, before, after)).undoAction();
    }

    @Test
    public void undoAction_shouldRestoreTransformedStateAfterUntransform() {
        Game game = createTestGame();
        Character before = createCharacter(CharacterType.Archer, TeamColor.Black);
        Character after = transformCharacter(before);
        game.getBoard().getCell(POSITION).setCharacter(after);
        game.addTransformedState(before, after);

        CharacterActionHandler handler = new CharacterActionHandler(game,
                createTransformAction(CharacterMotionType.Untransform, after, before));

        handler.doAction();
        assertNull(game.getTransformedState(before.getId()));

        handler.undoAction();
        assertNotNull(game.getTransformedState(before.getId()));
    }

    @Test
    public void doActionThenUndoAction_shouldLeaveTransformedStateUnchangedForNonTransformMotion() {
        Game game = createTestGame();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        game.getBoard().getCell(POSITION).setCharacter(character);

        CharacterAction action = new CharacterAction(character,
                List.of(new CharacterActionMotion(CharacterMotionType.Remove,
                        List.of(new CharacterActionTarget(character, POSITION, null)))));

        CharacterActionHandler handler = new CharacterActionHandler(game, action);
        handler.doAction();
        handler.undoAction();

        assertNull(game.getTransformedState(character.getId()));
        assertSame(character, game.getBoard().getCell(POSITION).getCharacter());
    }

    //endregion
}