package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public final class CustomPuzzleSave extends PuzzleSave {
    @NonNull
    private final String author;

    public CustomPuzzleSave(@NonNull String name, @NonNull String author, @NonNull JSONObject datas, boolean solved) {
        super(name, datas, solved);
        this.author = author;
    }

    @NonNull
    @Override
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzle = new JSONObject();
        joPuzzle.put("name", getName());
        joPuzzle.put("author", author);
        joPuzzle.put("datas", datas);
        joPuzzle.put("solved", isSolved());
        return joPuzzle;
    }

    @NonNull
    @Override
    public String getAuthor() {
        return author;
    }
}
