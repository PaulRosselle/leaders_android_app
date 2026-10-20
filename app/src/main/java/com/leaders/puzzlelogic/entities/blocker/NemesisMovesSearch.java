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
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.puzzlelogic.utilities.CharactersPermutationUtils;
import com.leaders.puzzlelogic.utilities.blocker.NemesisMovesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NemesisMovesSearch {

    private NemesisMovesSearch(){
        throw new AssertionError("Cannot instantiate NemesisMovesSearch");
    }
    private static final String PUZZLE_URL = "https://mthbrt.github.io/Leaders-editor/#white=C5:13,C3:8,E1:4,A1:17,C1:18,E6:2&black=E3:12,C4:7,B4:5,B2:14,B1:1";

    public static void main(String[] args) {
        GameHistory gameHistory = PuzzleImportUtils.getFromLbeUrl(null, PUZZLE_URL);

        Map<String, CharacterAction> blockingMoves = searchNemesisBlockingMoves(gameHistory);

        JSONObject jsonObject = getAsJson(blockingMoves);
        System.out.println(jsonObject);
    }

    private static Map<String, CharacterAction> searchNemesisBlockingMoves(@NonNull GameHistory gameHistory) {
        Game game = GameFactory.create(gameHistory);
        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalStateException("Nemesis moves search requires an ActionsPhase");
        }

        // TODO - check if enemy teams contains Nemesis

        TeamColor activeTeam = ((ActionsPhase) currentPhase).getTurnTeamColor();
        List<Character> characters = getActiveTeamCharacters(game, activeTeam);

        long permutationCount = CharactersPermutationUtils.getPermutationCount(characters);
        if (permutationCount == 0) {
            throw new IllegalStateException("Invalid puzzle : no playable character");
        }

        Map<String, CharacterAction> blockingMoves = new HashMap<>();
        Set<String> exploredStates = new HashSet<>();

        try {
            for (int permutationIndex = 0; permutationIndex < permutationCount; permutationIndex++) {
                NemesisMovesUtils.solve(
                        game, gameHistory, characters, permutationIndex,
                        blockingMoves, exploredStates, () -> false
                );
                System.out.printf("%s / %s%n", permutationIndex, permutationCount);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Search interruption is not allowed for Nemesis Moves", e);
        }

        return blockingMoves;
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
}
