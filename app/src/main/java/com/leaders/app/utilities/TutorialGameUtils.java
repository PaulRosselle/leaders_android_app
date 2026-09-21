package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.Settings;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TutorialGameUtils {
    private TutorialGameUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    @NonNull
    public static GameHistory getDefaultHistory(@NonNull Context context) {
        List<Player> players = new ArrayList<>();

        Settings settings = ((LeadersApplication) context.getApplicationContext()).getSettings();
        Player player = new Player(getPlayerTeamColor(), !settings.getUserName().isEmpty() ?
                settings.getUserName() : context.getString(R.string.player));
        players.add(player);
        players.add(new Player(getPlayerTeamColor().getOpposite(), context.getString(R.string.barry_the_bot)));

        List<Character> leaders = List.of(
                Character.create(CharacterType.LeaderQueen, TeamColor.Black),
                Character.create(CharacterType.LeaderKing, TeamColor.White)
        );


        // The default game history is initialized with each leader at their starting position
        List<RecruitmentActionMotion> leaderRecruitmentMotions = new ArrayList<>();
        for (Character leader : leaders) {
            leaderRecruitmentMotions.add(new RecruitmentActionMotion(
                    RecruitmentMotionType.Add, leader,
                    BoardQuery.getLeaderStartingPosition(leader.getTeamColor())
            ));
        }

        GameConfig gameConfig =new GameConfig(
                players, player, GameMode.Discovery, getRecruitableCards(),
                List.of(new RecruitmentAction(leaderRecruitmentMotions))
        );

        return new GameHistory(gameConfig, new ArrayList<>());
    }

    public static TeamColor getPlayerTeamColor() {
        return TeamColor.Black;
    }

    private static List<CharacterCard> getRecruitableCards() {
        List<CharacterCard> firstTurnCards = List.of(
                CharacterCard.Acrobat,
                CharacterCard.Rider,
                CharacterCard.Wanderer
        );

        CharacterCard botFirstPick = CharacterCard.Brewmaster;

        List<CharacterCard> remainingCharacters = new ArrayList<>(List.of(
                CharacterCard.Archer,
                CharacterCard.Assassin,
                CharacterCard.Bruiser,
                CharacterCard.ClawLauncher,
                CharacterCard.Illusionist,
                CharacterCard.Manipulator,
                CharacterCard.RoyalGuard,
                CharacterCard.Vizier
        ));
        Collections.shuffle(remainingCharacters);

        List<CharacterCard> recruitableCards = new ArrayList<>(firstTurnCards);
        recruitableCards.add(botFirstPick);
        recruitableCards.addAll(remainingCharacters);

        return recruitableCards;
    }
}
