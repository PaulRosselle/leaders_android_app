package com.leaders.puzzlelogic.serializers;

import static org.junit.Assert.assertEquals;

import com.leaders.gamelogic.entities.Character;

import org.json.JSONObject;

public final class SerializerRoundTripTestSupport {
    private SerializerRoundTripTestSupport() {
    }

    public static SerializationContext contextWith(Character... characters) {
        SerializationContext context = new SerializationContext();
        for (Character character : characters) {
            context.getCharactersMap().put(character.getId(), character);
        }
        return context;
    }

    public static <T> void assertRoundTrip(
            IJsonSerializer<T> serializer,
            T object,
            SerializationContext context
    ) throws Exception {
        JSONObject firstJson = serializer.getAsJson(object);
        T deserialized = serializer.getFromJson(firstJson, context);
        JSONObject secondJson = serializer.getAsJson(deserialized);

        assertEquals(firstJson.toString(), secondJson.toString());
    }
}
