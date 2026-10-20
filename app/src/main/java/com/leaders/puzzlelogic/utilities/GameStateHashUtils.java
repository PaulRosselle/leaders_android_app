package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.CharacterAbilityQuery;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;

import java.util.List;
import java.util.stream.Collectors;

public final class GameStateHashUtils {
    private static final String GLOBAL_SEPARATOR = ";";

    private GameStateHashUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Creates a hash string representing the current game state and character playability.
     *
     * @param game the current game
     * @param gameHistory the game history used to determine the current turn and playable characters
     * @return a string encoding the board state, the characters that can still act during the current
     *         turn, and the playable characters
     * @throws IllegalArgumentException if the current phase is not an {@link ActionsPhase}
     * @throws NullPointerException if {@code game} or {@code gameHistory} is {@code null}
     */
    public static String createHash(@NonNull Game game, @NonNull GameHistory gameHistory) {
        List<Cell> characterCells = BoardQuery.findCharacterCells(game.getBoard(), null, null);

        return String.format("%s%s%s%s%s",
                createBoardHash(characterCells),
                GLOBAL_SEPARATOR,
                createTurnHash(gameHistory, characterCells),
                GLOBAL_SEPARATOR,
                createPlayabilityHash(game, gameHistory)
        );
    }

    /**
     * Creates a hash string representing the positions and character types of the specified cells.
     *
     * @param characterCells the cells to include in the board representation
     * @return a string encoding the state of the specified cells
     * @throws NullPointerException if {@code characterCells} is {@code null}
     */
    private static String createBoardHash(@NonNull List<Cell> characterCells) {
        StringBuilder builder = new StringBuilder();

        for (Cell characterCell : characterCells) {
            builder.append(createCellHash(characterCell));
        }

        return builder.toString();
    }

    /**
     * Creates a hash string representing the characters that can still act during the current turn.
     *
     * @param gameHistory the game history containing the current phase
     * @param characterCells the cells containing the characters to evaluate
     * @return a string encoding the cells of characters that can still act during the current turn
     * @throws IllegalArgumentException if the current phase is not an {@link ActionsPhase}
     * @throws NullPointerException if {@code gameHistory} or {@code characterCells} is {@code null}
     */
    private static String createTurnHash(@NonNull GameHistory gameHistory, @NonNull List<Cell> characterCells) {
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("In progress actions phase required");
        }
        ActionsPhase currentActionsPhase = (ActionsPhase) currentPhase;

        // Here we're trying to get a list of the character that can still play during the turn.
        List<Cell> turnCharacterCells = characterCells.stream()
                .filter(cell -> CharacterAbilityQuery.canAct(currentActionsPhase, cell.getCharacter()))
                .collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();

        for (Cell turnCharacterCell : turnCharacterCells) {
            builder.append(createCellHash(turnCharacterCell));
        }

        return builder.toString();
    }

    /**
     * Creates a hash string representing the playable characters in the current game state.
     *
     * @param game the current game
     * @param gameHistory the game history used to determine character playability
     * @return a string encoding the state of all playable characters
     * @throws NullPointerException if {@code game} or {@code gameHistory} is {@code null}
     */
    private static String createPlayabilityHash(@NonNull Game game, @NonNull GameHistory gameHistory) {
        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, gameHistory);

        StringBuilder builder = new StringBuilder();

        for (PlayableCharacter playableCharacter : playableCharacters) {
            builder.append(createPlayableCharacterHash(playableCharacter));
        }

        return builder.toString();
    }

    /**
     * Creates a hash string representing the state of the specified cell.
     *
     * @param cell the cell whose state is represented
     * @return a string encoding the cell's position and, if present, its character type
     * @throws NullPointerException if {@code cell} is {@code null}
     */
    private static String createCellHash(@NonNull Cell cell) {
        StringBuilder builder = new StringBuilder();

        if (cell.getCharacter() != null) {
            builder.append(createCharacterHash(cell.getCharacter()));
        }
        builder.append(createPositionHash(cell.getPosition()));

        return builder.toString();
    }

    /**
     * Creates a hash string representing the type of the specified character.
     *
     * @param character the character whose type is represented
     * @return a string encoding the ordinal of the character type
     * @throws NullPointerException if {@code character} is {@code null}
     */
    private static String createCharacterHash(@NonNull Character character) {
        final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return String.valueOf(CHARS.charAt(character.getCharacterType().ordinal()));
    }

    /**
     * Creates a string representation of the specified position.
     *
     * @param position the position to represent
     * @return a string encoding the position's coordinates
     * @throws NullPointerException if {@code position} is {@code null}
     */
    private static String createPositionHash(@NonNull Position position) {
        final int[] COLUMN_OFFSETS = {0, 4, 9, 15, 22, 28, 33};
        final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return String.valueOf(CHARS.charAt(COLUMN_OFFSETS[position.getX()] + position.getY() + 1));
    }

    /**
     * Creates a hash string representing the state of the specified playable character.
     *
     * @param playableCharacter the playable character whose state is represented
     * @return a string encoding the character's identity, position, mandatory status,
     *         and active ability availability
     * @throws NullPointerException if {@code playableCharacter} is {@code null}
     */
    private static String createPlayableCharacterHash(@NonNull PlayableCharacter playableCharacter) {
        return String.format("%s%s%s%s",
                createCharacterHash(playableCharacter.getCharacter()),
                createPositionHash(playableCharacter.getPosition()),
                playableCharacter.isMandatory() ? "1" : "0",
                playableCharacter.canUseActiveAbility() ? "1" : "0"
        );
    }
}
