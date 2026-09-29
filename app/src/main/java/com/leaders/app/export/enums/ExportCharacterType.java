package com.leaders.app.export.enums;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.enums.CharacterType;

public enum ExportCharacterType {
    ACROBAT,
    ARCHER,
    ASSASSIN,
    BREWMASTER,
    BRUISER,
    CLAW_LAUNCHER,
    CUB,
    HERMIT,
    ILLUSIONIST,
    JAILER,
    LEADER_KING,
    LEADER_QUEEN,
    MANIPULATOR,
    NEMESIS,
    PROTECTOR,
    RIDER,
    ROYAL_GUARD,
    VIZIER,
    WANDERER,
    LEADER_VERMILLION,
    FROG,
    SHAMAN,
    SNIPER,
    TACTICIAN,
    WISP;

    public CharacterType getAsCharacterType() {
        switch (this) {
            case ACROBAT: return CharacterType.Acrobat;
            case ARCHER: return CharacterType.Archer;
            case ASSASSIN: return CharacterType.Assassin;
            case BREWMASTER: return CharacterType.Brewmaster;
            case BRUISER: return CharacterType.Bruiser;
            case CLAW_LAUNCHER: return CharacterType.ClawLauncher;
            case CUB: return CharacterType.Cub;
            case HERMIT: return CharacterType.Hermit;
            case ILLUSIONIST: return CharacterType.Illusionist;
            case JAILER: return CharacterType.Jailer;
            case LEADER_KING: return CharacterType.LeaderKing;
            case LEADER_QUEEN: return CharacterType.LeaderQueen;
            case MANIPULATOR: return CharacterType.Manipulator;
            case NEMESIS: return CharacterType.Nemesis;
            case PROTECTOR: return CharacterType.Protector;
            case RIDER: return CharacterType.Rider;
            case ROYAL_GUARD: return CharacterType.RoyalGuard;
            case VIZIER: return CharacterType.Vizier;
            case WANDERER: return CharacterType.Wanderer;
            default: throw new IllegalStateException("Export not handled for character type: " + this);
        }
    }

    public static ExportCharacterType getFromCharacterType(@NonNull CharacterType characterType) {
        switch (characterType) {
            case Acrobat: return ACROBAT;
            case Archer: return ARCHER;
            case Assassin: return ASSASSIN;
            case Brewmaster: return BREWMASTER;
            case Bruiser: return BRUISER;
            case ClawLauncher: return CLAW_LAUNCHER;
            case Cub: return CUB;
            case Hermit: return HERMIT;
            case Illusionist: return ILLUSIONIST;
            case Jailer: return JAILER;
            case LeaderKing: return LEADER_KING;
            case LeaderQueen: return LEADER_QUEEN;
            case Manipulator: return MANIPULATOR;
            case Nemesis: return NEMESIS;
            case Protector: return PROTECTOR;
            case Rider: return RIDER;
            case RoyalGuard: return ROYAL_GUARD;
            case Vizier: return VIZIER;
            case Wanderer: return WANDERER;
            default: throw new IllegalStateException("Character type not handled for export: " + characterType);
        }
    }
}
