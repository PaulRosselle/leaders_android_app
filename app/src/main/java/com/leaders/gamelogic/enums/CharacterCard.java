package com.leaders.gamelogic.enums;

import java.util.NoSuchElementException;

public enum CharacterCard {
    Acrobat,
    Archer,
    Assassin,
    Brewmaster,
    Bruiser,
    ClawLauncher,
    HermitAndCub,
    Illusionist,
    Jailer,
    LeaderKing,
    LeaderQueen,
    Manipulator,
    Nemesis,
    Protector,
    Rider,
    RoyalGuard,
    Vizier,
    Wanderer;

    /**
     * Returns the ability types associated with this character card.
     * @return an array of {@link AbilityType} corresponding to this card
     * @throws NoSuchElementException if this card is not handled by any case
     *         (should not occur, since all enum constants are covered)
     */
    public AbilityType[] getAbilityTypes() {
        switch (this){
            case Acrobat:
            case Brewmaster:
            case Bruiser:
            case ClawLauncher:
            case Illusionist:
            case Manipulator:
            case Rider:
            case RoyalGuard:
            case Wanderer:
                return new AbilityType[] { AbilityType.Active };
            case Archer:
            case Assassin:
            case Jailer:
            case Protector:
            case Vizier:
                return new AbilityType[] { AbilityType.Passive };
            case HermitAndCub:
            case Nemesis:
                return new AbilityType[] { AbilityType.Special };
            case LeaderKing:
            case LeaderQueen:
                return new AbilityType[0];
            default: throw new NoSuchElementException(String.format("No ability type found matching %s", this));
        }
    }

    /** Indicates whether this character card is a leader.*/
    public boolean isLeader() {
        return this == LeaderKing || this == LeaderQueen;
    }

    /** Indicates whether this character card can be recruited.*/
    public boolean canBeRecruited() {
        return !isLeader();
    }
}
