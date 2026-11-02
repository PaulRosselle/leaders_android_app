package com.leaders.app.enums;

import com.leaders.gamelogic.enums.TeamColor;

import java.util.Random;

public enum FirstPlayerTeam {
    Random,
    Black,
    White;

    private static final Random random = new Random();

    public TeamColor getTeamColor() {
        switch (this) {
            case Random: return random.nextBoolean() ? TeamColor.Black : TeamColor.White;
            case Black: return TeamColor.Black;
            case White: return TeamColor.White;
            default: throw new IllegalStateException("No team color found matching first team: " + this);
        }
    }
}
