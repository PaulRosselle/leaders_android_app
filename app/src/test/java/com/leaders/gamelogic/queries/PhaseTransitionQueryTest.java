package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;

public class PhaseTransitionQueryTest {

    private Game createTestGame() {
        return new Game(
                new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private GameHistory createTestGameHistory(GameMode gameMode) {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        return new GameHistory(
                new GameConfig(
                        players,
                        players.get(1), // firstPlayer
                        gameMode,
                        new ArrayList<>(), // initialRecruitableCards
                        new ArrayList<>() // initialPlacements
                ),
                new ArrayList<>()
        );
    }

    private Turn createTestTurn() {
        return new Turn(TeamColor.Black);
    }

    private GamePhase getNextPhase(GameHistory history) {
        return PhaseTransitionQuery.getNextPhase(createTestGame(), history);
    }

    @Test
    public void getNextPhase_shouldReturnTurnStartForFirstPlayerInDiscoveryMode() {
        GameHistory history = createTestGameHistory(GameMode.Discovery);

        GamePhase nextPhase = getNextPhase(history);

        assertEquals(GamePhaseType.TurnStart, nextPhase.getPhaseType());
        assertEquals(TeamColor.White, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnBanishmentForOppositePlayerInStrategistMode() {
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        GamePhase nextPhase = getNextPhase(history);

        assertEquals(GamePhaseType.Banishment, nextPhase.getPhaseType());
        assertEquals(TeamColor.Black, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnActionsAfterTurnStart() {
        GameHistory history = createTestGameHistory(GameMode.Discovery);
        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        TurnStartPhase turnStartPhase = (TurnStartPhase) turn.getSubPhasesInOrder()[0];
        turnStartPhase.start();
        turnStartPhase.end();

        GamePhase nextPhase = getNextPhase(history);

        assertEquals(GamePhaseType.Actions, nextPhase.getPhaseType());
        assertEquals(TeamColor.Black, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnRecruitmentWhenRecruitmentIsPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Discovery);
        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhasesInOrder()[1];
        actionsPhase.start();
        actionsPhase.end();

        RecruitmentPhase recruitmentPhase =
                (RecruitmentPhase) turn.getSubPhasesInOrder()[2];
        recruitmentPhase.start();

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.Recruitment, nextPhase.getPhaseType());
        assertEquals(TeamColor.Black, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnTurnEndWhenRecruitmentIsNotPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Discovery);
        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhasesInOrder()[1];
        actionsPhase.start();
        actionsPhase.end();

        RecruitmentPhase recruitmentPhase =
                (RecruitmentPhase) turn.getSubPhasesInOrder()[2];
        recruitmentPhase.start();
        recruitmentPhase.getActions().add(new RecruitmentAction(new ArrayList<>()));

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.TurnEnd, nextPhase.getPhaseType());
        assertEquals(TeamColor.Black, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnTurnEndAfterRecruitment() {
        GameHistory history = createTestGameHistory(GameMode.Discovery);
        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        RecruitmentPhase recruitmentPhase =
                (RecruitmentPhase) turn.getSubPhasesInOrder()[2];
        recruitmentPhase.start();
        recruitmentPhase.end();

        GamePhase nextPhase = getNextPhase(history);

        assertEquals(GamePhaseType.TurnEnd, nextPhase.getPhaseType());
        assertEquals(TeamColor.Black, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnBanishmentForOppositePlayerWhenBanishmentIsPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        TurnEndPhase turnEndPhase =
                (TurnEndPhase) turn.getSubPhasesInOrder()[3];
        turnEndPhase.start();
        turnEndPhase.end();

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.Banishment, nextPhase.getPhaseType());
        assertEquals(TeamColor.White, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnTurnStartForOppositePlayerWhenBanishmentIsNotPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Discovery);

        Turn turn = createTestTurn();
        history.getEntries().add(turn);

        TurnEndPhase turnEndPhase =
                (TurnEndPhase) turn.getSubPhasesInOrder()[3];
        turnEndPhase.start();
        turnEndPhase.end();

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.TurnStart, nextPhase.getPhaseType());
        assertEquals(TeamColor.White, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnBanishmentForOppositePlayerAfterBanishmentWhenPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        BanishmentPhase banishmentPhase =
                new BanishmentPhase(TeamColor.Black);
        banishmentPhase.start();
        banishmentPhase.end();
        history.getEntries().add(banishmentPhase);

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.Banishment, nextPhase.getPhaseType());
        assertEquals(TeamColor.White, nextPhase.getPhasePlayer().getTeamColor());
    }

    @Test
    public void getNextPhase_shouldReturnTurnStartForOppositePlayerAfterBanishmentWhenNotPossible() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Discovery);

        BanishmentPhase banishmentPhase =
                new BanishmentPhase(TeamColor.Black);
        banishmentPhase.start();
        banishmentPhase.end();
        history.getEntries().add(banishmentPhase);

        GamePhase nextPhase = PhaseTransitionQuery.getNextPhase(game, history);

        assertEquals(GamePhaseType.TurnStart, nextPhase.getPhaseType());
        assertEquals(TeamColor.White, nextPhase.getPhasePlayer().getTeamColor());
    }

    private void addRecruitedCharacter(Game game, CharacterType characterType) {
        game.getRecruitedCharacters().add(
                Character.create(characterType, TeamColor.White)
        );
    }
}