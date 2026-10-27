package com.leaders.puzzlelogic.entities.blocker;

import androidx.annotation.NonNull;

import com.leaders.app.utilities.LbeUtils;
import com.leaders.app.utilities.PuzzleImportUtils;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.puzzlelogic.utilities.CharactersPermutationUtils;
import com.leaders.puzzlelogic.utilities.blocker.NemesisMovesUtils;
import com.leaders.puzzlelogic.utilities.blocker.SearchCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NemesisMovesSearch {
    private NemesisMovesSearch(){
        throw new AssertionError("Cannot instantiate NemesisMovesSearch");
    }

    // Full LBE urls and "#" content supported
    private static final String PUZZLE_URL = "#white=C5:4,E1:9,C1:6,F2:1,B3:13&black=A4:10,A2:2,F5:12,B5:16,F1:11";
    // Will be saved in the "app" folder at the project root
    private static final String SAVE_FILENAME = "save.json";

    public static void main(String[] args) {
        GameHistory gameHistory = PuzzleImportUtils.getFromLbeUrl(null, PUZZLE_URL);

        Map<String, CharacterAction> blockingMoves = searchNemesisBlockingMoves(gameHistory);

        saveAsJson(blockingMoves, new File(SAVE_FILENAME));

        System.out.println("\nTotal moves: " + blockingMoves.size());
    }

    @NonNull
    private static Map<String, CharacterAction> searchNemesisBlockingMoves(@NonNull GameHistory gameHistory) {
        Game game = GameFactory.create(gameHistory);
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalStateException("Nemesis moves search requires an ActionsPhase");
        }

        TeamColor activeTeam = ((ActionsPhase) currentPhase).getTurnTeamColor();
        if (BoardQuery.findCharacterCells(game.getBoard(), activeTeam.getOpposite(), CharacterType.Nemesis).isEmpty()) {
            throw new IllegalArgumentException("Enemy Nemesis is required to search for blocking moves");
        }

        List<Character> characters = getActiveTeamCharacters(game, activeTeam);

        long permutationCount = CharactersPermutationUtils.getPermutationCount(characters);
        if (permutationCount == 0) {
            throw new IllegalStateException("Invalid puzzle : no playable character");
        }

        Map<String, CharacterAction> blockingMoves = new HashMap<>();
        Set<String> exploredStates = new HashSet<>();
        Set<String> nemesisEvaluated = new HashSet<>();
        SearchCache searchCache = new SearchCache();
        try {
            logProgress(0, permutationCount);
            for (long permutationIndex = 0; permutationIndex < permutationCount; permutationIndex++) {
                NemesisMovesUtils.solve(
                        game, gameHistory, characters, permutationIndex,
                        blockingMoves, exploredStates, nemesisEvaluated, searchCache,
                        () -> false // Interruption is not allowed in this context
                );
                logProgress(permutationIndex + 1, permutationCount);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Search interruption is not allowed for Nemesis Moves", e);
        }

        return blockingMoves;
    }

    private static void logProgress(long permutationIndex, long permutationCount) {
        System.out.printf("\r%s / %s", permutationIndex, permutationCount);
        System.out.flush();
    }

    @NonNull
    private static List<Character> getActiveTeamCharacters(@NonNull Game game,
                                                           @NonNull TeamColor activeTeam) {
        List<Character> characters = new ArrayList<>();

        for (Cell cell : game.getBoard().getCells().values()) {
            Character character = cell.getCharacter();
            // Nemesis cannot use her action normally and is therefore not included within
            // the active team characters (she will still react to an enemy leader movement)
            if (character != null && character.getTeamColor() == activeTeam &&
                    character.getCharacterType() != CharacterType.Nemesis) {
                characters.add(character);
            }
        }

        return characters;
    }

    @NonNull
    private static JSONObject getAsJson(@NonNull Map<String, CharacterAction> blockingMoves) {
        JSONObject jsonObject = new JSONObject();

        try {
            for (Map.Entry<String, CharacterAction> entry : blockingMoves.entrySet()) {
                jsonObject.put(
                        entry.getKey(),
                        LbeUtils.getPositionExportStr(NemesisBlocker.getNemesisDestination(entry.getValue()))
                );
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Error while parsing blockingMoves as Json", e);
        }

        return jsonObject;
    }

    private static void saveAsJson(@NonNull Map<String, CharacterAction> blockingMoves,
                                   @NonNull File saveFile) {
        JSONObject jsonObject = getAsJson(blockingMoves);

        try (FileWriter writer = new FileWriter(saveFile)) {
            writer.write(jsonObject.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Error while writing save file: " + saveFile, e);
        }
    }
}
