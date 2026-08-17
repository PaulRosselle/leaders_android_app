package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class RecruitmentActionMotionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Character character = Character.create(CharacterType.Acrobat, TeamColor.Black);
        RecruitmentActionMotion motion =
                new RecruitmentActionMotion(
                        RecruitmentMotionType.Add,
                        character,
                        new Position(3, 3)
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new RecruitmentActionMotionSerializer(),
                motion,
                SerializerRoundTripTestSupport.contextWith(character)
        );
    }
}
