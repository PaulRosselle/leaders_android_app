package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class BanishmentQueryTest {

    private Game createTestGame() {
        return new Game(
                new Board(),
                new ArrayList<>(), // recruitableCards
                new ArrayList<>(), // recruitedCharacters
                new ArrayList<>(), // banishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private void addRecruitedCharacter(
            Game game,
            CharacterType characterType,
            TeamColor teamColor
    ) {
        game.getRecruitedCharacters().add(
                Character.create(characterType, teamColor)
        );
    }

    @Test
    public void canBanish_shouldReturnTrueForTwoTeamsWithNoRecruitmentAndNoBanishment() {
        Game game = createTestGame();

        assertTrue(BanishmentQuery.canBanish(game));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenTeamsHaveDifferentRecruitmentCounts() {
        Game game = createTestGame();

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );

        assertFalse(BanishmentQuery.canBanish(game));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenBanishmentCountIsInvalid() {
        Game game = createTestGame();

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Bruiser,
                TeamColor.White
        );

        game.getBanishedCards().add(CharacterCard.Archer);

        assertFalse(BanishmentQuery.canBanish(game));
    }

    @Test
    public void canBanish_shouldReturnTrueWithEqualRecruitmentCountsAndTwoBanishments() {
        Game game = createTestGame();

        addRecruitedCharacter(
                game,
                CharacterType.Archer,
                TeamColor.Black
        );
        addRecruitedCharacter(
                game,
                CharacterType.Bruiser,
                TeamColor.White
        );

        game.getBanishedCards().add(CharacterCard.Archer);
        game.getBanishedCards().add(CharacterCard.Bruiser);

        assertTrue(BanishmentQuery.canBanish(game));
    }

    @Test
    public void canBanish_shouldReturnFalseWhenRecruitmentCountReachesBanishmentLimit() {
        Game game = createTestGame();

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

        assertFalse(BanishmentQuery.canBanish(game));
    }

    @Test
    public void getBanishableCards_shouldReturnRecruitableCards() {
        Game game = createTestGame();

        game.getRecruitableCards().add(CharacterCard.Archer);
        game.getRecruitableCards().add(CharacterCard.Bruiser);

        List<CharacterCard> banishableCards =
                BanishmentQuery.getBanishableCards(game);

        assertSame(
                game.getRecruitableCards(),
                banishableCards
        );
    }
}