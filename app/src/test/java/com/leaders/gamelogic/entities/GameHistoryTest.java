package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.Segment;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;

public class GameHistoryTest {

    private GameConfig createTestGameConfig() {

        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        return new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        );
    }

    private Turn createTestTurn() {
        return new Turn(TeamColor.Black);
    }

    private BanishmentPhase createTestBanishmentPhase() {
        return new BanishmentPhase(TeamColor.Black);
    }

    @Test
    public void constructor_shouldStoreConfigAndEntries() {
        GameConfig config = createTestGameConfig();
        ArrayList<IHistoryEntry> entries = new ArrayList<>();

        GameHistory history = new GameHistory(config, entries);

        assertSame(config, history.getConfig());
        assertSame(entries, history.getEntries());
    }

    @Test
    public void copy_shouldShareImmutableConfig() {
        GameConfig config = createTestGameConfig();
        ArrayList<IHistoryEntry> entries = new ArrayList<>();

        GameHistory original = new GameHistory(config, entries);
        GameHistory copy = new GameHistory(original);

        assertSame(
                original.getConfig(),
                copy.getConfig()
        );
    }

    @Test
    public void copy_shouldCreateIndependentEntriesList() {
        GameHistory original = new GameHistory(
                createTestGameConfig(),
                new ArrayList<>()
        );

        GameHistory copy = new GameHistory(original);

        assertNotSame(
                original.getEntries(),
                copy.getEntries()
        );
    }

    @Test
    public void copy_shouldCopyTurn() {
        Turn turn = createTestTurn();

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(turn);

        GameHistory original = new GameHistory(
                createTestGameConfig(),
                entries
        );

        GameHistory copy = new GameHistory(original);

        assertEquals(1, copy.getEntries().size());
        assertNotSame(
                turn,
                copy.getEntries().get(0)
        );
    }

    @Test
    public void copy_shouldCopyBanishmentPhase() {
        BanishmentPhase phase = createTestBanishmentPhase();

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(phase);

        GameHistory original = new GameHistory(
                createTestGameConfig(),
                entries
        );

        GameHistory copy = new GameHistory(original);

        assertEquals(1, copy.getEntries().size());
        assertNotSame(
                phase,
                copy.getEntries().get(0)
        );
    }

    @Test
    public void copy_shouldNotShareTurnPhases() {
        Turn turn = createTestTurn();

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(turn);

        GameHistory original = new GameHistory(
                createTestGameConfig(),
                entries
        );

        GameHistory copy = new GameHistory(original);

        Turn copiedTurn = (Turn) copy.getEntries().get(0);

        assertNotSame(
                turn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.TurnStart
                ),
                copiedTurn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.TurnStart
                )
        );

        assertNotSame(
                turn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.Actions
                ),
                copiedTurn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.Actions
                )
        );

        assertNotSame(
                turn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.Recruitment
                ),
                copiedTurn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.Recruitment
                )
        );

        assertNotSame(
                turn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.TurnEnd
                ),
                copiedTurn.getSubPhase(
                        com.leaders.gamelogic.enums.GamePhaseType.TurnEnd
                )
        );
    }

    @Test
    public void copy_shouldPreserveEntryOrder() {
        Turn turn = createTestTurn();
        BanishmentPhase banishmentPhase = createTestBanishmentPhase();

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(turn);
        entries.add(banishmentPhase);

        GameHistory original = new GameHistory(
                createTestGameConfig(),
                entries
        );

        GameHistory copy = new GameHistory(original);

        assertEquals(2, copy.getEntries().size());

        assertEquals(
                turn.getTransitionTarget(),
                ((Segment) copy.getEntries().get(0)).getTransitionTarget()
        );

        assertEquals(
                banishmentPhase.getTransitionTarget(),
                ((Segment) copy.getEntries().get(1)).getTransitionTarget()
        );
    }
}