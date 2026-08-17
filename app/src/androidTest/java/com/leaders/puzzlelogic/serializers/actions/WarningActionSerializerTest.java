package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class WarningActionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        WarningAction action =
                new WarningAction(WarningType.Barrage, TeamColor.Black, 2);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new WarningActionSerializer(),
                action,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
