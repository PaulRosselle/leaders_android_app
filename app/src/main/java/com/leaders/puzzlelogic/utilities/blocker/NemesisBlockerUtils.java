package com.leaders.puzzlelogic.utilities.blocker;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.gamelogic.queries.GameQuery;
import com.leaders.gamelogic.queries.PlayabilityQuery;
import com.leaders.puzzlelogic.utilities.CharactersPermutationUtils;
import com.leaders.puzzlelogic.utilities.GameSearchUtils;

import java.util.List;

public class NemesisBlockerUtils {
    private NemesisBlockerUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean canBlock(@NonNull Game game,
                                   @NonNull GameHistory history,
                                   @NonNull CharacterAction nemesisAction,
                                   @NonNull List<Character> opponentCharacters,
                                   long permutationIndex) throws InterruptedException {
        IPhase phase = GameHistoryQuery.findCurrentPhase(history);

        if (!(phase instanceof ActionsPhase)) {
            throw new IllegalArgumentException("NemesisBlockerUtils requires an ActionsPhase");
        }

        ActionsPhase actionsPhase = (ActionsPhase) phase;

        List<Character> permutation = CharactersPermutationUtils.getPermutation(
                opponentCharacters, permutationIndex
        );

        GameActionHandler handler = GameActionHandlerFactory.create(game, nemesisAction);

        handler.doAction();
        actionsPhase.getActions().add(nemesisAction);

        try {
            return !canOpponentWin(game, history, actionsPhase, permutation);
        } finally {
            actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
            handler.undoAction();
        }
    }

    private static boolean canOpponentWin(@NonNull Game game,
                                          @NonNull GameHistory history,
                                          @NonNull ActionsPhase actionsPhase,
                                          @NonNull List<Character> remainingCharacters) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        TeamColor currentTeam = actionsPhase.getTurnTeamColor();
        TeamColor opponentTeam = currentTeam.getOpposite();

        // Before exploring new actions, we check if the game isn't already won
        TeamColor winnerTeam = GameQuery.getWinnerTeam(game, currentTeam);
        if (winnerTeam != null) {
            return winnerTeam == opponentTeam;
        }

        List<PlayableCharacter> playableCharacters = PlayabilityQuery.getPlayableCharacters(game, history);

        // MANDATORY CHARACTER : always has priority over the requested order.
        if (playableCharacters.size() == 1 && playableCharacters.get(0).isMandatory()) {
            PlayableCharacter playableCharacter = playableCharacters.get(0);
            Character character = playableCharacter.getCharacter();

            int characterIndex = remainingCharacters.indexOf(character);

            if (characterIndex != -1) {
                remainingCharacters.remove(characterIndex);
            }

            try {
                return exploreOpponentCharacterActions(
                        game,
                        history,
                        actionsPhase,
                        remainingCharacters,
                        playableCharacter
                );
            } finally {
                if (characterIndex != -1) {
                    remainingCharacters.add(characterIndex, character);
                }
            }
        }

        // PLAYABLE CHARACTER : we play the remaining characters in order.
        if (remainingCharacters.isEmpty()) {
            return false;
        }

        Character character = remainingCharacters.get(0);

        PlayableCharacter playableCharacter = GameSearchUtils.findPlayableCharacterById(
                playableCharacters, character
        );
        
        // This character is not playable in the current state.
        // Skip it in this permutation and continue with the next character.
        if (playableCharacter == null) {
            remainingCharacters.remove(0);
            try {
                return canOpponentWin(game, history, actionsPhase, remainingCharacters);
            } finally {
                remainingCharacters.add(0, character);
            }
        }

        remainingCharacters.remove(0);

        try {
            // If playing this character is not mandatory, we can try to skip its action
            if (!playableCharacter.isMandatory() &&
                    canOpponentWin(game, history, actionsPhase, remainingCharacters)) {
                return true;
            }

            return exploreOpponentCharacterActions(
                    game,
                    history,
                    actionsPhase,
                    remainingCharacters,
                    playableCharacter
            );
        } finally {
            remainingCharacters.add(0, character);
        }
    }

    private static boolean exploreOpponentCharacterActions(@NonNull Game game,
                                                           @NonNull GameHistory history,
                                                           @NonNull ActionsPhase actionsPhase,
                                                           @NonNull List<Character> remainingCharacters,
                                                           @NonNull PlayableCharacter playableCharacter) throws InterruptedException {
        List<CharacterAction> actions = GameSearchUtils.enumerateActions(
                game, history, playableCharacter.getCharacter()
        );

        for (CharacterAction action : actions) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            GameActionHandler handler = GameActionHandlerFactory.create(game, action);

            handler.doAction();
            actionsPhase.getActions().add(action);

            try {
                if (canOpponentWin(game, history, actionsPhase, remainingCharacters)) {
                    return true;
                }
            } finally {
                actionsPhase.getActions().remove(actionsPhase.getActions().size() - 1);
                handler.undoAction();
            }
        }

        return false;
    }
}
