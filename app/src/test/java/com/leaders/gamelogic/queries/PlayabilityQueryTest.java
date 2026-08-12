package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPlayableState;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;
import com.leaders.gamelogic.actions.CharacterAction;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class PlayabilityQueryTest {

    private Game createTestGame(Board board) {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(board,
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private GameHistory createTestGameHistory() {
        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        return new GameHistory(new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        ), new ArrayList<>());
    }

    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
    }

    private ActionsPhase createActionsPhase() {
        ActionsPhase actionsPhase = new ActionsPhase(null, null, TeamColor.Black);
        actionsPhase.start();
        return actionsPhase;
    }

    private Turn createTurn(ActionsPhase actionsPhase) {
        return new Turn(
                null,
                null,
                TeamColor.Black,
                new TurnStartPhase(null, null, TeamColor.Black),
                actionsPhase,
                new RecruitmentPhase(null, null, TeamColor.Black),
                new TurnEndPhase(null, null, TeamColor.Black)
        );
    }

    @Test(expected = IllegalStateException.class)
    public void getCharacterPlayableStates_shouldThrowWhenCurrentPhaseIsNotActionsPhase() {
        Game game = createTestGame(new Board());
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(
                new BanishmentPhase(null, null, TeamColor.Black)
        );

        PlayabilityQuery.getCharacterPlayableStates(game, gameHistory);
    }

    @Test
    public void getCharacterPlayableStates_shouldReturnEmptyListWhenNoCharacterCanAct() {
        Board board = new Board();
        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        assertTrue(
                PlayabilityQuery.getCharacterPlayableStates(game, gameHistory).isEmpty()
        );
    }

    @Test
    public void getCharacterPlayableStates_shouldReturnCharacterThatCanAct() {
        Board board = new Board();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(character);

        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        List<CharacterPlayableState> states =
                PlayabilityQuery.getCharacterPlayableStates(game, gameHistory);

        assertEquals(1, states.size());
        assertSame(character, states.get(0).getCharacter());
        assertFalse(states.get(0).isMandatory());
    }

    @Test
    public void getCharacterPlayableStates_shouldNotReturnCharacterThatAlreadyActed() {
        Board board = new Board();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(character);

        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();
        actionsPhase.getActions().add(new CharacterAction(character, new ArrayList<>()));

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        assertTrue(
                PlayabilityQuery.getCharacterPlayableStates(game, gameHistory).isEmpty()
        );
    }

    @Test
    public void getCharacterPlayableStates_shouldReturnOnlyCharactersAllowedToAct() {
        Board board = new Board();

        Character blackCharacter = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        Character whiteCharacter = createCharacter(CharacterType.Acrobat, TeamColor.White);

        board.getCell(new Position(3, 3)).setCharacter(blackCharacter);
        board.getCell(new Position(3, 4)).setCharacter(whiteCharacter);

        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        List<CharacterPlayableState> states =
                PlayabilityQuery.getCharacterPlayableStates(game, gameHistory);

        assertEquals(1, states.size());
        assertSame(blackCharacter, states.get(0).getCharacter());
    }

    @Test
    public void getCharacterPlayableStates_shouldReturnOnlyCharacterThatMustActNow() {
        Board board = new Board();

        Character leader = createCharacter(CharacterType.LeaderKing, TeamColor.White);
        Character nemesis = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.Black);

        board.getCell(new Position(3, 2)).setCharacter(leader);
        board.getCell(new Position(3, 3)).setCharacter(nemesis);
        board.getCell(new Position(3, 4)).setCharacter(acrobat);

        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();

        /*
         * The exact Nemesis trigger is already covered by CharacterAbilityQueryTest.
         * Here we only need to verify PlayabilityQuery's contract:
         * when mustActNow() is true, this character is returned alone.
         */
        CharacterAction leaderAction = new CharacterAction(leader,
                List.of(new CharacterActionMotion(CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(
                                leader,
                                new Position(3, 1),
                                new Position(3, 2)
                        ))
                ))
        );
        actionsPhase.getActions().add(leaderAction);

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        List<CharacterPlayableState> states =
                PlayabilityQuery.getCharacterPlayableStates(game, gameHistory);

        assertEquals(1, states.size());
        assertSame(nemesis, states.get(0).getCharacter());
        assertTrue(states.get(0).isMandatory());
    }

    @Test
    public void getCharacterPlayableStates_shouldExposeActiveAbilityAvailability() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(bruiser);

        Game game = createTestGame(board);
        ActionsPhase actionsPhase = createActionsPhase();

        Turn turn = createTurn(actionsPhase);
        GameHistory gameHistory = createTestGameHistory();
        gameHistory.getEntries().add(turn);

        List<CharacterPlayableState> states = PlayabilityQuery.getCharacterPlayableStates(game, gameHistory);

        assertEquals(1, states.size());
        assertSame(bruiser, states.get(0).getCharacter());
        assertTrue(states.get(0).canUseActiveAbility());
    }
}