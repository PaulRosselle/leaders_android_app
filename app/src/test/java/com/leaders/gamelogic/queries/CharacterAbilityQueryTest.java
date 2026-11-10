package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CharacterAbilityQueryTest {

    private Game createTestGame(Board board) {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(board,
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private ActionsPhase createActionsPhase() {
        return new ActionsPhase(TeamColor.Black);
    }

    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForAdjacentCharacter() {
        Board board = new Board();

        // Character who have specific capture rules are not tested here
        List<CharacterType> excludedCharacters = List.of(
                CharacterType.Archer,
                CharacterType.Assassin
        );

        for (CharacterType characterType : CharacterType.values()) {
            // Characters unable to capture are also excluded
            if (!characterType.canCapture() || excludedCharacters.contains(characterType)) {
                continue;
            }

            Character character = createCharacter(characterType, TeamColor.Black);
            Cell leaderCell = board.getCell(new Position(3, 3));

            List<Position> adjacentPositions = List.of(
                    new Position(2, 2),
                    new Position(2, 3),
                    new Position(3, 2),
                    new Position(3, 4),
                    new Position(4, 2),
                    new Position(4, 3)
            );

            Position characterPosition = null;
            for (Cell cell : board.getCells().values()) {
                if (cell == leaderCell) {
                    continue;
                }

                if (characterPosition != null) {
                    board.getCell(characterPosition).setCharacter(null);
                }
                characterPosition = cell.getPosition();
                Cell characterCell = board.getCell(characterPosition);
                characterCell.setCharacter(character);

                assertEquals(
                        adjacentPositions.contains(characterPosition) ? 1 : 0,
                        CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell)
                );
            }

            if (characterPosition != null) {
                board.getCell(characterPosition).setCharacter(null);
            }
        }
    }

    @Test
    public void getCaptureCntoribution_shouldReturnOneForArcherAtDistanceTwo() {
        Board board = new Board();

        List<Position> archerCapturePositions = List.of(
                new Position(1, 1),
                new Position(1, 3),
                new Position(3, 1),
                new Position(3, 5),
                new Position(5, 1),
                new Position(5, 3)
        );

        Character archer = createCharacter(CharacterType.Archer, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Position archerPosition = null;
        for (Cell cell : board.getCells().values()) {
            if (cell == leaderCell) {
                continue;
            }

            if (archerPosition != null) {
                board.getCell(archerPosition).setCharacter(null);
            }
            archerPosition = cell.getPosition();
            Cell archerCell = board.getCell(archerPosition);
            archerCell.setCharacter(archer);

            assertEquals(
                    archerCapturePositions.contains(archerPosition) ? 1 : 0,
                    CharacterAbilityQuery.getCaptureContribution(leaderCell, archerCell)
            );
        }
    }

    @Test
    public void getCaptureContribution_shouldReturnTwoForAdjacentAssassin() {
        Board board = new Board();

        Character assassin = createCharacter(CharacterType.Assassin, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));

        List<Position> adjacentPositions = List.of(
                new Position(2, 2),
                new Position(2, 3),
                new Position(3, 2),
                new Position(3, 4),
                new Position(4, 2),
                new Position(4, 3)
        );

        Position assassinPosition = null;
        for (Cell cell : board.getCells().values()) {
            if (cell == leaderCell) {
                continue;
            }

            if (assassinPosition != null) {
                board.getCell(assassinPosition).setCharacter(null);
            }
            assassinPosition = cell.getPosition();
            Cell assassinCell = board.getCell(assassinPosition);
            assassinCell.setCharacter(assassin);

            assertEquals(
                    adjacentPositions.contains(assassinPosition) ? 2 : 0,
                    CharacterAbilityQuery.getCaptureContribution(leaderCell, assassinCell)
            );
        }
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroWhenCanCaptureIsFalse() {
        Board board = new Board();

        for (CharacterType characterType : CharacterType.values()) {
            if (characterType.canCapture()) {
                continue;
            }

            Character character = createCharacter(characterType, TeamColor.Black);
            Cell leaderCell = board.getCell(new Position(3, 3));

            Position characterPosition = null;
            for (Cell cell : board.getCells().values()) {
                if (cell == leaderCell) {
                    continue;
                }

                if (characterPosition != null) {
                    board.getCell(characterPosition).setCharacter(null);
                }
                characterPosition = cell.getPosition();
                Cell characterCell = board.getCell(characterPosition);
                characterCell.setCharacter(character);

                assertEquals(0, CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell));
            }
        }
    }

    @Test
    public void getCaptureContribution_shouldThrowWhenCharacterCellIsEmpty() {
        Board board = new Board();

        Cell characterCell = board.getCell(new Position(3, 2));
        Cell leaderCell = board.getCell(new Position(3, 3));

        assertThrows(IllegalArgumentException.class, () ->
                CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell)
        );
    }

    @Test
    public void canAct_shouldReturnFalseForOpposingTeam() {
        ActionsPhase phase = createActionsPhase();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.White);
        assertFalse(CharacterAbilityQuery.canAct(phase, character));
    }

    @Test
    public void canAct_shouldReturnFalseForNemesis() {
        ActionsPhase phase = createActionsPhase();
        Character character = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.canAct(phase, character));
    }

    @Test
    public void canAct_shouldReturnTrueForCharacterThatHasNotActed() {
        ActionsPhase phase = createActionsPhase();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        assertTrue(CharacterAbilityQuery.canAct(phase, character));
    }

    @Test
    public void canAct_shouldReturnFalseForCharacterThatAlreadyActed() {
        ActionsPhase phase = createActionsPhase();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        CharacterAction action = new CharacterAction(character, new ArrayList<>());

        phase.getActions().add(action);
        assertFalse(CharacterAbilityQuery.canAct(phase, character));
    }

    @Test
    public void canAct_shouldAllowHermitWhenCubHasNotActed() {
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);

        assertTrue(CharacterAbilityQuery.canAct(phase, hermit));
    }

    @Test
    public void canAct_shouldAllowHermitImmediatelyAfterCub() {
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        CharacterAction cubAction = new CharacterAction(cub, new ArrayList<>());
        phase.getActions().add(cubAction);

        assertTrue(CharacterAbilityQuery.canAct(phase, hermit));
    }

    @Test
    public void canAct_shouldNotAllowHermitIfAnotherActionWasPlayedAfterCub() {
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.Black);

        phase.getActions().add(new CharacterAction(cub, new ArrayList<>()));

        phase.getActions().add(new CharacterAction(acrobat, new ArrayList<>()));

        assertFalse(CharacterAbilityQuery.canAct(phase, hermit));
    }

    @Test
    public void mustAct_shouldAlwaysReturnFalse() {
        assertFalse(CharacterAbilityQuery.mustAct());
    }

    @Test
    public void mustActNow_shouldReturnFalseForNonNemesis() {
        ActionsPhase phase = createActionsPhase();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.mustActNow(phase, character));
    }

    @Test
    public void mustActNow_shouldReturnFalseForNemesisWithoutActions() {
        ActionsPhase phase = createActionsPhase();
        Character nemesis = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.mustActNow(phase, nemesis));
    }

    @Test
    public void canUseActiveAbility_shouldReturnFalseForCharacterWithoutActiveAbility() {
        Game game = createTestGame(new Board());
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, character));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenAbilityIsNotBlocked() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(character);
        Game game = createTestGame(board);
        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, character));
    }

    @Test
    public void canUseActiveAbility_shouldReturnFalseWhenAdjacentEnemyJailerExists() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character jailer = createCharacter(CharacterType.Jailer, TeamColor.White);

        Position characterPosition = new Position(3, 3);
        board.getCell(characterPosition).setCharacter(character);
        board.getCell(new Position(3, 2)).setCharacter(jailer);

        Game game = createTestGame(board);
        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, character));
    }

    @Test
    public void canBeMovedByEnemyAbilities_shouldReturnFalseForProtector() {
        Game game = createTestGame(new Board());
        Character protector = createCharacter(CharacterType.Protector, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, protector));
    }

    @Test
    public void canBeMovedByEnemyAbilities_shouldReturnFalseForCharacterAdjacentToAlliedProtector() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        Character protector = createCharacter(CharacterType.Protector, TeamColor.Black);

        Position characterPosition = new Position(3, 3);
        board.getCell(characterPosition).setCharacter(character);
        board.getCell(new Position(3, 2)).setCharacter(protector);

        Game game = createTestGame(board);
        assertFalse(CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, character));
    }

    @Test
    public void canBeMovedByEnemyAbilities_shouldReturnTrueWithoutProtector() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);

        board.getCell(new Position(3, 3)).setCharacter(character);

        Game game = createTestGame(board);

        assertTrue(CharacterAbilityQuery.canBeMovedByEnemyAbilities(game, character));
    }

    @Test
    public void getNormalMovementPaths_shouldReturnAdjacentEmptyCells() {
        Board board = new Board();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        Position position = new Position(3, 3);
        board.getCell(position).setCharacter(character);

        Game game = createTestGame(board);
        List<CharacterPath> destinations = CharacterAbilityQuery.getNormalMovementPaths(game, character);
        assertEquals(Direction.values().length, destinations.size());
    }

    @Test
    public void getNormalMovementPaths_shouldExtendLeaderMovementWithVizier() {
        Board board = new Board();

        Character leader = createCharacter(CharacterType.LeaderKing, TeamColor.Black);
        Character vizier = createCharacter(CharacterType.Vizier, TeamColor.Black);

        board.getCell(new Position(3, 3)).setCharacter(leader);
        board.getCell(new Position(0, 0)).setCharacter(vizier);

        Game game = createTestGame(board);
        game.addRecruitedCharacter(TeamColor.Black, CharacterType.Vizier);

        List<CharacterPath> destinations = CharacterAbilityQuery.getNormalMovementPaths(game, leader);
        assertEquals(Direction.values().length * 3, destinations.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNormalMovementPaths_shouldThrowForNemesis() {
        Board board = new Board();
        Character nemesis = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(nemesis);
        Game game = createTestGame(board);
        CharacterAbilityQuery.getNormalMovementPaths(game, nemesis);
    }
}