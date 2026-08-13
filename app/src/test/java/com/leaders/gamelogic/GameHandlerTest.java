package com.leaders.gamelogic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameHandlerTest {

    @Test
    public void constructor_createsGameHandler() {
        GameHistory history = createGameHistory();
        IGameFlowListener listener = createListener();

        GameHandler gameHandler = new GameHandler(history, listener);

        assertNotNull(gameHandler);
    }

    @Test
    public void getCurrentGame_returnsCurrentGame() {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, createListener());

        Game game = gameHandler.getCurrentGame();

        assertNotNull(game);
    }

    @Test
    public void getCurrentHistory_returnsCurrentHistory() {
        GameHistory history = createGameHistory();
        GameHandler gameHandler = new GameHandler(history, createListener());

        assertSame(history, gameHandler.getCurrentHistory());
    }

    @Test
    public void getGameMode_returnsCurrentGameMode() {
        GameMode expectedGameMode = GameMode.Strategist;
        GameHistory history = createGameHistory(expectedGameMode);
        GameHandler gameHandler = new GameHandler(history, createListener());

        assertSame(expectedGameMode, gameHandler.getGameMode());
    }

    @Test
    public void getPlayers_returnsCurrentPlayers() {
        List<Player> expectedPlayers = Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
        GameHistory history = createGameHistory(GameMode.Discovery, expectedPlayers);
        GameHandler gameHandler = new GameHandler(history, createListener());

        List<Player> actualPlayers = gameHandler.getPlayers();

        assertNotSame(expectedPlayers, actualPlayers);
        assertSame(expectedPlayers.size(), actualPlayers.size());

        for (int playerIdx = 0; playerIdx < expectedPlayers.size(); playerIdx++) {
            assertSame(expectedPlayers.get(playerIdx), actualPlayers.get(playerIdx));
        }
    }

    private GameHistory createGameHistory() {
        return createGameHistory(GameMode.Discovery, createPlayers());
    }

    private GameHistory createGameHistory(GameMode gameMode) {
        return createGameHistory(gameMode, createPlayers());
    }

    private GameHistory createGameHistory(GameMode gameMode, List<Player> players) {
        GameConfig config = new GameConfig(
                players,
                players.get(0),
                gameMode,
                Collections.emptyList(),
                Collections.emptyList()
        );
        return new GameHistory(config, new ArrayList<>());
    }

    private List<Player> createPlayers() {
        return Arrays.asList(
                new Player(TeamColor.Black, "Black Player"),
                new Player(TeamColor.White, "White Player")
        );
    }

    private IGameFlowListener createListener() {
        return new IGameFlowListener() {
            @NonNull
            @Override
            public CompletableFuture<Void> onGameStarted(@NonNull Game game) {
                return CompletableFuture.completedFuture(null);
            }

            @NonNull
            @Override
            public CompletableFuture<Void> onGameEnded(@NonNull Player winner) {
                return CompletableFuture.completedFuture(null);
            }

            @NonNull
            @Override
            public CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase) {
                return CompletableFuture.completedFuture(null);
            }

            @NonNull
            @Override
            public CompletableFuture<InteractionResult> onInputRequired(
                    @NonNull InteractionRequest request) {
                return CompletableFuture.completedFuture(null);
            }

            @NonNull
            @Override
            public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}