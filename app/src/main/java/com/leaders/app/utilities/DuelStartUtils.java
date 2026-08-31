package com.leaders.app.utilities;

import androidx.annotation.NonNull;

import com.leaders.app.entities.PlayerSetup;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DuelStartUtils {
    private DuelStartUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    @NonNull
    public static GameHistory getDefaultHistory(@NonNull List<PlayerSetup> playerSetups,
                                                @NonNull TeamColor firstPlayerTeamColor,
                                                @NonNull GameMode gameMode,
                                                @NonNull List<CharacterCard> recruitableCards) {
        List<Player> players = new ArrayList<>();
        Player firstPlayer = null;
        List<Character> leaders = new ArrayList<>();

        for (PlayerSetup playerSetup : playerSetups) {
            Player player = playerSetup.createPlayer();
            if (player.getTeamColor() == firstPlayerTeamColor) {
                firstPlayer = player;
            }

            players.add(player);
            leaders.add(playerSetup.createLeader());
        }

        if (firstPlayer == null) {
            throw new IllegalArgumentException("Player not found for team: " + firstPlayerTeamColor);
        }

        // The default game history is initialized with each leader at their starting position
        List<RecruitmentActionMotion> leaderRecruitmentMotions = new ArrayList<>();
        for (Character leader : leaders) {
            leaderRecruitmentMotions.add(new RecruitmentActionMotion(
                    RecruitmentMotionType.Add,
                    leader,
                    BoardQuery.getLeaderStartingPosition(leader.getTeamColor())
            ));
        }

        // In discovery mode, recruitable cards are shuffled before the game starts
        if (gameMode == GameMode.Discovery) {
            Collections.shuffle(recruitableCards);
        }

        GameConfig gameConfig =new GameConfig(
                players, firstPlayer, gameMode, recruitableCards,
                List.of(new RecruitmentAction(leaderRecruitmentMotions))
        );

        return new GameHistory(gameConfig, new ArrayList<>());
    }

    public static List<CharacterCard> getDefaultRecruitableCards() {
        List<CharacterCard> characterCards = new ArrayList<>();

        for (CharacterCard characterCard : CharacterCard.values()) {
            if (characterCard.canBeRecruited()) {
                characterCards.add(characterCard);
            }
        }

        return characterCards;
    }
}
