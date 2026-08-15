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
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterCardSelectionStatus;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.Turn;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
        return new Turn(TeamColor.Black);
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

        // White is the first player.
        // Black therefore receives the special first-recruitment limit of 2.
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
        for (Cell cell : BoardQuery.getRecruitmentCells(board, TeamColor.Black)) {
            cell.setCharacter(Character.create(CharacterType.Archer, TeamColor.Black));
        }

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        assertFalse(RecruitmentQuery.canRecruit(game, history, TeamColor.Black));
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

        Set<CharacterCard> cards = RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, true);

        assertTrue(cards.contains(CharacterCard.Archer));
        assertTrue(cards.contains(CharacterType.LeaderKing.getCharacterCard()));
    }

    @Test
    public void getRecruitedCards_shouldIgnoreCharactersFromOtherTeams() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Bruiser, TeamColor.White);

        Set<CharacterCard> cards =
                RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, false);

        assertEquals(Set.of(CharacterCard.Archer), cards);
    }

    @Test
    public void getRecruitedCards_shouldReturnDistinctCards() {
        Game game = createTestGame(new Board());

        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);
        addRecruitedCharacter(game, CharacterType.Archer, TeamColor.Black);

        Set<CharacterCard> cards = RecruitmentQuery.getRecruitedCards(game, TeamColor.Black, false);

        assertEquals(1, cards.size());
        assertTrue(cards.contains(CharacterCard.Archer));
    }

    @Test
    public void getValidRecruitmentCards_shouldReturnEmptyOutsideRecruitmentPhase() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();

        assertTrue(invokeGetValidRecruitmentCards(game, history).isEmpty());
    }

    @Test
    public void getValidRecruitmentCards_shouldReturnCardsDuringRecruitmentPhase() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        assertTrue(invokeGetValidRecruitmentCards(game, history).contains(CharacterCard.Archer));
    }

    @Test
    public void getValidRecruitmentCards_shouldReturnEmptyWhenNoRecruitmentCellIsAvailable() {
        Board board = new Board();

        for (Cell cell : BoardQuery.getRecruitmentCells(board, TeamColor.Black)) {
            cell.setCharacter(Character.create(CharacterType.Archer, TeamColor.Black));
        }

        Game game = createTestGame(board);
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        assertTrue(invokeGetValidRecruitmentCards(game, history).isEmpty());
    }

    @Test
    public void getSelectableRecruitmentCards_shouldMarkValidCardsAsRecruitable() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        List<SelectableCharacterCard> cards =
                RecruitmentQuery.getSelectableRecruitmentCards(game, history);

        assertEquals(1, cards.size());
        assertEquals(CharacterCard.Archer, cards.get(0).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.Recruitable, cards.get(0).getSelectionStatus());
    }

    @Test
    public void getSelectableRecruitmentCards_shouldMarkInvalidCardsAsRecruitmentImpossible() {
        Game game = createTestGame(new Board());

        game.getRecruitableCards().add(CharacterCard.Archer);
        game.getRecruitableCards().add(CharacterCard.Acrobat);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        // Fill all recruitment cells so no card can be recruited.
        for (Cell cell : BoardQuery.getRecruitmentCells(game.getBoard(), TeamColor.Black)) {
            cell.setCharacter(Character.create(CharacterType.Archer, TeamColor.Black));
        }

        List<SelectableCharacterCard> cards = RecruitmentQuery.getSelectableRecruitmentCards(game, history);

        assertEquals(2, cards.size());

        assertEquals(CharacterCardSelectionStatus.RecruitmentImpossible, cards.get(0).getSelectionStatus());
        assertEquals(CharacterCardSelectionStatus.RecruitmentImpossible, cards.get(1).getSelectionStatus());
    }

    @Test
    public void getSelectableRecruitmentCards_shouldIncludeAlreadyBannedCards() {
        Game game = createTestGame(new Board());

        game.getRecruitableCards().add(CharacterCard.Archer);

        game.addBanishedCard(TeamColor.Black, CharacterCard.Acrobat);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        List<SelectableCharacterCard> cards =
                RecruitmentQuery.getSelectableRecruitmentCards(
                        game,
                        history
                );

        assertEquals(2, cards.size());

        assertEquals(CharacterCard.Archer, cards.get(0).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.Recruitable, cards.get(0).getSelectionStatus());

        assertEquals(CharacterCard.Acrobat, cards.get(1).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.AlreadyBanned, cards.get(1).getSelectionStatus());
    }

    @Test
    public void getSelectableRecruitmentCards_shouldIncludeBannedCardsFromBothTeams() {
        Game game = createTestGame(new Board());

        game.getRecruitableCards().add(CharacterCard.Archer);

        game.addBanishedCard(TeamColor.Black, CharacterCard.Acrobat);
        game.addBanishedCard(TeamColor.White, CharacterCard.Assassin);

        GameHistory history = createTestGameHistory();
        addRecruitmentPhase(history);

        List<SelectableCharacterCard> cards =
                RecruitmentQuery.getSelectableRecruitmentCards(
                        game,
                        history
                );

        assertEquals(3, cards.size());

        assertEquals(CharacterCard.Archer, cards.get(0).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.Recruitable, cards.get(0).getSelectionStatus());

        assertEquals(CharacterCard.Acrobat, cards.get(1).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.AlreadyBanned, cards.get(1).getSelectionStatus());

        assertEquals(CharacterCard.Assassin, cards.get(2).getCharacterCard());
        assertEquals(CharacterCardSelectionStatus.AlreadyBanned, cards.get(2).getSelectionStatus());
    }

    @Test
    public void getSelectableRecruitmentCards_shouldReturnEmptyOutsideRecruitmentPhaseForRecruitableCards() {
        Game game = createTestGame(new Board());
        game.getRecruitableCards().add(CharacterCard.Archer);

        GameHistory history = createTestGameHistory();

        List<SelectableCharacterCard> cards =
                RecruitmentQuery.getSelectableRecruitmentCards(
                        game,
                        history
                );

        assertEquals(1, cards.size());
        assertEquals(
                CharacterCard.Archer,
                cards.get(0).getCharacterCard()
        );
        assertEquals(
                CharacterCardSelectionStatus.RecruitmentImpossible,
                cards.get(0).getSelectionStatus()
        );
    }

    @SuppressWarnings("unchecked")
    private List<CharacterCard> invokeGetValidRecruitmentCards(@NonNull Game game, @NonNull GameHistory gameHistory) {
        try {
            Method method = RecruitmentQuery.class.getDeclaredMethod("getValidRecruitmentCards", Game.class, GameHistory.class);

            method.setAccessible(true);

            return (List<CharacterCard>) method.invoke(null, game, gameHistory);
        } catch (Exception e) {
            throw new AssertionError("Unable to invoke getValidRecruitableCards", e);
        }
    }
}