package com.leaders.gamelogic.enums;

public enum TeamColor {
    Black,
    White;

    /**
     * Returns the color opposing this one.
     *
     * @return {@link TeamColor#White} if this is {@link TeamColor#Black},
     *         {@link TeamColor#Black} otherwise
     */
    public TeamColor getOpposite() {
        if (this == TeamColor.Black) {
            return TeamColor.White;
        }
        return TeamColor.Black;
    }
}
