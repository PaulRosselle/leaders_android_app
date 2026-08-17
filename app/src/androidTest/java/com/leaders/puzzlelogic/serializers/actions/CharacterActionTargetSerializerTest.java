package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class CharacterActionTargetSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Character character = Character.create(CharacterType.Acrobat, TeamColor.Black);
        CharacterActionTarget target =
                new CharacterActionTarget(
                        character,
                        new Position(2, 2),
                        new Position(3, 3)
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new CharacterActionTargetSerializer(),
                target,
                SerializerRoundTripTestSupport.contextWith(character)
        );
    }
}
