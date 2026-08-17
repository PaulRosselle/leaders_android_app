package com.leaders.puzzlelogic.serializers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Position;

import org.json.JSONException;
import org.json.JSONObject;

public class PositionSerializer implements IJsonSerializer<Position> {
    @NonNull
    @Override
    public Position getFromJson(@NonNull JSONObject jsonObject,
                                @NonNull SerializationContext srlContext) throws JSONException {
        return new Position(jsonObject.getInt("x"), jsonObject.getInt("y"));
    }

    @NonNull
    @Override
    public JSONObject getAsJson(Position object) throws JSONException {
        JSONObject joPosition = new JSONObject();
        joPosition.put("x", object.getX());
        joPosition.put("y", object.getY());
        return joPosition;
    }
}
