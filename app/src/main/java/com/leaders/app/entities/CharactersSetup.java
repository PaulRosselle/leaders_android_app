package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.CharacterExpansion;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CharactersSetup {
    public final static int CHARACTERS_START_COUNT = 16;

    @NonNull
    private final Map<CharacterCard, AltCharacterSelection> altCharacterSelections;
    @NonNull
    private final Set<CharacterCard> bannedCharacters;

    public CharactersSetup(@NonNull Set<AltCharacterSelection> existingAltSelections,
                           @NonNull Set<CharacterCard> bannedCharacters) {
        this.altCharacterSelections = new EnumMap<>(CharacterCard.class);
        this.bannedCharacters = new HashSet<>(bannedCharacters);

        initAltSelections(existingAltSelections);
    }

    private void initAltSelections(@NonNull Set<AltCharacterSelection> existingAltSelections) {
        for (AltCharacterSelection existingAltSelection : existingAltSelections) {
            this.altCharacterSelections.put(existingAltSelection.getCharacter(), existingAltSelection);
        }

        for (CharacterCard card : CharacterCard.values()) {
            if (altCharacterSelections.containsKey(card)) {
                continue;
            }

            CharacterCard baseCard = CharacterCardUtils.getBaseCharacter(card);
            if (baseCard != null) {
                altCharacterSelections.put(baseCard, new AltCharacterSelection(baseCard, null));
            }
        }
    }

    public List<CharacterCard> getRemainingCharacters() {
        List<CharacterCard> remainingCards = new ArrayList<>();

        for (CharacterCard card : CharacterCard.values()) {
            if (canSetupCard(card)) {
                remainingCards.add(getSelectedCard(card));
            }
        }

        return remainingCards;
    }

    public List<CharacterCard> getCharactersInExpansion(@NonNull CharacterExpansion expansion) {
        return getRemainingCharacters().stream()
                .filter(card -> CharacterExpansion.getFromCard(card) == expansion)
                .collect(Collectors.toList());
    }

    @NonNull
    private CharacterCard getSelectedCard(@NonNull CharacterCard card) {
        AltCharacterSelection altCharacterSelection = altCharacterSelections.get(card);

        if (altCharacterSelection != null && altCharacterSelection.getSelectedAlternative() != null) {
            return altCharacterSelection.getSelectedAlternative();
        }

        return card;
    }

    public boolean canSetupCard(@NonNull CharacterCard card) {
        // Cards that cannot be recruited and alternative versions cannot be setup directly
        return card.canBeRecruited() && !CharacterCardUtils.isAlternativeCharacter(card);
    }

    @NonNull
    private AltCharacterSelection getNonNullAltSelection(@NonNull CharacterCard altCharacter) {
        return Objects.requireNonNull(
                altCharacterSelections.get(CharacterCardUtils.getBaseCharacter(altCharacter)),
                "No alternative character selection found for " + altCharacter
        );
    }

    public void selectAlt(@NonNull CharacterCard altCharacter) {
        getNonNullAltSelection(altCharacter).setSelectedAlternative(altCharacter);
    }

    public void unselectAlt(@NonNull CharacterCard altCharacter) {
        getNonNullAltSelection(altCharacter).setSelectedAlternative(null);
    }

    public void addBannedCard(@NonNull CharacterCard card) {
        bannedCharacters.add(card);
    }

    public void removeBannedCard(@NonNull CharacterCard card) {
        bannedCharacters.remove(card);
    }

    @NonNull
    public Set<CharacterCard> getBannedCharacters() {
        return Set.copyOf(bannedCharacters);
    }
}
