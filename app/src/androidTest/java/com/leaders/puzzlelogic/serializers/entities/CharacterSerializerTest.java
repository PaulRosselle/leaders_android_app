package com.leaders.puzzlelogic.serializers.entities;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class CharacterSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Character character = Character.create(CharacterType.LeaderQueen, TeamColor.Black);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new CharacterSerializer(),
                character,
                SerializerRoundTripTestSupport.contextWith(character)
        );
    }
}
