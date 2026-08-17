package com.leaders.puzzlelogic.serializers.actions;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

public class BanishmentActionSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        BanishmentAction action =
                new BanishmentAction(CharacterCard.Acrobat, TeamColor.Black);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new BanishmentActionSerializer(),
                action,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
