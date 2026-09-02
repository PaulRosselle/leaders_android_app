package com.leaders.gamelogic.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.PhaseTransitionQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.gamelogic.queries.SelectableCardsQuery;

import java.util.List;

public final class GameContext {
    @NonNull
    private final GamePhase gamePhase;
    @NonNull
    private final Player currentPlayer;
    @NonNull
    private final Player opposingPlayer;
    @NonNull
    private final GameMode gameMode;
    @NonNull
    private final Board board;

    @NonNull
    private final List<SelectableCharacterCard> availableCharacterCards;

    @Nullable
    private final List<PlayableCharacter> playableCharacters;

    public GameContext(@NonNull GamePhase gamePhase,
                       @NonNull Player currentPlayer, @NonNull Player opposingPlayer,
                       @NonNull GameMode gameMode, @NonNull Board board,
                       @NonNull List<SelectableCharacterCard> availableCharacterCards,
                       @Nullable List<PlayableCharacter> playableCharacters) {
        this.gamePhase = gamePhase;
        this.currentPlayer = new Player(currentPlayer);
        this.opposingPlayer = new Player(opposingPlayer);
        this.gameMode = gameMode;
        this.board = new Board(board);
        this.availableCharacterCards = List.copyOf(availableCharacterCards);
        this.playableCharacters = playableCharacters != null ? List.copyOf(playableCharacters) : null;
    }

    @NonNull
    public GamePhase getGamePhase() {
        return gamePhase;
    }

    @NonNull
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @NonNull
    public Player getOpposingPlayer() {
        return opposingPlayer;
    }

    @NonNull
    public GameMode getGameMode() {
        return gameMode;
    }

    @NonNull
    public List<SelectableCharacterCard> getAvailableCharacterCards() {
        return availableCharacterCards;
    }

    @Nullable
    public List<PlayableCharacter> getPlayableCharacters() {
        return playableCharacters;
    }

    @NonNull
    public Board getBoard() {
        return board;
    }

    private static GamePhase getCurrentContextPhase(@NonNull Game game, @NonNull GameHistory gameHistory) {
        if (gameHistory.getEntries().isEmpty()) {
            return PhaseTransitionQuery.getNextPhase(game, gameHistory);
        }

        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(gameHistory);
        if (currentPhase != null) {
            return new GamePhase(
                    GamePhaseType.getFromTransitionTarget(GameHistoryQuery.getPhaseTransitionTarget(currentPhase)),
                    GameHistoryQuery.getPlayerFromTeam( gameHistory, GameHistoryQuery.getPhaseTeamColor(currentPhase))
            );
        }

        IPhase lastEndedPhase = GameHistoryQuery.findLastEndedPhase(gameHistory);
        if (lastEndedPhase == null) {
            throw new IllegalStateException("A non empty history should always a current or ended phase");
        }

        return new GamePhase(
                GamePhaseType.getFromTransitionTarget(GameHistoryQuery.getPhaseTransitionTarget(lastEndedPhase)),
                GameHistoryQuery.getPlayerFromTeam( gameHistory, GameHistoryQuery.getPhaseTeamColor(lastEndedPhase))
        );
    }

    @Nullable
    private static TeamColor playableCharactersTeamColor(@NonNull List<PlayableCharacter> playableCharacters) {
        if (playableCharacters.isEmpty()) {
            return null;
        }

        return playableCharacters.get(0).getCharacter().getTeamColor();
    }

    @NonNull
    public static GameContext createCurrent(@NonNull Game game, @NonNull GameHistory gameHistory) {
        GamePhase currentGamePhase = getCurrentContextPhase(game, gameHistory);

        Player currentPlayer = currentGamePhase.getPhasePlayer();

        List<PlayableCharacter> playableCharacters = null;
        if (currentGamePhase.getPhaseType() == GamePhaseType.Actions) {
            playableCharacters = PlayabilityQuery.getPlayableCharacters(game, gameHistory);
            TeamColor playableCharacterColor = playableCharactersTeamColor(playableCharacters);
            if (playableCharacterColor != null) {
                currentPlayer = GameHistoryQuery.getPlayerFromTeam(gameHistory, playableCharacterColor);
            }
        }

        Player opposingPlayer = GameHistoryQuery.getPlayerFromTeam(
                gameHistory, currentPlayer.getTeamColor().getOpposite()
        );

        return new GameContext(currentGamePhase,
                currentPlayer,
                opposingPlayer,
                gameHistory.getConfig().getGameMode(),
                game.getBoard(),
                SelectableCardsQuery.getSelectableCards(game, gameHistory),
                playableCharacters
        );
    }
}
