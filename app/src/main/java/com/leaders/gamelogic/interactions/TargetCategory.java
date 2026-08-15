package com.leaders.gamelogic.interactions;

public enum TargetCategory {
    PlayableCharacter, // target a PlayableCharacter
    RecruitmentCard, // target a SelectableCharacterCard
    BanishmentCard, // target a SelectableCharacterCard
    RecruitmentDestination, // target a Position
    MovementDestination, // target a Position
    ActiveAbilityDestination, // target a Position
    ActiveAbilityTargetPosition // target a Position
}