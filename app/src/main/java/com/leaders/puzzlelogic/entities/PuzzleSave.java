package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PuzzleSave {
    @NonNull
    private final String name;
    @NonNull
    protected final JSONObject datas;
    private final boolean solved;


    protected PuzzleSave(@NonNull String name, @NonNull JSONObject datas, boolean solved) {
        this.name = name;
        this.datas = datas;
        this.solved = solved;
    }

    @NonNull
    public abstract JSONObject getAsJsonObject() throws JSONException;

    @NonNull
    public final String getName() {
        return name;
    }

    @NonNull
    public abstract String getAuthor();

    public final boolean isSolved() {
        return solved;
    }
}
