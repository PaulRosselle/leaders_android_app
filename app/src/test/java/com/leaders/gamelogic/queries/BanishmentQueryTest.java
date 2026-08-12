package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;

public class BanishmentQueryTest {

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private GameHistory createTestGameHistory(GameMode gameMode) {
        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        return new GameHistory(new GameConfig(
                players,
                players.get(1), // firstPlayer
                gameMode,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        ), new ArrayList<>());
    }

    private void addRecruitedCharacter(
            Game game,
            CharacterType characterType,
            TeamColor teamColor) {
        game.getRecruitedCharacters().add(
                Character.create(characterType, teamColor)
        );
    }

    @Test
    public void canBanish_shouldReturnFalseOutsideStrategistMode() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Discovery);

        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.Black));
    }

    @Test
    public void canBanish_shouldReturnTrueForTwoTeamsWithNoRecruitmentAndNoBanishment() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        assertTrue(BanishmentQuery.canBanish(game, history, TeamColor.Black));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenTeamsHaveDifferentRecruitmentCounts() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );

        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.Black));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenBanishmentCountIsInvalid() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        game.addBanishedCard(TeamColor.Black, CharacterCard.Archer);

        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.Black));
    }

    @Test
    public void canBanish_shouldReturnTrueWithEqualRecruitmentCountsAndOneBanishment() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Acrobat,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Bruiser,
                TeamColor.White
        );
        addRecruitedCharacter(
                game,
                CharacterType.Assassin,
                TeamColor.White
        );

        game.addBanishedCard(TeamColor.Black, CharacterCard.Archer);

        assertTrue(BanishmentQuery.canBanish(game, history, TeamColor.Black));
    }

    @Test
    public void canBanish_shouldUseBanishmentCountOfTheSpecifiedTeam() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        game.addBanishedCard(TeamColor.White, CharacterCard.Archer);

        assertTrue(BanishmentQuery.canBanish(game, history, TeamColor.Black));
        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.White));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenRecruitmentCountReachesBanishmentLimit() {
        Game game = createTestGame();
        GameHistory history = createTestGameHistory(GameMode.Strategist);

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Acrobat,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.ClawLauncher,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Bruiser,
                TeamColor.White
        );
        addRecruitedCharacter(
                game,
                CharacterType.Assassin,
                TeamColor.White
        );
        addRecruitedCharacter(
                game,
                CharacterType.Illusionist,
                TeamColor.White
        );

        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.Black));
        assertFalse(BanishmentQuery.canBanish(game, history, TeamColor.White));
    }

    @Test
    public void getBanishableCards_shouldReturnRecruitableCards() {
        Game game = createTestGame();

        game.getRecruitableCards().add(CharacterCard.Archer);
        game.getRecruitableCards().add(CharacterCard.Bruiser);

        assertSame(
                game.getRecruitableCards(),
                BanishmentQuery.getBanishableCards(game)
        );
    }
}