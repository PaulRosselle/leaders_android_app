package com.leaders.gamelogic.actions;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;

public class RecruitmentActionTarget {

    @NonNull
    private final Character character;

    @NonNull
    private final Position position;


    public RecruitmentActionTarget(@NonNull Character character, @NonNull Position position) {
        this.character = character;
        this.position = position;
    }

    @NonNull
    public Character getCharacter() {
        return character;
    }

    @NonNull
    public Position getPosition() {
        return position;
    }
}
