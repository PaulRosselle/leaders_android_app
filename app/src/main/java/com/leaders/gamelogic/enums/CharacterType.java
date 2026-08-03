package com.leaders.gamelogic.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public enum CharacterType {
    Acrobat,
    Archer,
    Assassin,
    Brewmaster,
    Bruiser,
    ClawLauncher,
    Cub,
    Hermit,
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
     * Returns the {@link CharacterCard} corresponding to this character type.
     *
     * @return the matching {@link CharacterCard}
     * @throws NoSuchElementException if no matching card is found
     */
    public CharacterCard getCharacterCard() {
        switch (this) {
            case Acrobat: return CharacterCard.Acrobat;
            case Archer: return CharacterCard.Archer;
            case Assassin: return CharacterCard.Assassin;
            case Brewmaster: return CharacterCard.Brewmaster;
            case Bruiser: return CharacterCard.Bruiser;
            case ClawLauncher: return CharacterCard.ClawLauncher;
            case Cub:
            case Hermit:
                return CharacterCard.HermitAndCub;
            case Illusionist: return CharacterCard.Illusionist;
            case Jailer: return CharacterCard.Jailer;
            case LeaderKing: return CharacterCard.LeaderKing;
            case LeaderQueen: return CharacterCard.LeaderQueen;
            case Manipulator: return CharacterCard.Manipulator;
            case Nemesis: return CharacterCard.Nemesis;
            case Protector: return CharacterCard.Protector;
            case Rider: return CharacterCard.Rider;
            case RoyalGuard: return CharacterCard.RoyalGuard;
            case Vizier: return CharacterCard.Vizier;
            case Wanderer: return CharacterCard.Wanderer;
            default: throw new NoSuchElementException(String.format("No character card found matching %s", this));
        }
    }

    /**
     * Returns all {@link CharacterType} values matching the given {@link CharacterCard}.
     *
     * @param characterCard the card to match against
     * @return the list of matching {@link CharacterType} values
     */
    public static List<CharacterType> getCharacterTypesMatchingCard(CharacterCard characterCard) {
        ArrayList<CharacterType> matchingTypes = new ArrayList<>();
        for (CharacterType characterType : CharacterType.values()) {
            if (characterType.getCharacterCard() == characterCard) {
                matchingTypes.add(characterType);
            }
        }
        return matchingTypes;
    }
}
