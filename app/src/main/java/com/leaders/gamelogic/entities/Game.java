package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Game {
    @NonNull
    private final Board board;
    @NonNull
    private final List<CharacterCard> recruitableCards;
    @NonNull
    private final EnumMap<TeamColor, EnumMap<CharacterType, Integer>> playerRecruitedCharacters;
    @NonNull
    private final EnumMap<TeamColor, List<CharacterCard>> playerBanishedCards;
    @NonNull
    private final EnumMap<TeamColor, EnumMap<WarningType, Integer>> playerWarnings;
    @NonNull
    private final HashMap<UUID, TransformedState> charactersTransformedState;

    public Game(@NonNull Board board, @NonNull List<CharacterCard> recruitableCards,
                @NonNull EnumMap<TeamColor, EnumMap<CharacterType, Integer>> playerRecruitedCharacters,
                @NonNull EnumMap<TeamColor, List<CharacterCard>> playerBanishedCards,
                @NonNull EnumMap<TeamColor, EnumMap<WarningType, Integer>> playerWarnings,
                @NonNull HashMap<UUID, TransformedState> charactersTransformedState) {
        this.board = board;
        this.recruitableCards = recruitableCards;
        this.playerRecruitedCharacters = playerRecruitedCharacters;
        this.playerBanishedCards = playerBanishedCards;
        this.playerWarnings = playerWarnings;
        this.charactersTransformedState = charactersTransformedState;
    }

    public Game(@NonNull Game refGame) {
        this(new Board(refGame.board),
                new ArrayList<>(refGame.recruitableCards),
                copyRecruitedCharacters(refGame.playerRecruitedCharacters),
                copyBannedCards(refGame.playerBanishedCards),
                copyWarnings(refGame.playerWarnings),
                copyTransformedState(refGame.charactersTransformedState)
        );
    }

    /**
     * Creates a deep copy of the recruited characters map: each nested map is duplicated.
     * Integer values are immutable, so they can be shared safely.
     *
     * @param source the map to copy
     * @return an independent copy of the map
     */
    @NonNull
    private static EnumMap<TeamColor, EnumMap<CharacterType, Integer>> copyRecruitedCharacters(@NonNull EnumMap<TeamColor, EnumMap<CharacterType, Integer>> source) {
        EnumMap<TeamColor, EnumMap<CharacterType, Integer>> copy = new EnumMap<>(TeamColor.class);
        for (EnumMap.Entry<TeamColor, EnumMap<CharacterType, Integer>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Creates a deep copy of a map of lists: the map and every list it contains are duplicated.
     *
     * <p>{@link CharacterCard} is an enum, so its elements are immutable and shared safely.</p>
     *
     * @param source the map to copy
     * @return an independent copy of the map
     */
    @NonNull
    private static EnumMap<TeamColor, List<CharacterCard>> copyBannedCards(@NonNull EnumMap<TeamColor, List<CharacterCard>> source) {
        EnumMap<TeamColor, List<CharacterCard>> copy = new EnumMap<>(TeamColor.class);
        for (EnumMap.Entry<TeamColor, List<CharacterCard>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Creates a deep copy of the warnings map: each nested map is duplicated.
     * Integer values are immutable, so they can be shared safely.
     */
    @NonNull
    private static EnumMap<TeamColor, EnumMap<WarningType, Integer>> copyWarnings(@NonNull EnumMap<TeamColor, EnumMap<WarningType, Integer>> source) {
        EnumMap<TeamColor, EnumMap<WarningType, Integer>> copy = new EnumMap<>(TeamColor.class);
        for (EnumMap.Entry<TeamColor, EnumMap<WarningType, Integer>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Creates a copy of the transformed states map.
     *
     * <p>{@link TransformedState} is immutable, so its instances can be shared safely:
     * copying the map itself is enough to obtain an independent copy.</p>
     *
     * @param source the map to copy
     * @return an independent copy of the map
     */
    @NonNull
    private static HashMap<UUID, TransformedState> copyTransformedState(@NonNull HashMap<UUID, TransformedState> source) {
        // TransformedState is an immutable object, a simple hashMap copy is enough
        return new HashMap<>(source);
    }

    @NonNull
    public Board getBoard() {
        return board;
    }

    @NonNull
    public List<CharacterCard> getRecruitableCards() {
        return recruitableCards;
    }

    @NonNull
    public List<CharacterCard> getRecruitedCards(@NonNull TeamColor team) {
        EnumMap<CharacterType, Integer> counts = playerRecruitedCharacters.get(team);
        if (counts == null) {
            return List.of();
        }

        List<CharacterCard> cards = new ArrayList<>();
        for (CharacterCard card : CharacterCard.values()) {
            if (!card.canBeRecruited())  {
                continue;
            }

            int copies = 0;
            for (CharacterType type : CharacterType.getCharacterTypesMatchingCard(card)) {
                copies = Math.max(copies, Objects.requireNonNullElse(counts.get(type), 0));
            }
            for (int i = 0; i < copies; i++) cards.add(card);
        }
        return Collections.unmodifiableList(cards);
    }

    public void addRecruitedCharacter(@NonNull TeamColor team, @NonNull CharacterType type) {
        playerRecruitedCharacters
                .computeIfAbsent(team, k -> new EnumMap<>(CharacterType.class))
                .merge(type, 1, Integer::sum);
    }

    public void removeRecruitedCharacter(@NonNull TeamColor team, @NonNull CharacterType type) {
        EnumMap<CharacterType, Integer> counts = playerRecruitedCharacters.get(team);
        if (counts == null) {
            return;
        }
        counts.computeIfPresent(type, (characterType, characterCount) ->
                characterCount > 1 ? characterCount - 1 : null
        );
        if (counts.isEmpty()) {
            playerRecruitedCharacters.remove(team);
        }
    }

    @NonNull
    public List<CharacterCard> getBanishedCards(@NonNull TeamColor playerTeamColor) {
        List<CharacterCard> characterCards = playerBanishedCards.get(playerTeamColor);
        if (characterCards == null) {
            return List.of();
        }
        return Collections.unmodifiableList(characterCards);
    }

    public void addBanishedCard(@NonNull TeamColor playerTeamColor, @NonNull CharacterCard characterCard) {
        playerBanishedCards.computeIfAbsent(playerTeamColor, key -> new ArrayList<>())
                .add(characterCard);
    }

    public void removeBanishedCard(@NonNull TeamColor playerTeamColor, @NonNull CharacterCard characterCard) {
        List<CharacterCard> characterCards = playerBanishedCards.get(playerTeamColor);
        if (characterCards != null) {
            characterCards.remove(characterCard);
        }
    }

    /**
     * Returns the current number of warnings of the specified type for a player team.
     *
     * <p>The absence of an entry in the underlying map represents a warning count of zero.
     * This avoids storing unnecessary zero values and keeps the projection compact.</p>
     *
     * @param playerTeamColor the team whose warnings must be retrieved
     * @param warningType the type of warning to retrieve
     * @return the current warning count, or {@code 0} if no warning of this type is active
     */
    public int getPlayerWarningCount(@NonNull TeamColor playerTeamColor, @NonNull WarningType warningType) {
        EnumMap<WarningType, Integer> teamWarnings = playerWarnings.get(playerTeamColor);
        // A missing team entry means that this team currently has no active warnings.
        if (teamWarnings == null) {
            return 0;
        }
        // Java collections cannot store primitive int values. A null Integer is treated as
        // an absent warning count and therefore interpreted as zero.
        Integer count = teamWarnings.get(warningType);
        return count == null ? 0 : count;
    }

    /**
     * Updates the current number of warnings of the specified type for a player team.
     *
     * <p>A warning count of zero is represented by the absence of an entry in the underlying
     * map. Therefore, setting a warning count to zero removes the corresponding warning
     * instead of storing a zero value.</p>
     *
     * <p>This method maintains the following invariant:
     * <ul>
     *     <li>no warning type is stored with a count of zero;</li>
     *     <li>no team is stored if it has no active warnings.</li>
     * </ul>
     * </p>
     *
     * @param playerTeamColor the team whose warnings must be updated
     * @param warningType the type of warning to update
     * @param warningCount the new warning count, must be greater than or equal to zero
     * @throws IllegalArgumentException if {@code warningCount} is negative
     */
    public void setPlayerWarningCount(@NonNull TeamColor playerTeamColor, @NonNull WarningType warningType, int warningCount) {
        if (warningCount < 0) {
            throw new IllegalArgumentException("Warning count cannot be negative");
        }

        // A zero warning count is represented by the absence of an entry in the map.
        // Removing empty nested maps also avoids keeping teams without active warnings.
        if (warningCount == 0) {
            EnumMap<WarningType, Integer> teamWarnings = playerWarnings.get(playerTeamColor);
            if (teamWarnings != null) {
                teamWarnings.remove(warningType);
                if (teamWarnings.isEmpty()) {
                    playerWarnings.remove(playerTeamColor);
                }
            }
            return;
        }
        // Creates the team's warning map only when the first warning of this team is added.
        // computeIfAbsent avoids creating an unused EnumMap when the team already exists.
        playerWarnings.computeIfAbsent(playerTeamColor, key -> new EnumMap<>(WarningType.class))
                .put(warningType, warningCount);
    }

    /**
     * Returns the transformed state of a character.
     *
     * @param characterId the identifier of the character
     * @return the transformed state, or {@code null} if the character is not transformed
     */
    @Nullable
    public TransformedState getTransformedState(@NonNull UUID characterId) {
        return charactersTransformedState.get(characterId);
    }

    /**
     * Records that a character has been transformed.
     *
     * @param characterBefore the character before its transformation
     * @param characterAfter  the character after its transformation
     */
    public void setTransformedState(@NonNull Character characterBefore, @NonNull Character characterAfter) {
        charactersTransformedState.put(
                characterAfter.getId(),
                new TransformedState(characterBefore, characterAfter)
        );
    }

    /**
     * Returns the identifiers of all transformed characters.
     *
     * @return an unmodifiable view of the identifiers
     */
    @NonNull
    public Set<UUID> getTransformedCharacterIds() {
        return Collections.unmodifiableSet(charactersTransformedState.keySet());
    }
}
