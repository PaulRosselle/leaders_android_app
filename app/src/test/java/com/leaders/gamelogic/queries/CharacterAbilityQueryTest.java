package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
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
                new ArrayList<>(), // recruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class) // playerWarnings
        );
    }

    private ActionsPhase createActionsPhase() {
        return new ActionsPhase(null, null, TeamColor.Black);
    }

    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForAdjacentCharacter() {
        Board board = new Board();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);

        Cell characterCell = board.getCell(new Position(3, 2));
        characterCell.setCharacter(character);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Game game = createTestGame(board);
        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell));
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForArcherAtDistanceTwo() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        board.getCell(new Position(3, 1)).setCharacter(character);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Game game = createTestGame(board);
        assertEquals(1, CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell));
    }

    @Test
    public void getCaptureContribution_shouldReturnTwoForAdjacentAssassin() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Assassin, TeamColor.Black);

        board.getCell(new Position(3, 2)).setCharacter(character);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Game game = createTestGame(board);
        assertEquals(CharacterAbilityQuery.LEADER_CAPTURE_VALUE, CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell));
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForCub() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Cub, TeamColor.Black);
        board.getCell(new Position(3, 2)).setCharacter(character);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Game game = createTestGame(board);
        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell));
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForNonAdjacentCharacter() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        board.getCell(new Position(3, 1)).setCharacter(character);
        Cell leaderCell = board.getCell(new Position(3, 3));

        Game game = createTestGame(board);

        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(game, character, leaderCell));
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
    public void getNormalMovementDestCells_shouldReturnAdjacentEmptyCells() {
        Board board = new Board();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        Position position = new Position(3, 3);
        board.getCell(position).setCharacter(character);

        Game game = createTestGame(board);
        List<Cell> destinations = CharacterAbilityQuery.getNormalMovementDestCells(game, character);
        assertEquals(Direction.values().length, destinations.size());
    }

    @Test
    public void getNormalMovementDestCells_shouldExtendLeaderMovementWithVizier() {
        Board board = new Board();

        Character leader = createCharacter(CharacterType.LeaderKing, TeamColor.Black);
        Character vizier = createCharacter(CharacterType.Vizier, TeamColor.Black);

        board.getCell(new Position(3, 3)).setCharacter(leader);
        board.getCell(new Position(0, 0)).setCharacter(vizier);

        Game game = createTestGame(board);
        game.getRecruitedCharacters().add(vizier);

        List<Cell> destinations = CharacterAbilityQuery.getNormalMovementDestCells(game, leader);
        assertEquals(Direction.values().length * 3, destinations.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNormalMovementDestCells_shouldThrowForNemesis() {
        Board board = new Board();
        Character nemesis = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(nemesis);
        Game game = createTestGame(board);
        CharacterAbilityQuery.getNormalMovementDestCells(game, nemesis);
    }
}