package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GameConfigTest {

    private Player createTestPlayer() {
        return new Player(TeamColor.Black, "Test Player");
    }

    private GameConfig createTestGameConfig(
            List<Player> players,
            List<CharacterCard> initialRecruitableCards,
            List<IGameAction> initialPlacements) {

        return new GameConfig(
                players,
                players.get(0),
                GameMode.Discovery,
                initialRecruitableCards,
                initialPlacements
        );
    }

    @Test
    public void constructor_shouldStoreValues() {
        Player player = createTestPlayer();
        List<Player> players = List.of(player);
        List<CharacterCard> recruitableCards =
                List.of(CharacterCard.Acrobat);
        List<IGameAction> initialPlacements = List.of();

        GameConfig config = createTestGameConfig(
                players,
                recruitableCards,
                initialPlacements
        );

        assertSame(player, config.getPlayers().get(0));
        assertSame(player, config.getFirstPlayer());
        assertEquals(GameMode.Discovery, config.getGameMode());
        assertEquals(
                recruitableCards,
                config.getInitialRecruitableCards()
        );
        assertEquals(
                initialPlacements,
                config.getInitialPlacements()
        );
    }

    @Test
    public void constructor_shouldDefensivelyCopyPlayers() {
        Player player = createTestPlayer();
        List<Player> players = new ArrayList<>();
        players.add(player);

        GameConfig config = createTestGameConfig(
                players,
                List.of(),
                List.of()
        );

        players.clear();

        assertEquals(1, config.getPlayers().size());
        assertSame(player, config.getPlayers().get(0));
    }

    @Test
    public void getPlayers_shouldReturnUnmodifiableList() {
        GameConfig config = createTestGameConfig(
                List.of(createTestPlayer()),
                List.of(),
                List.of()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> config.getPlayers().add(
                        new Player(TeamColor.White, "Another Player")
                )
        );
    }

    @Test
    public void constructor_shouldDefensivelyCopyInitialRecruitableCards() {
        List<CharacterCard> cards = new ArrayList<>();
        cards.add(CharacterCard.Acrobat);

        GameConfig config = createTestGameConfig(
                List.of(createTestPlayer()),
                cards,
                List.of()
        );

        cards.clear();

        assertEquals(
                List.of(CharacterCard.Acrobat),
                config.getInitialRecruitableCards()
        );
    }

    @Test
    public void getInitialRecruitableCards_shouldReturnUnmodifiableList() {
        GameConfig config = createTestGameConfig(
                List.of(createTestPlayer()),
                List.of(CharacterCard.Acrobat),
                List.of()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> config.getInitialRecruitableCards()
                        .add(CharacterCard.Archer)
        );
    }

    @Test
    public void constructor_shouldDefensivelyCopyInitialPlacements() {
        List<IGameAction> placements = new ArrayList<>();

        GameConfig config = createTestGameConfig(
                List.of(createTestPlayer()),
                List.of(),
                placements
        );

        placements.clear();

        assertEquals(
                0,
                config.getInitialPlacements().size()
        );
    }

    @Test
    public void getInitialPlacements_shouldReturnUnmodifiableList() {
        GameConfig config = createTestGameConfig(
                List.of(createTestPlayer()),
                List.of(),
                List.of()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> config.getInitialPlacements().add(null)
        );
    }
}