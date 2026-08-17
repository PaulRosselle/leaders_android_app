package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.Collections;

public class CharacterActionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Character character = Character.create(CharacterType.Acrobat, TeamColor.Black);
        CharacterAction action =
                new CharacterAction(character, Collections.emptyList());

        SerializerRoundTripTestSupport.assertRoundTrip(
                new CharacterActionSerializer(),
                action,
                SerializerRoundTripTestSupport.contextWith(character)
        );
    }
}
