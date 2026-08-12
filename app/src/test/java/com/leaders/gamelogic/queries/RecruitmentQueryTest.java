package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Set;

public class RecruitmentQueryTest {

    private GameHistory createTestGameHistory() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        return new GameHistory(
                new GameConfig(
                        players,
                        players.get(1), // firstPlayer
                        GameMode.Discovery,
                        new ArrayList<>(), // initialRecruitableCards
                        new ArrayList<>()  // initialPlacements
                ),
                new ArrayList<>()
        );
    }

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

    private Turn createTestTurn() {
        return new Turn(
                null,
                null,
                TeamColor.Black,
                new TurnStartPhase(null, null, TeamColor.Black),
                new ActionsPhase(null, null, TeamColor.Black),
                new RecruitmentPhase(null, null, TeamColor.Black),
                new TurnEndPhase(null, null, TeamColor.Black)
        );
    }

    private void addRecruitmentPhase(@NonNull GameHistory gameHistory) {
        Turn turn = createTestTurn();
        gameHistory.getEntries().add(turn);

        turn.getSubPhasesInOrder()[2].start();
    }

    private void addRecruitedCharacter(@NonNull Game game,
                                       @NonNull CharacterType characterType,
                                       @NonNull TeamColor teamColor) {
        game.getRecruitedCharacters().add(Character.create(characterType, teamColor));
    }

    @Test
    public void canRecruit_shouldReturnTrueWhenTeamCanRecruitAndCellIsAvailable() {
        Game game = createTestGame(new Board());
        GameHistory history = createTestGameHistory();

        assertTrue(RecruitmentQuery.canRecruit(game, history, TeamColor.Black));
    }

    @Test
    public void canRecruit_shouldReturnTrueForSecondPlayerWhenTheyHaveNoRecruitments() {
        Game game = createTestGame(new Board());
        GameHistory history = createTestGameHistory();

        // White is the second player because White is the first player.
        // Black therefore receives the special first-recruitment limit.
        assertTrue(RecruitmentQuery.canRecruit(game, history, TeamColor.Black));
    }

    @Test
    public void canRecruit_shouldReturnFalseWhenTeamIsFull() {
        Game game = createTestGame(new Board());
        GameHistory history = createTestGameHistory();

        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Acrobat, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Assassin, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Bruiser, TeamColor.Black);

        assertFalse(RecruitmentQuery.canRecruit(game, history, TeamColor.Black));
    }

    @Test
    public void canRecruit_shouldReturnFalseWhenNoRecruitmentCellIsAvailable() {
        Board board = new Board();

        // Fill every recruitment cell for Black.
        for (Cell cell :
                BoardQuery.getRecruitmentCells(board, TeamColor.Black)) {
            cell.setCharacter(
                    Character.create(
                            CharacterType.Archer,
                            TeamColor.Black
                    )
            );
        }

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        assertFalse(
                RecruitmentQuery.canRecruit(game, history, TeamColor.Black)
        );
    }

    @Test
    public void getRecruitedCards_shouldExcludeLeadersByDefault() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.LeaderKing, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);

        Set<CharacterCard> cards =
                RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, false);

        assertEquals(
                Set.of(CharacterCard.Archer),
                cards
        );
    }

    @Test
    public void getRecruitedCards_shouldIncludeLeadersWhenRequested() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.LeaderKing, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);

        Set<CharacterCard> cards =
                RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, true);

        assertTrue(cards.contains(CharacterCard.Archer));
        assertTrue(
                cards.contains(
                        CharacterType.LeaderKing.getCharacterCard()
                )
        );
    }

    @Test
    public void getRecruitedCards_shouldIgnoreCharactersFromOtherTeams() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Bruiser, TeamColor.White);

        Set<CharacterCard> cards =
                RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, false);

        assertEquals(
                Set.of(CharacterCard.Archer),
                cards
        );
    }

    @Test
    public void getRecruitedCards_shouldReturnDistinctCards() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);

        Set<CharacterCard> cards =
                RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, false);

        assertEquals(1, cards.size());
        assertTrue(cards.contains(CharacterCard.Archer));
    }

    @Test
    public void getRecruitableCards_shouldReturnEmptyOutsideRecruitmentPhase() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();

        assertTrue(
                RecruitmentQuery.getRecruitableCards(game, history).isEmpty()
        );
    }

    @Test
    public void getRecruitableCards_shouldReturnCardsDuringRecruitmentPhase() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        assertTrue(
                RecruitmentQuery.getRecruitableCards(game, history)
                        .contains(CharacterCard.Archer)
        );
    }

    @Test
    public void getRecruitableCards_shouldReturnEmptyWhenNoRecruitmentCellIsAvailable() {
        Board board = new Board();

        for (Cell cell :
                BoardQuery.getRecruitmentCells(board, TeamColor.Black)) {
            cell.setCharacter(
                    Character.create(
                            CharacterType.Archer,
                            TeamColor.Black
                    )
            );
        }

        Game game = createTestGame(board);
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        assertTrue(
                RecruitmentQuery.getRecruitableCards(game, history).isEmpty()
        );
    }
}