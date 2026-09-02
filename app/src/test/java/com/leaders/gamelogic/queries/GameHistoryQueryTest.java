package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;

import org.junit.Test;

import java.util.ArrayList;

public class GameHistoryQueryTest {

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

    private Turn createTestTurn() {
        return new Turn(TeamColor.Black);
    }

    @Test
    public void findCurrentTurn_shouldReturnLastTurn() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();
        gameHistory.getEntries().add(turn);

        assertSame(turn, GameHistoryQuery.findCurrentTurn(gameHistory));
    }

    @Test
    public void findCurrentTurn_shouldReturnNullWhenHistoryIsEmpty() {
        GameHistory gameHistory = createTestGameHistory();

        assertNull(GameHistoryQuery.findCurrentTurn(gameHistory));
    }

    @Test
    public void findCurrentTurn_shouldReturnNullWhenLastEntryIsNotATurn() {
        GameHistory gameHistory = createTestGameHistory();
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        gameHistory.getEntries().add(phase);

        assertNull(GameHistoryQuery.findCurrentTurn(gameHistory));
    }

    @Test
    public void findCurrentTurn_shouldReturnNullWhenTurnIsNotLastEntry() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();
        gameHistory.getEntries().add(turn);
        gameHistory.getEntries().add(new BanishmentPhase(TeamColor.Black));

        assertNull(GameHistoryQuery.findCurrentTurn(gameHistory));
    }

    @Test
    public void findCurrentPhase_shouldReturnLastPhaseWhenLastEntryIsPhase() {
        GameHistory gameHistory = createTestGameHistory();
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        gameHistory.getEntries().add(phase);
        phase.start();

        assertSame(phase, GameHistoryQuery.findCurrentPhase(gameHistory));
    }

    @Test
    public void findCurrentPhase_shouldReturnActivePhase() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();
        TurnPhase[] subPhases = turn.getSubPhasesInOrder();
        gameHistory.getEntries().add(turn);

        subPhases[0].start();
        assertSame(subPhases[0], GameHistoryQuery.findCurrentPhase(gameHistory));

        subPhases[1].start();
        assertSame(subPhases[0], GameHistoryQuery.findCurrentPhase(gameHistory));

        subPhases[0].end();
        assertSame(subPhases[1], GameHistoryQuery.findCurrentPhase(gameHistory));
    }

    @Test
    public void findCurrentPhase_shouldReturnNullWhenNoPhaseIsActive() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();
        gameHistory.getEntries().add(turn);

        assertNull(GameHistoryQuery.findCurrentPhase(gameHistory));

        TurnPhase[] subPhases = turn.getSubPhasesInOrder();
        subPhases[0].start();
        subPhases[0].end();

        assertNull(GameHistoryQuery.findCurrentPhase(gameHistory));
    }

    @Test
    public void findCurrentPhase_shouldReturnNullWhenAllPhasesAreEnded() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();

        for (int i = 0; i < turn.getSubPhasesInOrder().length; i++) {
            turn.getSubPhasesInOrder()[i].start();
            turn.getSubPhasesInOrder()[i].end();
        }

        gameHistory.getEntries().add(turn);

        assertNull(GameHistoryQuery.findCurrentPhase(gameHistory));
    }

    @Test
    public void findLastEndedPhase_shouldReturnLastPhaseWhenLastEntryIsPhase() {
        GameHistory gameHistory = createTestGameHistory();
        BanishmentPhase phase = new BanishmentPhase(TeamColor.Black);
        phase.start();
        phase.end();
        gameHistory.getEntries().add(phase);

        assertSame(phase, GameHistoryQuery.findLastEndedPhase(gameHistory));
    }

    @Test
    public void findLastEndedPhase_shouldReturnMostRecentlyEndedPhase() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();

        TurnStartPhase turnStartPhase = (TurnStartPhase) turn.getSubPhasesInOrder()[0];
        ActionsPhase actionsPhase = (ActionsPhase) turn.getSubPhasesInOrder()[1];

        turnStartPhase.start();
        turnStartPhase.end();
        actionsPhase.start();
        actionsPhase.end();

        gameHistory.getEntries().add(turn);

        assertSame(actionsPhase, GameHistoryQuery.findLastEndedPhase(gameHistory));
    }

    @Test
    public void findLastEndedPhase_shouldReturnNullWhenNoPhaseHasEnded() {
        GameHistory gameHistory = createTestGameHistory();
        Turn turn = createTestTurn();

        gameHistory.getEntries().add(turn);

        assertNull(GameHistoryQuery.findLastEndedPhase(gameHistory));
    }

    @Test
    public void findLastEndedPhase_shouldReturnNullWhenHistoryIsEmpty() {
        GameHistory gameHistory = createTestGameHistory();

        assertNull(GameHistoryQuery.findLastEndedPhase(gameHistory));
    }
}