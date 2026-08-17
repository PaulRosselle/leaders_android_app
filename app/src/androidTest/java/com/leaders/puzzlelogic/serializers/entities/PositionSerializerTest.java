package com.leaders.puzzlelogic.serializers.entities;

import com.leaders.gamelogic.entities.Position;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class PositionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Position position = new Position(3, 2);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new PositionSerializer(),
                position,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
