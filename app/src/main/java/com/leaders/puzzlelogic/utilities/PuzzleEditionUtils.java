package com.leaders.puzzlelogic.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PuzzleEditionUtils {
    private PuzzleEditionUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static String getPuzzleValidityErrors(@NonNull Context context, @NonNull GameHistory gameHistory) {
        return getPuzzleValidityErrors(context, GameFactory.create(gameHistory).getBoard());
    }

    public static String getPuzzleValidityErrors(@NonNull Context context, @NonNull Board board) {
        return ""; // TODO
    }

    public static String getCardAdditionErrors(@NonNull Context context,
                                               @NonNull Board board,
                                               @NonNull CharacterCard cardToAdd,
                                               @NonNull List<TeamColor> addableColors) {
        addableColors.addAll(Arrays.asList(TeamColor.values()));

        if (!cardToAdd.isLeader() && !isCardRestrictedToOnePerTeam(cardToAdd)) {
            return "";
        }

        for (Cell cell : board.getCells().values()) {
            Character character = cell.getCharacter();

            if (character == null) {
                continue;
            }

            CharacterCard existingCard = character.getCharacterType().getCharacterCard();

            // The exact same leader can only exist once on the board.
            if (cardToAdd.isLeader() && cardToAdd == existingCard) {
                return context.getString(R.string.error_toast_limited_character);
            }

            // Leaders and restricted cards cannot be added to a team that already contains one.
            boolean isLeader = cardToAdd.isLeader() && existingCard.isLeader();
            boolean isRestrictedCard = cardToAdd == existingCard && isCardRestrictedToOnePerTeam(cardToAdd);

            if (isLeader || isRestrictedCard) {
                addableColors.remove(character.getTeamColor());
                if (addableColors.isEmpty()) {
                    if (isLeader) {
                        return context.getString(R.string.error_toast_max_two_leaders_on_the_board);
                    }
                    return context.getString(R.string.error_toast_character_limited_to_one_per_team);
                }
            }

        }

        return "";
    }

    public static boolean isCardRestrictedToOnePerTeam(@NonNull CharacterCard characterCard) {
        return characterCard == CharacterCard.Nemesis;
    }

    @NonNull
    public static GameHistory getDefaultHistory(@NonNull List<IGameAction> initialActions) {
        // The black player represents the human player (official puzzles convention)
        Player playerBlack = new Player(TeamColor.Black, "Player");
        Player playerWhite = new Player(TeamColor.White, "Puzzle");

        GameConfig gameConfig =new GameConfig(
                List.of(playerBlack, playerWhite),
                playerBlack, // firstPlayer ; the human player
                GameMode.Strategist,
                Collections.emptyList(), // initialRecruitableCards ; no recruitment in puzzles
                initialActions
        );

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        Turn turn = new Turn(playerBlack.getTeamColor());

        // We start and end immediately the turn start phase to skip to the actions phase
        TurnPhase turnStartPhase = turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();
        turnStartPhase.end();

        // Finally, we start the actions turn
        TurnPhase turnActionsPhase = turn.getSubPhase(GamePhaseType.Actions);
        turnActionsPhase.start();
        entries.add(turn);

        return new GameHistory(gameConfig, entries);
    }

    @NonNull
    public static GameHistory getDefaultHistory() {
        // The default game history is initialized each leader at their starting position
        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);

        List<IGameAction> initialActions = new ArrayList<>();
        initialActions.add(new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack,
                        BoardQuery.getLeaderStartingPosition(leaderBlack.getTeamColor())),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite,
                        BoardQuery.getLeaderStartingPosition(leaderWhite.getTeamColor()))
        )));
        return getDefaultHistory(initialActions);
    }

    @NonNull
    public static GameHistory getDefaultHistory(@NonNull Board board) {
        List<IGameAction> initialActions = new ArrayList<>();
        for (Cell characterCell : BoardQuery.findCharacterCells(board, null, null)) {
            initialActions.add(new RecruitmentAction(List.of(
                    new RecruitmentActionMotion(RecruitmentMotionType.Add,
                            characterCell.getCharacter(),
                            characterCell.getPosition())
                    ))
            );
        }
        return getDefaultHistory(initialActions);
    }
}
