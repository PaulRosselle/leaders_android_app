package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.Collections;

public class CharacterActionMotionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        CharacterActionMotion motion =
                new CharacterActionMotion(
                        CharacterMotionType.Move,
                        Collections.emptyList()
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new CharacterActionMotionSerializer(),
                motion,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
