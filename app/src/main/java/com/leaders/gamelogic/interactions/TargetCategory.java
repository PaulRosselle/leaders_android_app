package com.leaders.gamelogic.interactions;

import androidx.annotation.NonNull;

public enum TargetCategory {
    PlayableCharacter, // target a PlayableCharacter
    RecruitmentCard, // target a SelectableCharacterCard
    BanishmentCard, // target a SelectableCharacterCard
    RecruitmentDestination, // target a Position
    MovementDestination, // target a Position
    ActiveAbilityDestination, // target a Position
    ActiveAbilityTargetPosition; // target a Position

    /**
     * Returns the interaction result type associated with this target category.
     *
     * @return the interaction result type corresponding to this target category
     * @throws IllegalArgumentException if this target category has no associated result type
     */
    public InteractionResultType getResultType() {
        switch (this) {
            case PlayableCharacter:
                return InteractionResultType.PlayableCharacterChosen;

            case RecruitmentCard:
            case BanishmentCard:
                return InteractionResultType.SelectableCharacterCardChosen;

            case RecruitmentDestination:
            case MovementDestination:
            case ActiveAbilityDestination:
            case ActiveAbilityTargetPosition:
                return InteractionResultType.PositionChosen;

            default:
                throw new IllegalArgumentException("Cannot find a result type matching target category:" + this);
        }
    }
}