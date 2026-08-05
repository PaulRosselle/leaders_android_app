package com.leaders.gamelogic.resolvers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.interactions.CharacterActionBuilder;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.queries.BoardQuery;

public abstract class CharacterActionResolver {
    @NonNull
    protected final Game game;
    @NonNull
    protected final GameHistory gameHistory;

    @NonNull
    protected final Character character;

    @NonNull
    protected final Cell characterCell;

    public CharacterActionResolver(@NonNull Game game, @NonNull GameHistory gameHistory, @NonNull Character character) {
        this.game = game;
        this.gameHistory = gameHistory;
        this.character = character;
        this.characterCell = BoardQuery.getCellByCharacterId(game.getBoard(), character.getId());
    }

    @Nullable
    public abstract InteractionRequest getNextInteraction(@NonNull CharacterActionBuilder characterActionBuilder);

    @NonNull
    public abstract CharacterAction buildAction(@NonNull CharacterActionBuilder characterActionBuilder);
}
