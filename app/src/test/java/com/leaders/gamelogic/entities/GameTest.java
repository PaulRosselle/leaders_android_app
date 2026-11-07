package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class GameTest {

    private Game createTestGame() {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    @Test
    public void copy_shouldCreateIndependentBoard() {
        Game original = createTestGame();

        Game copy = new Game(original);

        assertNotSame(original.getBoard(), copy.getBoard());
    }

    @Test
    public void copy_shouldNotShareRecruitableCards() {
        Game original = createTestGame();
        Game copy = new Game(original);

        List<CharacterCard> before = new ArrayList<>(copy.getRecruitableCards());

        original.getRecruitableCards().add(CharacterCard.values()[0]);

        assertEquals(before, copy.getRecruitableCards());
    }

    @Test
    public void copy_shouldNotShareRecruitedCards() {
        Game original = createTestGame();
        Game copy = new Game(original);

        List<CharacterCard> before = original.getRecruitedCards(TeamColor.Black);

        original.addRecruitedCharacter(TeamColor.Black, CharacterType.Nemesis);

        assertEquals(before, copy.getRecruitedCards(TeamColor.Black));
    }

    @Test
    public void copy_shouldNotShareBanishedCards() {
        Game original = createTestGame();
        Game copy = new Game(original);

        TeamColor team = TeamColor.values()[0];
        List<CharacterCard> before = new ArrayList<>(copy.getBanishedCards(team));

        original.addBanishedCard(team, CharacterCard.values()[0]);

        assertEquals(before, copy.getBanishedCards(team));
    }

    @Test
    public void copy_shouldNotShareWarnings() {
        Game original = createTestGame();
        Game copy = new Game(original);

        TeamColor team = TeamColor.values()[0];
        WarningType type = WarningType.values()[0];
        int before = copy.getPlayerWarningCount(team, type);

        original.setPlayerWarningCount(team, type, before + 1);

        assertEquals(before, copy.getPlayerWarningCount(team, type));
    }
}
