package com.leaders.gamelogic.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.junit.Test;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.gamelogic.historyentries.segments.RecruitmentPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnEndPhase;
import com.leaders.gamelogic.historyentries.segments.TurnStartPhase;

import java.util.ArrayList;
import java.util.List;

public class GameFactoryTest {
    @NonNull
    public GameHistory createTestInitialGameHistory() {
        // Build the minimal GameHistory state required by the tests.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));


        List<IGameAction> initialPlacements = new ArrayList<>();
        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        initialPlacements.add(new CharacterAction(leaderBlack,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Add,
                        List.of(new CharacterActionTarget(
                                leaderBlack,
                                null,
                                new Position(3, 0))
                        ))
                ))
        );
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);
        initialPlacements.add(new CharacterAction(leaderWhite,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Add,
                        List.of(new CharacterActionTarget(
                                leaderWhite,
                                null,
                                new Position(3, 6))
                        ))
                ))
        );

        GameConfig gameConfig = new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                initialPlacements
        );

        return new GameHistory(gameConfig, new ArrayList<>());
    }

    @NonNull
    public GameHistory createTestGameHistoryWithTurn() {
        // Build the minimal GameHistory state required by the tests.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        TeamColor teamColor = TeamColor.Black;

        TurnStartPhase turnStartPhase = new TurnStartPhase(null, null, teamColor);
        turnStartPhase.start();
        turnStartPhase.end();

        ActionsPhase actionsPhase = new ActionsPhase(null, null, teamColor);
        actionsPhase.start();
        Character movedCharacter = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        actionsPhase.getActions().add(new CharacterAction(movedCharacter,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(
                                movedCharacter,
                                new Position(3, 0),
                                new Position(3, 1))
                        ))
                ))
        );
        actionsPhase.end();

        RecruitmentPhase recruitmentPhase = new RecruitmentPhase(null, null, teamColor);
        recruitmentPhase.start();
        Character recruitedCharacter = Character.create(CharacterType.Acrobat, TeamColor.Black);
        recruitmentPhase.getActions().add(new RecruitmentAction(
                List.of(new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        recruitedCharacter,
                        new Position(0, 0)
                ))
        ));

        Turn turn = new Turn(null, null, teamColor,
                turnStartPhase,
                actionsPhase,
                recruitmentPhase,
                new TurnEndPhase(null, null, teamColor)
        );

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(turn);

        List<CharacterCard> recruitableCards = List.of(
                CharacterCard.Acrobat,
                CharacterCard.Wanderer
        );

        GameConfig gameConfig = new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                recruitableCards, // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        );

        return new GameHistory(gameConfig, entries);
    }

    @NonNull
    public GameHistory createTestGameHistoryWithBanishmentPhase() {
        // Build the minimal GameHistory state required by the tests.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));

        List<CharacterCard> recruitableCards = List.of(
                CharacterCard.HermitAndCub,
                CharacterCard.Protector
        );

        GameConfig gameConfig = new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Strategist,
                recruitableCards, // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        );

        TeamColor teamColor = TeamColor.White;
        BanishmentPhase banishmentPhase = new BanishmentPhase(null, null, teamColor);
        banishmentPhase.start();
        banishmentPhase.getActions().add(new BanishmentAction(CharacterCard.HermitAndCub, teamColor));

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(banishmentPhase);

        return new GameHistory(gameConfig, entries);
    }

    private boolean areSameCharacter(@NonNull Character c1, @NonNull Character c2) {
        return c1.getCharacterType() == c2.getCharacterType() &&
                c1.getTeamColor() == c2.getTeamColor();
    }

    @Test
    public void create_shouldBuildInitialGameFromConfig() {
        GameHistory gameHistory = createTestInitialGameHistory();
        Game game = GameFactory.create(gameHistory);

        Character expectedChar1 = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character expectedChar2 = Character.create(CharacterType.LeaderQueen, TeamColor.White);
        Position expectedPos1 = new Position(3, 0);
        Position expectedPos2 = new Position(3, 6);

        for (Cell cell : game.getBoard().getCells().values()) {
            if (cell.getPosition().equals(expectedPos1)) {
                assertTrue(areSameCharacter(expectedChar1, cell.getCharacter()));
            } else if (cell.getPosition().equals(expectedPos2)) {
                assertTrue(areSameCharacter(expectedChar2, cell.getCharacter()));
            } else {
                assertNull(cell.getCharacter());
            }
        }
        assertTrue(game.getRecruitableCards().isEmpty());
        assertTrue(game.getRecruitedCharacters().isEmpty());
        for (TeamColor teamColor : TeamColor.values()) {
            assertEquals(0, game.getBanishedCards(teamColor).size());
        }
        for (WarningType warningType : WarningType.values()) {
            for (TeamColor teamColor : TeamColor.values()) {
                assertEquals(0, game.getPlayerWarningCount(teamColor, warningType));
            }
        }
    }

    @Test
    public void create_shouldReplayTurnActions() {
        GameHistory gameHistory = createTestGameHistoryWithTurn();
        Game game = GameFactory.create(gameHistory);

        Character expectedChar1 = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character expectedChar2 = Character.create(CharacterType.Acrobat, TeamColor.Black);
        Position expectedPos1 = new Position(3, 1);
        Position expectedPos2 = new Position(0, 0);

        for (Cell cell : game.getBoard().getCells().values()) {
            if (cell.getPosition().equals(expectedPos1)) {
                assertTrue(areSameCharacter(expectedChar1, cell.getCharacter()));
            } else if (cell.getPosition().equals(expectedPos2)) {
                assertTrue(areSameCharacter(expectedChar2, cell.getCharacter()));
            } else {
                assertNull(cell.getCharacter());
            }
        }
        assertEquals(1, game.getRecruitableCards().size());
        assertEquals(CharacterCard.Wanderer, game.getRecruitableCards().get(0));
        assertEquals(1, game.getRecruitedCharacters().size());
        assertTrue(areSameCharacter(expectedChar2, game.getRecruitedCharacters().get(0)));
        for (TeamColor teamColor : TeamColor.values()) {
            assertEquals(0, game.getBanishedCards(teamColor).size());
        }
        for (WarningType warningType : WarningType.values()) {
            for (TeamColor teamColor : TeamColor.values()) {
                assertEquals(0, game.getPlayerWarningCount(teamColor, warningType));
            }
        }
    }

    @Test
    public void create_shouldReplayBanishmentPhaseActions() {
        GameHistory gameHistory = createTestGameHistoryWithBanishmentPhase();
        Game game = GameFactory.create(gameHistory);

        for (Cell cell : game.getBoard().getCells().values()) {
            assertNull(cell.getCharacter());
        }
        assertEquals(1, game.getRecruitableCards().size());
        assertEquals(CharacterCard.Protector, game.getRecruitableCards().get(0));
        assertTrue(game.getRecruitedCharacters().isEmpty());

        for (TeamColor teamColor : TeamColor.values()) {
        }
        assertEquals(0, game.getBanishedCards(TeamColor.Black).size());
        List<CharacterCard> whiteBans = game.getBanishedCards(TeamColor.White);
        assertEquals(1, whiteBans.size());
        assertEquals(CharacterCard.HermitAndCub, whiteBans.get(0));
        for (WarningType warningType : WarningType.values()) {
            for (TeamColor teamColor : TeamColor.values()) {
                assertEquals(0, game.getPlayerWarningCount(teamColor, warningType));
            }
        }
    }
}