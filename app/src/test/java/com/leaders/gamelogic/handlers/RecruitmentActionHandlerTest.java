package com.leaders.gamelogic.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class RecruitmentActionHandlerTest {

    private Game createTestGame() {
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new ArrayList<>(), // banishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private RecruitmentAction createRecruitmentAction(@NonNull Character character, @NonNull Position position) {
        return new RecruitmentAction(List.of(new RecruitmentActionTarget(character, position)));
    }

    @Test
    public void doAction_shouldPlaceCharacterOnBoard() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, position);

        new RecruitmentActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(position).getCharacter());
    }

    @Test
    public void doAction_shouldRemoveCharacterCardFromRecruitableCards() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        game.getRecruitableCards().add(CharacterCard.Archer);

        RecruitmentAction action = createRecruitmentAction(character, position);

        new RecruitmentActionHandler(game, action).doAction();

        assertEquals(0, game.getRecruitableCards().size());
    }

    @Test
    public void doAction_shouldAddCharacterToRecruitedCharacters() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, position);

        new RecruitmentActionHandler(game, action).doAction();

        assertEquals(1, game.getRecruitedCharacters().size());
        assertSame(character, game.getRecruitedCharacters().get(0));
    }

    @Test
    public void undoAction_shouldRemoveCharacterFromBoard() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, position);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertNull(game.getBoard().getCell(position).getCharacter());
    }

    @Test
    public void undoAction_shouldRemoveCharacterFromRecruitedCharacters() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, position);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(0, game.getRecruitedCharacters().size());
    }

    @Test
    public void undoAction_shouldRestoreCharacterCardToRecruitableCards() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        game.getRecruitableCards().add(CharacterCard.Archer);

        RecruitmentAction action = createRecruitmentAction(character, position);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        handler.doAction();
        handler.undoAction();

        assertEquals(1, game.getRecruitableCards().size());
        assertEquals(CharacterCard.Archer, game.getRecruitableCards().get(0));
    }

    @Test
    public void undoAction_shouldNotAddDuplicateCharacterCard() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        game.getRecruitableCards().add(CharacterCard.Archer);

        RecruitmentAction action = createRecruitmentAction(character, position);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        handler.doAction();
        game.getRecruitableCards().add(CharacterCard.Archer);
        handler.undoAction();

        assertEquals(1, game.getRecruitableCards().size());
        assertEquals(CharacterCard.Archer, game.getRecruitableCards().get(0));
    }

    @Test
    public void doAction_shouldNotFailWhenCharacterCardIsNotInRecruitableCards() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Cub, TeamColor.Black);
        RecruitmentAction action = createRecruitmentAction(character, position);

        new RecruitmentActionHandler(game, action).doAction();

        assertSame(character, game.getBoard().getCell(position).getCharacter());
        assertEquals(1, game.getRecruitedCharacters().size());
        assertSame(character, game.getRecruitedCharacters().get(0));
    }

    @Test
    public void doActionThenUndoAction_shouldRestoreGameState() {
        Game game = createTestGame();
        Position position = new Position(3, 3);
        Character character = Character.create(CharacterType.Archer, TeamColor.Black);
        game.getRecruitableCards().add(CharacterCard.Archer);

        RecruitmentAction action = createRecruitmentAction(character, position);
        RecruitmentActionHandler handler = new RecruitmentActionHandler(game, action);

        Board boardBefore = new Board(game.getBoard());
        ArrayList<CharacterCard> recruitableCardsBefore = new ArrayList<>(game.getRecruitableCards());
        ArrayList<Character> recruitedCharactersBefore = new ArrayList<>(game.getRecruitedCharacters());

        handler.doAction();
        handler.undoAction();

        for (Position boardPosition : game.getBoard().getCells().keySet()) {
            assertEquals(
                    boardBefore.getCell(boardPosition).getCharacter(),
                    game.getBoard().getCell(boardPosition).getCharacter()
            );
        }

        assertEquals(recruitableCardsBefore, game.getRecruitableCards());
        assertEquals(recruitedCharactersBefore, game.getRecruitedCharacters());
    }
}