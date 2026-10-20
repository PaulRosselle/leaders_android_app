package com.leaders.puzzlelogic.entities.blocker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.LbeUtils;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.utilities.GameStateHashUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NemesisMoves {
    private final Map<String, Position> moves;

    private NemesisMoves(@NonNull Map<String, Position> moves) {
        this.moves = moves;
    }

    @Nullable
    public Position getDestination(@NonNull String stateHash) {
        return moves.get(stateHash);
    }

    @NonNull
    public InteractionTarget getBlockingTarget(@NonNull GameHistory gameHistory,
                                               @NonNull InteractionRequest request) {
        List<InteractionTarget> nemesisTargets = request.getLegalTargets();
        if (nemesisTargets.isEmpty()) {
            throw new IllegalStateException("Nemesis action require at least one target");
        }

        String stateHash = GameStateHashUtils.createHash(GameFactory.create(gameHistory), gameHistory);
        Position existingDestination = getDestination(stateHash);
        if (existingDestination != null) {
            return NemesisBlocker.getNemesisTargetFromDestination(nemesisTargets, existingDestination);
        }
        return nemesisTargets.get(0);
    }

    @Nullable
    public static NemesisMoves loadForPuzzle(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        if (!(puzzleSave instanceof OfficialPuzzleSave)) {
            return null;
        }

        OfficialPuzzleSave officialPuzzleSave = (OfficialPuzzleSave) puzzleSave;

        JSONObject joNemesisMoves = JsonUtils.loadNemesisMoves(context);
        try {
            JSONArray jaPuzzles = joNemesisMoves.getJSONArray("puzzles");
            for (int i = 0; i < jaPuzzles.length(); i++) {
                JSONObject joPuzzle = jaPuzzles.getJSONObject(i);
                if (joPuzzle.getInt("id") == officialPuzzleSave.getId()) {
                    return fromJson(context, joPuzzle.getJSONObject("nemesis_moves"));
                }
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Error while reading nemesis moves json", e);
        }

        return null;
    }

    @NonNull
    private static NemesisMoves fromJson(@Nullable Context context, @NonNull JSONObject jsonObject) throws JSONException {
        Map<String, Position> moves = new HashMap<>();

        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String stateHash = keys.next();
            Position destination = LbeUtils.getPositionFromExportStr(context, jsonObject.getString(stateHash));
            moves.put(stateHash, destination);
        }

        return new NemesisMoves(moves);
    }
}
