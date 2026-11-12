package com.leaders.gamelogic.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.WarningType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameTest {
    private static final TeamColor TEAM = TeamColor.Black;

    private Game createTestGame() {
        // Minimal Game state required by the tests.
        // This state is intentionally invalid as a real game state.
        return new Game(new Board(),
                new ArrayList<>(), // recruitableCards
                new EnumMap<>(TeamColor.class), // playerRecruitedCharacters
                new EnumMap<>(TeamColor.class), // playerBanishedCards
                new EnumMap<>(TeamColor.class), // playerWarnings
                new HashMap<>() // charactersTransformedState
        );
    }

    private static TeamColor otherTeam() {
        for (TeamColor color : TeamColor.values()) {
            if (color != TEAM) {
                return color;
            }
        }
        throw new IllegalStateException("At least two team colors are required");
    }

    /** Returns a card that can be recruited, so that recruited-card assertions are meaningful. */
    private static CharacterCard recruitableCard() {
        for (CharacterCard card : CharacterCard.values()) {
            if (card.canBeRecruited()) {
                return card;
            }
        }
        throw new IllegalStateException("No recruitable card found");
    }

    /** Returns a character type matching a recruitable card. */
    private static CharacterType recruitableType() {
        return CharacterType.getCharacterTypesMatchingCard(recruitableCard()).get(0);
    }

    /**
     * With a null argument, creates a brand new character (new id).
     * Otherwise, transforms the given character: the result has the same id.
     */
    @NonNull
    private static Character createCharacter(@Nullable Character character) {
        if (character == null) {
            return Character.create(CharacterType.Nemesis, TeamColor.Black);
        }
        return Character.transform(character, CharacterType.Frog, TeamColor.Black);
    }

    /** Registers a new transformed character in the game and returns its id. */
    @NonNull
    private static UUID addTransformedCharacter(@NonNull Game game) {
        Character before = createCharacter(null);
        Character after = createCharacter(before);
        game.addTransformedState(before, after);
        return before.getId();
    }

    // ------------------------------------------------------------------
    // Copy constructor: independence (populated before copy)
    // ------------------------------------------------------------------

    @Test
    public void copy_shouldCreateIndependentBoard() {
        Game original = createTestGame();

        Game copy = new Game(original);

        assertNotSame(original.getBoard(), copy.getBoard());
    }

    @Test
    public void copy_shouldNotShareRecruitableCards() {
        Game original = createTestGame();
        original.getRecruitableCards().add(CharacterCard.values()[0]);
        Game copy = new Game(original);

        original.getRecruitableCards().clear();
        assertEquals(List.of(CharacterCard.values()[0]), copy.getRecruitableCards());

        copy.getRecruitableCards().add(CharacterCard.values()[0]);
        assertTrue(original.getRecruitableCards().isEmpty());
    }

    @Test
    public void copy_shouldPreserveRecruitedCards() {
        Game original = createTestGame();
        original.addRecruitedCharacter(TEAM, recruitableType());

        Game copy = new Game(original);

        assertEquals(original.getRecruitedCards(TEAM), copy.getRecruitedCards(TEAM));
        assertEquals(1, copy.getRecruitedCards(TEAM).size());
    }

    @Test
    public void copy_shouldNotShareRecruitedCards_whenOriginalIsModified() {
        Game original = createTestGame();
        CharacterType type = recruitableType();
        original.addRecruitedCharacter(TEAM, type);
        Game copy = new Game(original);

        original.addRecruitedCharacter(TEAM, type);
        original.addRecruitedCharacter(otherTeam(), type);

        assertEquals(1, copy.getRecruitedCards(TEAM).size());
        assertTrue(copy.getRecruitedCards(otherTeam()).isEmpty());
    }

    @Test
    public void copy_shouldNotShareRecruitedCards_whenCopyIsModified() {
        Game original = createTestGame();
        CharacterType type = recruitableType();
        original.addRecruitedCharacter(TEAM, type);
        Game copy = new Game(original);

        copy.removeRecruitedCharacter(TEAM, type);

        assertTrue(copy.getRecruitedCards(TEAM).isEmpty());
        assertEquals(1, original.getRecruitedCards(TEAM).size());
    }

    @Test
    public void copy_shouldPreserveAndNotShareBanishedCards() {
        Game original = createTestGame();
        CharacterCard card = CharacterCard.values()[0];
        original.addBanishedCard(TEAM, card);
        Game copy = new Game(original);

        assertEquals(List.of(card), copy.getBanishedCards(TEAM));

        original.addBanishedCard(TEAM, CharacterCard.values()[1]);
        assertEquals(List.of(card), copy.getBanishedCards(TEAM));

        copy.removeBanishedCard(TEAM, card);
        assertEquals(List.of(card, CharacterCard.values()[1]), original.getBanishedCards(TEAM));
    }

    @Test
    public void copy_shouldPreserveAndNotShareWarnings() {
        Game original = createTestGame();
        WarningType type = WarningType.values()[0];
        original.setPlayerWarningCount(TEAM, type, 2);
        Game copy = new Game(original);

        assertEquals(2, copy.getPlayerWarningCount(TEAM, type));

        original.setPlayerWarningCount(TEAM, type, 3);
        assertEquals(2, copy.getPlayerWarningCount(TEAM, type));

        copy.setPlayerWarningCount(TEAM, type, 0);
        assertEquals(3, original.getPlayerWarningCount(TEAM, type));
    }

    @Test
    public void copy_shouldPreserveAndNotShareTransformedStates() {
        Game original = createTestGame();
        UUID id = addTransformedCharacter(original);
        TransformedState state = original.getTransformedState(id);
        Game copy = new Game(original);

        // Immutable values can be shared between the maps
        assertSame(state, copy.getTransformedState(id));

        // New entry in the original is not visible in the copy
        UUID otherId = addTransformedCharacter(original);
        assertNull(copy.getTransformedState(otherId));
        assertEquals(Set.of(id), copy.getTransformedCharacterIds());

        // Entry added in the copy is not visible in the original
        UUID thirdId = addTransformedCharacter(copy);
        assertNull(original.getTransformedState(thirdId));
    }

    // ------------------------------------------------------------------
    // Recruited characters
    // ------------------------------------------------------------------

    @Test
    public void getRecruitedCards_unknownTeam_returnsEmptyList() {
        assertTrue(createTestGame().getRecruitedCards(TEAM).isEmpty());
    }

    @Test
    public void addRecruitedCharacter_twice_returnsTwoCards() {
        Game game = createTestGame();
        CharacterType type = recruitableType();

        game.addRecruitedCharacter(TEAM, type);
        game.addRecruitedCharacter(TEAM, type);

        assertEquals(2, game.getRecruitedCards(TEAM).size());
        assertEquals(recruitableCard(), game.getRecruitedCards(TEAM).get(0));
    }

    @Test
    public void addRecruitedCharacter_isPerTeam() {
        Game game = createTestGame();

        game.addRecruitedCharacter(TEAM, recruitableType());

        assertEquals(1, game.getRecruitedCards(TEAM).size());
        assertTrue(game.getRecruitedCards(otherTeam()).isEmpty());
    }

    @Test
    public void removeRecruitedCharacter_decrementsCount() {
        Game game = createTestGame();
        CharacterType type = recruitableType();
        game.addRecruitedCharacter(TEAM, type);
        game.addRecruitedCharacter(TEAM, type);

        game.removeRecruitedCharacter(TEAM, type);

        assertEquals(1, game.getRecruitedCards(TEAM).size());
    }

    @Test
    public void removeRecruitedCharacter_lastCopy_emptiesTeam() {
        Game game = createTestGame();
        CharacterType type = recruitableType();
        game.addRecruitedCharacter(TEAM, type);

        game.removeRecruitedCharacter(TEAM, type);

        assertTrue(game.getRecruitedCards(TEAM).isEmpty());
    }

    @Test
    public void removeRecruitedCharacter_unknownTeam_doesNothing() {
        Game game = createTestGame();

        game.removeRecruitedCharacter(TEAM, recruitableType());

        assertTrue(game.getRecruitedCards(TEAM).isEmpty());
    }

    @Test
    public void removeRecruitedCharacter_notRecruitedType_doesNotAffectOthers() {
        Game game = createTestGame();
        CharacterType type = recruitableType();
        game.addRecruitedCharacter(TEAM, type);

        for (CharacterType other : CharacterType.values()) {
            if (other != type) {
                game.removeRecruitedCharacter(TEAM, other);
            }
        }

        assertEquals(1, game.getRecruitedCards(TEAM).size());
    }

    @Test
    public void getRecruitedCards_returnsUnmodifiableList() {
        Game game = createTestGame();
        game.addRecruitedCharacter(TEAM, recruitableType());

        List<CharacterCard> cards = game.getRecruitedCards(TEAM);

        assertThrows(UnsupportedOperationException.class, () -> cards.add(recruitableCard()));
    }

    // ------------------------------------------------------------------
    // Banished cards
    // ------------------------------------------------------------------

    @Test
    public void getBanishedCards_unknownTeam_returnsEmptyList() {
        assertTrue(createTestGame().getBanishedCards(TEAM).isEmpty());
    }

    @Test
    public void addBanishedCard_isPerTeam() {
        Game game = createTestGame();
        CharacterCard card = CharacterCard.values()[0];

        game.addBanishedCard(TEAM, card);

        assertEquals(List.of(card), game.getBanishedCards(TEAM));
        assertTrue(game.getBanishedCards(otherTeam()).isEmpty());
    }

    @Test
    public void removeBanishedCard_removesOnlyOneOccurrence() {
        Game game = createTestGame();
        CharacterCard card = CharacterCard.values()[0];
        game.addBanishedCard(TEAM, card);
        game.addBanishedCard(TEAM, card);

        game.removeBanishedCard(TEAM, card);

        assertEquals(List.of(card), game.getBanishedCards(TEAM));
    }

    @Test
    public void removeBanishedCard_absentCard_doesNothing() {
        Game game = createTestGame();
        game.addBanishedCard(TEAM, CharacterCard.values()[0]);

        game.removeBanishedCard(TEAM, CharacterCard.values()[1]);

        assertEquals(List.of(CharacterCard.values()[0]), game.getBanishedCards(TEAM));
    }

    @Test
    public void removeBanishedCard_unknownTeam_doesNothing() {
        Game game = createTestGame();

        game.removeBanishedCard(TEAM, CharacterCard.values()[0]);

        assertTrue(game.getBanishedCards(TEAM).isEmpty());
    }

    @Test
    public void getBanishedCards_returnsUnmodifiableList() {
        Game game = createTestGame();
        game.addBanishedCard(TEAM, CharacterCard.values()[0]);

        List<CharacterCard> cards = game.getBanishedCards(TEAM);

        assertThrows(UnsupportedOperationException.class, () -> cards.add(CharacterCard.values()[1]));
    }

    // ------------------------------------------------------------------
    // Warnings
    // ------------------------------------------------------------------

    @Test
    public void getPlayerWarningCount_default_isZero() {
        assertEquals(0, createTestGame().getPlayerWarningCount(TEAM, WarningType.values()[0]));
    }

    @Test
    public void setPlayerWarningCount_storesValue() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];

        game.setPlayerWarningCount(TEAM, type, 2);

        assertEquals(2, game.getPlayerWarningCount(TEAM, type));
    }

    @Test
    public void setPlayerWarningCount_overwritesPreviousValue() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];
        game.setPlayerWarningCount(TEAM, type, 2);

        game.setPlayerWarningCount(TEAM, type, 5);

        assertEquals(5, game.getPlayerWarningCount(TEAM, type));
    }

    @Test
    public void setPlayerWarningCount_zero_resetsToZero() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];
        game.setPlayerWarningCount(TEAM, type, 2);

        game.setPlayerWarningCount(TEAM, type, 0);

        assertEquals(0, game.getPlayerWarningCount(TEAM, type));
    }

    @Test
    public void setPlayerWarningCount_zeroOnUnknownTeam_doesNothing() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];

        game.setPlayerWarningCount(TEAM, type, 0);

        assertEquals(0, game.getPlayerWarningCount(TEAM, type));
    }

    @Test
    public void setPlayerWarningCount_isPerTeam() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];

        game.setPlayerWarningCount(TEAM, type, 2);

        assertEquals(0, game.getPlayerWarningCount(otherTeam(), type));
    }

    @Test
    public void setPlayerWarningCount_negative_throwsIllegalArgumentException() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];

        assertThrows(IllegalArgumentException.class,
                () -> game.setPlayerWarningCount(TEAM, type, -1));
    }

    @Test
    public void setPlayerWarningCount_negative_doesNotModifyState() {
        Game game = createTestGame();
        WarningType type = WarningType.values()[0];
        game.setPlayerWarningCount(TEAM, type, 2);

        assertThrows(IllegalArgumentException.class,
                () -> game.setPlayerWarningCount(TEAM, type, -1));

        assertEquals(2, game.getPlayerWarningCount(TEAM, type));
    }

    // ------------------------------------------------------------------
    // Transformed states
    // ------------------------------------------------------------------

    @Test
    public void getTransformedState_unknownCharacter_returnsNull() {
        assertNull(createTestGame().getTransformedState(UUID.randomUUID()));
    }

    @Test
    public void addTransformedState_storesBeforeAndAfter() {
        Game game = createTestGame();
        Character before = createCharacter(null);
        Character after = createCharacter(before);

        game.addTransformedState(before, after);

        TransformedState state = game.getTransformedState(before.getId());
        assertNotNull(state);
        assertSame(before, state.getCharacterBefore());
        assertSame(after, state.getCharacterAfter());
    }

    @Test
    public void addTransformedState_differentIds_throwsIllegalArgumentException() {
        Game game = createTestGame();
        Character before = createCharacter(null);
        Character after = createCharacter(null);

        assertThrows(IllegalArgumentException.class, () -> game.addTransformedState(before, after));
        assertTrue(game.getTransformedCharacterIds().isEmpty());
    }

    @Test
    public void addTransformedState_alreadyTransformed_throwsIllegalStateException() {
        Game game = createTestGame();
        Character before = createCharacter(null);
        Character after = createCharacter(before);

        game.addTransformedState(before, after);

        assertThrows(IllegalStateException.class, () -> game.addTransformedState(before, after));

        assertEquals(1, game.getTransformedCharacterIds().size());
    }

    @Test
    public void removeTransformedState_existingCharacter_removesState() {
        Game game = createTestGame();
        UUID id = addTransformedCharacter(game);

        game.removeTransformedState(id);

        assertNull(game.getTransformedState(id));
        assertTrue(game.getTransformedCharacterIds().isEmpty());
    }

    @Test
    public void removeTransformedState_oneOfSeveral_removesOnlyRequestedState() {
        Game game = createTestGame();
        UUID firstId = addTransformedCharacter(game);
        UUID secondId = addTransformedCharacter(game);

        game.removeTransformedState(firstId);

        assertNull(game.getTransformedState(firstId));
        assertNotNull(game.getTransformedState(secondId));
        assertEquals(Set.of(secondId), game.getTransformedCharacterIds());
    }

    @Test
    public void removeTransformedState_removingTwiceThrowsIllegalStateException() {
        Game game = createTestGame();
        UUID id = addTransformedCharacter(game);

        game.removeTransformedState(id);

        assertNull(game.getTransformedState(id));
        assertTrue(game.getTransformedCharacterIds().isEmpty());

        assertThrows(IllegalStateException.class, () -> game.removeTransformedState(id));
    }

    @Test
    public void removeTransformedState_updatesPreviouslyObtainedIdsView() {
        Game game = createTestGame();
        UUID id = addTransformedCharacter(game);

        Set<UUID> ids = game.getTransformedCharacterIds();

        game.removeTransformedState(id);

        assertTrue(ids.isEmpty());
    }

    @Test
    public void removeTransformedState_unknownCharacter_throwsIllegalStateException() {
        Game game = createTestGame();
        UUID unknownId = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> game.removeTransformedState(unknownId));
        assertTrue(game.getTransformedCharacterIds().isEmpty());
    }

    @Test
    public void getTransformedCharacterIds_initially_isEmpty() {
        assertTrue(createTestGame().getTransformedCharacterIds().isEmpty());
    }

    @Test
    public void getTransformedCharacterIds_containsAllTransformedCharacters() {
        Game game = createTestGame();
        UUID first = addTransformedCharacter(game);
        UUID second = addTransformedCharacter(game);

        assertEquals(Set.of(first, second), game.getTransformedCharacterIds());
    }

    @Test
    public void getTransformedCharacterIds_isUnmodifiable() {
        Game game = createTestGame();
        UUID id = addTransformedCharacter(game);

        Set<UUID> ids = game.getTransformedCharacterIds();

        assertThrows(UnsupportedOperationException.class, () -> ids.add(UUID.randomUUID()));
        assertThrows(UnsupportedOperationException.class, () -> ids.remove(id));
        assertTrue(game.getTransformedCharacterIds().contains(id));
    }

    @Test
    public void getTransformedCharacterIds_reflectsLaterChanges() {
        // Documents that the returned set is a live view, not a snapshot
        Game game = createTestGame();
        Set<UUID> ids = game.getTransformedCharacterIds();

        UUID id = addTransformedCharacter(game);

        assertTrue(ids.contains(id));
    }
}