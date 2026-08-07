package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TeamColorTest {

    @Test
    public void getOpposite_shouldReturnOther() {
        assertEquals(
                TeamColor.White,
                TeamColor.Black.getOpposite()
        );

        assertEquals(
                TeamColor.Black,
                TeamColor.White.getOpposite()
        );
    }

    @Test
    public void shouldContainExactlyTwoTeamColors() {
        assertEquals(2, TeamColor.values().length);
    }
}