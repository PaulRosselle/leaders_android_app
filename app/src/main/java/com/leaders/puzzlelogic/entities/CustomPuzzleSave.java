package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.puzzlelogic.enums.PuzzleCategory;
import com.leaders.puzzlelogic.enums.PuzzleLifetime;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

public final class CustomPuzzleSave extends PuzzleSave {
    @NonNull
    private final String author;

    public CustomPuzzleSave(@NonNull String name, @NonNull String author,
                            @NonNull PuzzleLifetime lifetime,
                            @NonNull JSONObject datas, boolean solved) {
        super(name, lifetime, datas, solved);
        this.author = author;
    }

    public CustomPuzzleSave(JSONObject joPuzzle) throws JSONException {
        super(joPuzzle);
        this.author = joPuzzle.getString("author");
    }

    @NonNull
    @Override
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzle = super.getAsJsonObject();
        joPuzzle.put("author", author);
        return joPuzzle;
    }

    @NonNull
    @Override
    public String getAuthor() {
        return author;
    }

    @NonNull
    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.Custom;
    }

    public static CustomPuzzleSave getDefault(@NonNull GameHistory gameHistory) {
        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            return new CustomPuzzleSave("", "", PuzzleLifetime.ActionsPhase,
                    serializer.getAsJson(gameHistory), false);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid default puzzle :" + e);
        }
    }
}
