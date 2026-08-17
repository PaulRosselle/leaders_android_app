package com.leaders.puzzlelogic.entities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;

import org.json.JSONException;
import org.json.JSONObject;

public final class OfficialPuzzleSave extends PuzzleSave {
    private final int id;
    public OfficialPuzzleSave(@NonNull Context context, int id, @NonNull JSONObject datas, boolean solved) {
        super(String.format(context.getString(R.string.official_puzzle_name_format), id), datas, solved);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @NonNull
    @Override
    public JSONObject getAsJsonObject() throws JSONException {
        JSONObject joPuzzle = new JSONObject();
        joPuzzle.put("id", id);
        joPuzzle.put("datas", datas);
        return joPuzzle;
    }

    @NonNull
    @Override
    public String getAuthor() {
        return "";
    }
}
