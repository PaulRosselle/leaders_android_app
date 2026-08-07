package com.leaders.gamelogic.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TeamColorTest {

    @Test
    public void getOpposite_shouldReturnWhiteForBlack() {
        assertEquals(
                TeamColor.White,
                TeamColor.Black.getOpposite()
        );
    }

    @Test
    public void getOpposite_shouldReturnBlackForWhite() {
        assertEquals(
                TeamColor.Black,
                TeamColor.White.getOpposite()
        );
    }
}