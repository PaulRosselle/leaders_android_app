package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
import java.util.List;

public class CharacterActionHandlerTest {

    private Game createTestGame() {
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new ArrayList<>(), // banishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
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
                List.of(new CharacterActionMotion(CharacterMotionType.Transform,
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
}