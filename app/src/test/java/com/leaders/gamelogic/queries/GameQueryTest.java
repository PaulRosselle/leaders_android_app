package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;

public class GameQueryTest {

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

    private Character placeCharacter(@NonNull Board board, @NonNull Position position,
                                     @NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
        Character character = Character.create(characterType, teamColor);
        board.getCell(position).setCharacter(character);
        return character;
    }

    private void placeCharacter(@NonNull Game game, @NonNull Position position,
                                @NonNull CharacterType characterType, @NonNull TeamColor teamColor) {
        Character character = placeCharacter(game.getBoard(), position, characterType, teamColor);
        game.getRecruitedCharacters().add(character);
    }

    private void placeLeader(Board board, Position position, TeamColor teamColor) {
        placeCharacter(board, position,
                teamColor == TeamColor.Black ? CharacterType.LeaderKing : CharacterType.LeaderQueen,
                teamColor);
    }

    @Test
    public void isLeaderCaptured_shouldReturnFalseWhenCaptureValueIsNotReached() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 1), CharacterType.Archer, TeamColor.White);

        assertFalse(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test
    public void isLeaderCaptured_shouldReturnTrueWhenCaptureValueIsReached() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 2), CharacterType.Acrobat, TeamColor.Black);
        placeCharacter(game, new Position(2, 2), CharacterType.Bruiser, TeamColor.Black);

        assertTrue(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test
    public void isLeaderCaptured_shouldReturnTrueWhenMultipleCharactersReachCaptureValue() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 1), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(1, 3), CharacterType.Archer, TeamColor.Black);

        assertTrue(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test(expected = NullPointerException.class)
    public void isLeaderCaptured_shouldThrowWhenLeaderIsMissing() {
        Game game = createTestGame(new Board());

        GameQuery.isLeaderCaptured(game, TeamColor.White);
    }

    @Test
    public void isLeaderCaptured_shouldReturnTrueWhenAssassinIsAdjacent() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 2), CharacterType.Assassin, TeamColor.Black);

        assertTrue(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test
    public void isLeaderCaptured_shouldReturnTrueWhenArcherIsAtDistanceTwo() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 2), CharacterType.Acrobat, TeamColor.Black);
        placeCharacter(game, new Position(2, 2), CharacterType.Archer, TeamColor.Black);

        assertFalse(GameQuery.isLeaderCaptured(game, TeamColor.White));

        placeCharacter(game, new Position(3, 1), CharacterType.Archer, TeamColor.Black);

        assertTrue(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test
    public void isLeaderCaptured_shouldReturnFalseWhenCubSupportsCapture() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.White);

        Game game = createTestGame(board);

        placeCharacter(game, new Position(2, 2), CharacterType.Cub, TeamColor.Black);
        placeCharacter(game, new Position(3, 2), CharacterType.Acrobat, TeamColor.Black);

        assertFalse(GameQuery.isLeaderCaptured(game, TeamColor.White));


        placeCharacter(game, new Position(4, 2), CharacterType.Bruiser, TeamColor.Black);

        assertTrue(GameQuery.isLeaderCaptured(game, TeamColor.White));
    }

    @Test
    public void isLeaderSurrounded_shouldReturnFalseWhenAdjacentCellIsEmpty() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.White);

        Game game = createTestGame(board);

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = leaderPosition.adjacent(direction);
            if (adjacentPosition != null) {
                placeCharacter(board, adjacentPosition, CharacterType.Archer, TeamColor.Black);
            }
        }

        Direction emptyDirection = Direction.values()[0];
        Position emptyPosition = leaderPosition.adjacent(emptyDirection);
        board.getCell(Objects.requireNonNull(emptyPosition)).setCharacter(null);

        assertFalse(GameQuery.isLeaderSurrounded(game, TeamColor.White));
    }

    @Test
    public void isLeaderSurrounded_shouldReturnTrueWhenAllAdjacentCellsAreOccupied() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.White);

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = leaderPosition.adjacent(direction);
            if (adjacentPosition != null) {
                placeCharacter(board, adjacentPosition, CharacterType.Archer, TeamColor.Black);
            }
        }

        Game game = createTestGame(board);

        assertTrue(GameQuery.isLeaderSurrounded(game, TeamColor.White));
    }

    @Test(expected = NullPointerException.class)
    public void isLeaderSurrounded_shouldThrowWhenLeaderIsMissing() {
        Game game = createTestGame(new Board());

        GameQuery.isLeaderSurrounded(game, TeamColor.White);
    }

    @Test
    public void isBarrageDetected_shouldReturnFalseWhenTeamHasLessThanFourCharacters() {
        Board board = new Board();
        Game game = createTestGame(board);

        placeCharacter(game, new Position(3, 3), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(3, 4), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(3, 5), CharacterType.Archer, TeamColor.Black);

        assertFalse(GameQuery.isBarrageDetected(game, TeamColor.Black));
    }

    @Test
    public void isBarrageDetected_shouldReturnFalseWhenCharactersDoNotFormBarrage() {
        Board board = new Board();
        Game game = createTestGame(board);

        placeCharacter(game, new Position(2, 2), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(4, 2), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(2, 4), CharacterType.Archer, TeamColor.Black);
        placeCharacter(game, new Position(4, 4), CharacterType.Archer, TeamColor.Black);

        assertFalse(GameQuery.isBarrageDetected(game, TeamColor.Black));
    }

    @Test
    public void isBarrageDetected_shouldReturnTrueWhenChainSeparatesBoard() {
        Board board = new Board();
        Game game = createTestGame(board);

        for (int y = 0; y <= 6; y++) {
            placeCharacter(game, new Position(3, y), CharacterType.Archer, TeamColor.Black);
        }

        assertTrue(GameQuery.isBarrageDetected(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnNullWhenGameHasNoWinner() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.Black);
        placeLeader(board, new Position(3, 6), TeamColor.White);

        Game game = createTestGame(board);

        assertNull(GameQuery.getWinnerTeam(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnOpponentWhenCurrentTeamReachedBarrageWarningLimit() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.Black);
        placeLeader(board, new Position(3, 6), TeamColor.White);

        Game game = createTestGame(board);
        game.setPlayerWarningCount(TeamColor.Black, WarningType.Barrage,2);

        assertEquals(TeamColor.White, GameQuery.getWinnerTeam(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnOpponentWhenCurrentLeaderIsCaptured() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.Black);
        placeLeader(board, new Position(3, 6), TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 2), CharacterType.Assassin, TeamColor.White);

        assertEquals(TeamColor.White, GameQuery.getWinnerTeam(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnOpponentWhenCurrentLeaderIsSurrounded() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 3);
        placeLeader(board, leaderPosition, TeamColor.Black);
        placeLeader(board, new Position(3, 6), TeamColor.White);

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = leaderPosition.adjacent(direction);
            if (adjacentPosition != null) {
                placeCharacter(board, adjacentPosition, CharacterType.Archer, TeamColor.White);
            }
        }

        Game game = createTestGame(board);

        assertEquals(TeamColor.White, GameQuery.getWinnerTeam(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnCurrentTeamWhenOpponentLeaderIsCaptured() {
        Board board = new Board();
        placeLeader(board, new Position(3, 3), TeamColor.Black);
        placeLeader(board, new Position(3, 6), TeamColor.White);

        Game game = createTestGame(board);
        placeCharacter(game, new Position(3, 5), CharacterType.Assassin, TeamColor.Black);

        assertEquals(TeamColor.Black, GameQuery.getWinnerTeam(game, TeamColor.Black));
    }

    @Test
    public void getWinnerTeam_shouldReturnCurrentTeamWhenOpponentLeaderIsSurrounded() {
        Board board = new Board();
        Position leaderPosition = new Position(3, 6);
        placeLeader(board, new Position(3, 3), TeamColor.Black);
        placeLeader(board, leaderPosition, TeamColor.White);

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = leaderPosition.adjacent(direction);
            if (adjacentPosition != null) {
                placeCharacter(board, adjacentPosition, CharacterType.Archer, TeamColor.Black);
            }
        }

        Game game = createTestGame(board);

        assertEquals(TeamColor.Black, GameQuery.getWinnerTeam(game, TeamColor.Black));
    }
}