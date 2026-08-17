package com.leaders.puzzlelogic.serializers.entities;

import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class PlayerSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Player player = new Player(TeamColor.Black, "Alice");

        SerializerRoundTripTestSupport.assertRoundTrip(
                new PlayerSerializer(),
                player,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
