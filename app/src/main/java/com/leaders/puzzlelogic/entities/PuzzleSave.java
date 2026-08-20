package com.leaders.puzzlelogic.entities;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.leaders.puzzlelogic.enums.PuzzleCategory;
import com.leaders.puzzlelogic.enums.PuzzleLifetime;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PuzzleSave {
    @NonNull
    private final String name;
    @NonNull
    private final PuzzleLifetime lifetime;
    @NonNull
    protected final JSONObject datas;
    private boolean solved;

    protected PuzzleSave(@NonNull String name, @NonNull PuzzleLifetime lifetime,
                         @NonNull JSONObject datas, boolean solved) {
        this.name = name;
        this.lifetime = lifetime;
        this.datas = datas;
        this.solved = solved;
    }

    protected PuzzleSave(@NonNull JSONObject joPuzzleSave) throws JSONException {
        this(
                joPuzzleSave.getString("name"),
                PuzzleLifetime.valueOf(joPuzzleSave.getString("lifetime")),
                joPuzzleSave.getJSONObject("datas"),
                joPuzzleSave.getBoolean("solved")
        );
    }

    @NonNull
    @CallSuper
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzleSave = new JSONObject();
        joPuzzleSave.put("name", getName());
        joPuzzleSave.put("lifetime", getLifetime().name());
        joPuzzleSave.put("datas", datas);
        joPuzzleSave.put("solved", isSolved());
        return joPuzzleSave;
    }

    @NonNull
    public final String getName() {
        return name;
    }

    @NonNull
    public abstract String getAuthor();

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public final boolean isSolved() {
        return solved;
    }

    @NonNull
    public abstract PuzzleCategory getCategory();

    @NonNull
    public PuzzleLifetime getLifetime() {
        return lifetime;
    }
}
