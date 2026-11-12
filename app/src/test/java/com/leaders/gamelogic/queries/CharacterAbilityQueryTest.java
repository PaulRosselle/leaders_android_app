package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.CharacterPath;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.Direction;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.historyentries.segments.Turn;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CharacterAbilityQueryTest {

    private Game createTestGame(Board board) {
        // Build the minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(board,
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class), // playerWarnings
                new HashMap<>() // charactersTransformedState
        );
    }

    private Game createTestGameWithTransformedState(Board board, Character before, Character after) {
        Game game = createTestGame(board);

        game.addTransformedState(before, after);

        return game;
    }

    private GameHistory createTestGameHistory() {
        // Build the minimal GameHistory state required by the tests.
        // This state is intentionally invalid as a real game state.
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        GameHistory history = new GameHistory(new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                new ArrayList<>() // initialPlacements
        ), new ArrayList<>());

        Turn turn = new Turn(TeamColor.White);
        history.getEntries().add(turn);

        turn.getSubPhase(GamePhaseType.TurnStart).start();
        turn.getSubPhase(GamePhaseType.TurnStart).end();
        turn.getSubPhase(GamePhaseType.Actions).start();

        return history;
    }

    private void addCharacterMovementAction(@NonNull GameHistory history,
                                            @NonNull Character sourceCharacter,
                                            @NonNull Character targetCharacter,
                                            @NonNull Position source,
                                            @NonNull Position destination) {
        CharacterAction action = new CharacterAction(
                sourceCharacter,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(
                                targetCharacter,
                                source,
                                destination
                        ))
                ))
        );

        Objects.requireNonNull(GameHistoryQuery.findCurrentPhase(history)).getActions().add(action);
    }

    private Turn createTurn() {
        return new Turn(TeamColor.Black);
    }

    private ActionsPhase getActionsPhase(@NonNull Turn turn) {
        return (ActionsPhase) turn.getSubPhase(GamePhaseType.Actions);
    }

    private CharacterAction createTransformAction(@NonNull Character source,
                                                  @NonNull Character before,
                                                  @NonNull Character after,
                                                  @NonNull Position position) {
        return new CharacterAction(
                source,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Transform,
                        List.of(
                                new CharacterActionTarget(before, position, null),
                                new CharacterActionTarget(after, null, position)
                        )
                ))
        );
    }

    private CharacterAction createMoveAction(@NonNull Character source,
                                             @NonNull Character target,
                                             @NonNull Position origin,
                                             @NonNull Position destination) {
        return new CharacterAction(
                source,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(target, origin, destination))
                ))
        );
    }

    private TransformedState getTransformedState(@NonNull Game game, @NonNull Character before) {
        return Objects.requireNonNull(game.getTransformedState(before.getId()));
    }

    private ActionsPhase createActionsPhase() {
        return new ActionsPhase(TeamColor.Black);
    }

    private GamePhase getCurrentPhase(@NonNull GamePhaseType phaseType) {
        return new GamePhase(phaseType, new Player(TeamColor.Black, "Test"));
    }

    private Character createCharacter(CharacterType characterType, TeamColor teamColor) {
        return Character.create(characterType, teamColor);
    }

    private Character frogifyCharacter(Character character, TeamColor teamColor) {
        return Character.transform(character, CharacterType.Frog, teamColor);
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForAdjacentCharacter() {
        Board board = new Board();

        // Character who have specific capture rules are not tested here
        List<CharacterType> excludedCharacters = List.of(
                CharacterType.Archer,
                CharacterType.Assassin,
                CharacterType.EliteArcher
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
                        CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell, getCurrentPhase(GamePhaseType.Actions))
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
                    CharacterAbilityQuery.getCaptureContribution(leaderCell, archerCell, getCurrentPhase(GamePhaseType.Actions))
            );
        }
    }


    @Test
    public void getCaptureContribution_shouldReturnOneForAlignedEliteArcherAtDistanceOne() {
        Board board = new Board();

        Character eliteArcher = createCharacter(CharacterType.EliteArcher, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell eliteArcherCell = board.getCell(new Position(3, 2));

        eliteArcherCell.setCharacter(eliteArcher);

        assertEquals(
                1,
                CharacterAbilityQuery.getCaptureContribution(
                        leaderCell,
                        eliteArcherCell,
                        getCurrentPhase(GamePhaseType.Actions)
                )
        );
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForAlignedEliteArcherAtDistanceTwo() {
        Board board = new Board();

        Character eliteArcher = createCharacter(CharacterType.EliteArcher, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell eliteArcherCell = board.getCell(new Position(3, 1));

        eliteArcherCell.setCharacter(eliteArcher);

        assertEquals(
                1,
                CharacterAbilityQuery.getCaptureContribution(
                        leaderCell,
                        eliteArcherCell,
                        getCurrentPhase(GamePhaseType.Actions)
                )
        );
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForUnalignedEliteArcherAtDistanceTwo() {
        Board board = new Board();

        Character eliteArcher = createCharacter(CharacterType.EliteArcher, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell eliteArcherCell = board.getCell(new Position(2, 1));

        eliteArcherCell.setCharacter(eliteArcher);

        assertEquals(
                0,
                CharacterAbilityQuery.getCaptureContribution(
                        leaderCell,
                        eliteArcherCell,
                        getCurrentPhase(GamePhaseType.Actions)
                )
        );
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForAlignedEliteArcherAtDistanceThree() {
        Board board = new Board();

        Character eliteArcher = createCharacter(CharacterType.EliteArcher, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell eliteArcherCell = board.getCell(new Position(3, 0));

        eliteArcherCell.setCharacter(eliteArcher);

        assertEquals(
                0,
                CharacterAbilityQuery.getCaptureContribution(
                        leaderCell,
                        eliteArcherCell,
                        getCurrentPhase(GamePhaseType.Actions)
                )
        );
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
                    CharacterAbilityQuery.getCaptureContribution(leaderCell, assassinCell, getCurrentPhase(GamePhaseType.Actions))
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

                assertEquals(0, CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell, getCurrentPhase(GamePhaseType.Actions)));
            }
        }
    }

    @Test
    public void getCaptureContribution_shouldThrowWhenCharacterCellIsEmpty() {
        Board board = new Board();

        Cell characterCell = board.getCell(new Position(3, 2));
        Cell leaderCell = board.getCell(new Position(3, 3));

        assertThrows(IllegalArgumentException.class, () ->
                CharacterAbilityQuery.getCaptureContribution(leaderCell, characterCell, getCurrentPhase(GamePhaseType.Actions))
        );
    }

    @Test
    public void getCaptureContribution_shouldReturnLeaderCaptureValueForAlignedSniperAtTurnStart() {
        Board board = new Board();

        Character sniper = createCharacter(CharacterType.Sniper, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell sniperCell = board.getCell(new Position(3, 0));

        sniperCell.setCharacter(sniper);

        GamePhase turnStartPhase = getCurrentPhase(GamePhaseType.TurnStart);

        assertEquals(
                CharacterAbilityQuery.LEADER_CAPTURE_VALUE,
                CharacterAbilityQuery.getCaptureContribution(
                        leaderCell,
                        sniperCell,
                        turnStartPhase
                )
        );
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForUnalignedSniperAtTurnStart() {
        Board board = new Board();

        Character sniper = createCharacter(CharacterType.Sniper, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell sniperCell = board.getCell(new Position(1, 1));

        sniperCell.setCharacter(sniper);

        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(leaderCell, sniperCell,
                getCurrentPhase(GamePhaseType.TurnStart)));
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForSniperDuringActionsPhaseAtDistanceTwoOrMore() {
        Board board = new Board();

        Character sniper = createCharacter(CharacterType.Sniper, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell sniperCell = board.getCell(new Position(3, 0));

        sniperCell.setCharacter(sniper);

        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(leaderCell, sniperCell,
                getCurrentPhase(GamePhaseType.Actions)));
    }

    @Test
    public void getCaptureContribution_shouldReturnZeroForEnemySniperAtTurnStart() {
        Board board = new Board();

        Character sniper = createCharacter(CharacterType.Sniper, TeamColor.White);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell sniperCell = board.getCell(new Position(3, 0));

        sniperCell.setCharacter(sniper);

        assertEquals(0, CharacterAbilityQuery.getCaptureContribution(leaderCell, sniperCell,
                getCurrentPhase(GamePhaseType.TurnStart)));
    }

    @Test
    public void getCaptureContribution_shouldReturnOneForAdjacentSniperOutsideTurnStart() {
        Board board = new Board();

        Character sniper = createCharacter(CharacterType.Sniper, TeamColor.Black);
        Cell leaderCell = board.getCell(new Position(3, 3));
        Cell sniperCell = board.getCell(new Position(3, 2));

        sniperCell.setCharacter(sniper);

        assertEquals(1, CharacterAbilityQuery.getCaptureContribution(leaderCell, sniperCell,
                getCurrentPhase(GamePhaseType.Actions)));
    }

    @Test
    public void canAct_shouldReturnFalseForOpposingTeam() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();
        Character character = createCharacter(CharacterType.Acrobat, TeamColor.White);
        assertFalse(CharacterAbilityQuery.canAct(game, phase, character));
    }

    @Test
    public void canAct_shouldReturnFalseForNemesis() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();
        Character character = createCharacter(CharacterType.Nemesis, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.canAct(game, phase, character));
    }

    @Test
    public void canAct_shouldReturnTrueForCharacterThatHasNotActed() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        assertTrue(CharacterAbilityQuery.canAct(game, phase, character));
    }

    @Test
    public void canAct_shouldReturnFalseForCharacterThatAlreadyActed() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();

        Character character = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        CharacterAction action = new CharacterAction(character, new ArrayList<>());

        phase.getActions().add(action);
        assertFalse(CharacterAbilityQuery.canAct(game, phase, character));
    }

    @Test
    public void canAct_shouldAllowHermitWhenCubHasNotActed() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);

        assertTrue(CharacterAbilityQuery.canAct(game, phase, hermit));
    }

    @Test
    public void canAct_shouldAllowHermitImmediatelyAfterCub() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        CharacterAction cubAction = new CharacterAction(cub, new ArrayList<>());
        phase.getActions().add(cubAction);

        assertTrue(CharacterAbilityQuery.canAct(game, phase, hermit));
    }

    @Test
    public void canAct_shouldNotAllowHermitIfAnotherActionWasPlayedAfterCub() {
        Game game = createTestGame(new Board());
        ActionsPhase phase = createActionsPhase();

        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.Black);

        phase.getActions().add(new CharacterAction(cub, new ArrayList<>()));

        phase.getActions().add(new CharacterAction(acrobat, new ArrayList<>()));

        assertFalse(CharacterAbilityQuery.canAct(game, phase, hermit));
    }

    @Test
    public void canAct_shouldAllowTransformedHermitWhenCubHasNotActed() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character acrobat = frogifyCharacter(hermit, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), hermit, acrobat);
        ActionsPhase phase = createActionsPhase();

        assertTrue(CharacterAbilityQuery.canAct(game, phase, acrobat));
    }

    @Test
    public void canAct_shouldAllowTransformedHermitImmediatelyAfterCub() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character acrobat = frogifyCharacter(hermit, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), hermit, acrobat);
        ActionsPhase phase = createActionsPhase();
        phase.getActions().add(new CharacterAction(cub, new ArrayList<>()));

        // Le Cub vient de jouer : l'ex-Hermit conserve la contrainte et peut jouer juste après
        assertTrue(CharacterAbilityQuery.canAct(game, phase, acrobat));
    }

    @Test
    public void canAct_shouldNotAllowTransformedHermitIfAnotherActionWasPlayedAfterCub() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character acrobat = frogifyCharacter(hermit, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), hermit, acrobat);
        ActionsPhase phase = createActionsPhase();
        phase.getActions().add(new CharacterAction(cub, new ArrayList<>()));
        phase.getActions().add(new CharacterAction(bruiser, new ArrayList<>()));

        // La contrainte "immédiatement après" s'applique toujours à l'ex-Hermit
        assertFalse(CharacterAbilityQuery.canAct(game, phase, acrobat));
    }

    @Test
    public void canAct_shouldAllowTransformedCubWhenHermitHasNotActed() {
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character acrobat = frogifyCharacter(cub, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), cub, acrobat);
        ActionsPhase phase = createActionsPhase();

        assertTrue(CharacterAbilityQuery.canAct(game, phase, acrobat));
    }

    @Test
    public void canAct_shouldNotAllowTransformedCubIfAnotherActionWasPlayedAfterHermit() {
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character froggedCub = frogifyCharacter(cub, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), cub, froggedCub);
        ActionsPhase phase = createActionsPhase();
        phase.getActions().add(new CharacterAction(hermit, new ArrayList<>()));
        phase.getActions().add(new CharacterAction(bruiser, new ArrayList<>()));

        assertFalse(CharacterAbilityQuery.canAct(game, phase, froggedCub));
    }

    @Test
    public void canAct_shouldReturnFalseForTransformedHermitThatAlreadyActed() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character froggedHermit = frogifyCharacter(hermit, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), hermit, froggedHermit);
        ActionsPhase phase = createActionsPhase();
        // L'action a été jouée avec la forme Hermit, puis le personnage a été transformé
        phase.getActions().add(new CharacterAction(hermit, new ArrayList<>()));

        // Comparaison par ID : le personnage transformé a déjà agi
        assertFalse(CharacterAbilityQuery.canAct(game, phase, froggedHermit));
    }

    @Test
    public void canAct_shouldReturnFalseForTransformedHermitOfOpposingTeam() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character froggedHermit = frogifyCharacter(hermit, TeamColor.White);

        Game game = createTestGameWithTransformedState(new Board(), hermit, froggedHermit);
        ActionsPhase phase = createActionsPhase(); // tour des Black

        assertFalse(CharacterAbilityQuery.canAct(game, phase, froggedHermit));
    }

    @Test
    public void canAct_shouldNotAllowHermitIfTransformedCubActedThenAnotherActionWasPlayed() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.Black);
        Character cub = createCharacter(CharacterType.Cub, TeamColor.Black);

        Character froggedCub = frogifyCharacter(cub, TeamColor.Black);
        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);

        Game game = createTestGameWithTransformedState(new Board(), cub, froggedCub);
        ActionsPhase phase = createActionsPhase();
        // Le Cub a agi, puis a été transformé, puis une autre action a été jouée
        phase.getActions().add(new CharacterAction(froggedCub, new ArrayList<>()));
        phase.getActions().add(new CharacterAction(bruiser, new ArrayList<>()));

        assertFalse(CharacterAbilityQuery.canAct(game, phase, hermit));
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
        GameHistory gameHistory = createTestGameHistory();
        Character character = createCharacter(CharacterType.Archer, TeamColor.Black);
        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenAbilityIsNotBlocked() {
        Board board = new Board();

        Character character = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(character);
        Game game = createTestGame(board);
        GameHistory gameHistory = createTestGameHistory();
        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character));
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
        GameHistory gameHistory = createTestGameHistory();
        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, gameHistory, character));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenNoCurrentPhaseExists() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        board.getCell(new Position(3, 3)).setCharacter(bruiser);

        Game game = createTestGame(board);

        GameHistory history = createTestGameHistory();
        history.getEntries().clear();

        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenMovedByNonTactician() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character manipulator = createCharacter(CharacterType.Manipulator, TeamColor.Black);

        Position bruiserPos = new Position(3, 3);
        board.getCell(bruiserPos).setCharacter(bruiser);

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        addCharacterMovementAction(history, manipulator, bruiser, bruiserPos, new Position(3, 2));

        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenTacticianMovesAnotherCharacter() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character tactician = createCharacter(CharacterType.Tactician, TeamColor.Black);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.Black);

        board.getCell(new Position(3, 3)).setCharacter(bruiser);
        Position acrobatPos = new Position(2, 2);
        board.getCell(acrobatPos).setCharacter(acrobat);

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        addCharacterMovementAction(history, tactician, acrobat, acrobatPos, new Position(3, 2));

        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
    }

    @Test
    public void canUseActiveAbility_shouldReturnFalseWhenMovedByFriendlyTactician() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character tactician = createCharacter(CharacterType.Tactician, TeamColor.Black);

        Position bruiserPos = new Position(3, 3);
        board.getCell(bruiserPos).setCharacter(bruiser);

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        addCharacterMovementAction(history, tactician, bruiser, bruiserPos, new Position(3, 2));

        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
    }

    @Test
    public void canUseActiveAbility_shouldReturnTrueWhenMovedByEnemyTactician() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character tactician = createCharacter(CharacterType.Tactician, TeamColor.White);

        Position bruiserPos = new Position(3, 3);
        board.getCell(bruiserPos).setCharacter(bruiser);

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        addCharacterMovementAction(history, tactician, bruiser, bruiserPos, new Position(3, 2));

        assertTrue(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
    }

    @Test
    public void canUseActiveAbility_shouldReturnFalseWhenTransformedCharacterWasMovedByFriendlyTactician() {
        Board board = new Board();

        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character transformedBruiser = Character.transform(bruiser, CharacterType.Cub, TeamColor.Black);

        Character tactician = createCharacter(CharacterType.Tactician, TeamColor.Black);

        Position bruiserPos = new Position(3, 3);
        board.getCell(bruiserPos).setCharacter(transformedBruiser);

        Game game = createTestGame(board);
        GameHistory history = createTestGameHistory();

        addCharacterMovementAction(history, tactician, bruiser, bruiserPos, new Position(3, 2));

        assertFalse(CharacterAbilityQuery.canUseActiveAbility(game, history, bruiser));
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
    public void getExistingTransformedFrogState_shouldReturnNullWhenNoCharacterIsTransformed() {
        Game game = createTestGame(new Board());

        assertNull(CharacterAbilityQuery.getExistingTransformedFrogState(game));
    }

    @Test
    public void getExistingTransformedFrogState_shouldReturnFrogState() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        TransformedState state = CharacterAbilityQuery.getExistingTransformedFrogState(game);

        assertNotNull(state);
        assertEquals(hermit.getId(), state.getCharacterId());
        assertEquals(CharacterType.Frog, state.getCharacterAfter().getCharacterType());
        assertEquals(CharacterType.Hermit, state.getCharacterBefore().getCharacterType());
    }

    @Test
    public void getExistingTransformedFrogState_shouldReturnNullWhenOnlyNonFrogTransformationExists() {
        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character cub = Character.transform(bruiser, CharacterType.Cub, TeamColor.Black);
        Game game = createTestGameWithTransformedState(new Board(), bruiser, cub);

        assertNull(CharacterAbilityQuery.getExistingTransformedFrogState(game));
    }

    @Test
    public void getExistingTransformedFrogState_shouldIgnoreNonFrogTransformationsWhenFrogExists() {
        Character bruiser = createCharacter(CharacterType.Bruiser, TeamColor.Black);
        Character cub = Character.transform(bruiser, CharacterType.Cub, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);

        Game game = createTestGame(new Board());
        game.addTransformedState(bruiser, cub);
        game.addTransformedState(hermit, frog);

        TransformedState state = CharacterAbilityQuery.getExistingTransformedFrogState(game);

        assertNotNull(state);
        assertEquals(hermit.getId(), state.getCharacterId());
    }

    @Test
    public void getExistingTransformedFrogState_shouldThrowWhenMultipleFrogsExist() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.White);

        Game game = createTestGame(new Board());
        game.addTransformedState(hermit, frogifyCharacter(hermit, TeamColor.White));
        game.addTransformedState(acrobat, frogifyCharacter(acrobat, TeamColor.White));

        assertThrows(IllegalStateException.class, () ->
                CharacterAbilityQuery.getExistingTransformedFrogState(game));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnTrueWhenActionsPhaseIsEmpty() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        Turn turn = createTurn();

        assertTrue(CharacterAbilityQuery.isFrogTransformationOver(turn, getTransformedState(game, hermit)));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnFalseWhenCharacterWasTransformedDuringTurn() {
        Character shaman = createCharacter(CharacterType.Shaman, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        Turn turn = createTurn();
        getActionsPhase(turn).getActions().add(
                createTransformAction(shaman, hermit, frog, new Position(3, 5)));

        assertFalse(CharacterAbilityQuery.isFrogTransformationOver(turn, getTransformedState(game, hermit)));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnTrueWhenAnotherCharacterWasTransformedDuringTurn() {
        Character shaman = createCharacter(CharacterType.Shaman, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.White);
        Character otherFrog = frogifyCharacter(acrobat, TeamColor.White);

        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        // The Shaman transformed another character during this turn
        Turn turn = createTurn();
        getActionsPhase(turn).getActions().add(
                createTransformAction(shaman, acrobat, otherFrog, new Position(3, 5)));

        assertTrue(CharacterAbilityQuery.isFrogTransformationOver(turn, getTransformedState(game, hermit)));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnTrueWhenCharacterOnlyMovedDuringTurn() {
        Character shaman = createCharacter(CharacterType.Shaman, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        // The motion concerns the same character but is not a transformation
        Turn turn = createTurn();
        getActionsPhase(turn).getActions().add(
                createMoveAction(shaman, frog, new Position(3, 5), new Position(3, 4)));

        assertTrue(CharacterAbilityQuery.isFrogTransformationOver(turn, getTransformedState(game, hermit)));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnFalseWhenTransformationIsNotTheFirstAction() {
        Character shaman = createCharacter(CharacterType.Shaman, TeamColor.Black);
        Character acrobat = createCharacter(CharacterType.Acrobat, TeamColor.Black);
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        Turn turn = createTurn();
        ActionsPhase actionsPhase = getActionsPhase(turn);
        actionsPhase.getActions().add(
                createMoveAction(acrobat, acrobat, new Position(3, 3), new Position(3, 2)));
        actionsPhase.getActions().add(
                createTransformAction(shaman, hermit, frog, new Position(3, 5)));

        assertFalse(CharacterAbilityQuery.isFrogTransformationOver(turn, getTransformedState(game, hermit)));
    }

    @Test
    public void isFrogTransformationOver_shouldReturnTrueWhenTransformationWasDoneInPreviousTurn() {
        Character hermit = createCharacter(CharacterType.Hermit, TeamColor.White);
        Character frog = frogifyCharacter(hermit, TeamColor.White);
        Game game = createTestGameWithTransformedState(new Board(), hermit, frog);

        // The transformation was done in an earlier turn: the current turn has no matching action
        Turn previousTurn = createTurn();
        Turn currentTurn = createTurn();
        getActionsPhase(previousTurn).getActions().add(
                createTransformAction(
                        createCharacter(CharacterType.Shaman, TeamColor.Black),
                        hermit, frog, new Position(3, 5)));

        TransformedState state = getTransformedState(game, hermit);

        assertFalse(CharacterAbilityQuery.isFrogTransformationOver(previousTurn, state));
        assertTrue(CharacterAbilityQuery.isFrogTransformationOver(currentTurn, state));
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