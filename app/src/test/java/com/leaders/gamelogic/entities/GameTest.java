package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertNotSame;

import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;

public class GameTest {

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

    @Test
    public void copy_shouldCreateIndependentBoard() {
        Game original = createTestGame();

        Game copy = new Game(original);

        assertNotSame(original.getBoard(), copy.getBoard());
    }

    @Test
    public void copy_shouldCreateIndependentRecruitableCardsCollection() {
        Game original = createTestGame();

        Game copy = new Game(original);

        assertNotSame(
                original.getRecruitableCards(),
                copy.getRecruitableCards()
        );
    }

    @Test
    public void copy_shouldCreateIndependentRecruitedCharactersCollection() {
        Game original = createTestGame();

        Game copy = new Game(original);

        assertNotSame(
                original.getRecruitedCharacters(),
                copy.getRecruitedCharacters()
        );
    }
}
