package com.leaders.puzzlelogic.entities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.puzzlelogic.enums.PuzzleCategory;
import com.leaders.puzzlelogic.enums.PuzzleLifetime;

import org.json.JSONException;
import org.json.JSONObject;

public final class OfficialPuzzleSave extends PuzzleSave {
    private final int id;

    public OfficialPuzzleSave(@NonNull Context context, int id,
                              @NonNull PuzzleLifetime lifetime,
                              @NonNull JSONObject datas, boolean solved) {
        super(String.format(context.getString(R.string.official_puzzle_name_format), id), lifetime, datas, solved);
        this.id = id;
    }

    public OfficialPuzzleSave(JSONObject joPuzzle) throws JSONException {
        super(joPuzzle);
        this.id = joPuzzle.getInt("id");
    }

    public final int getId() {
        return id;
    }

    @NonNull
    @Override
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzle = super.getAsJsonObject();
        joPuzzle.put("id", id);
        return joPuzzle;
    }

    @NonNull
    @Override
    public String getAuthor() {
        return "";
    }

    @NonNull
    @Override
    public PuzzleCategory getCategory() {
        return PuzzleCategory.Official;
    }
}
