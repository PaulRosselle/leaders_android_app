package com.leaders.puzzlelogic.serializers.entities;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.enums.WarningType;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GameConfigSerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Player firstPlayer = new Player(TeamColor.Black, "Alice");
        Player secondPlayer = new Player(TeamColor.White, "Bob");

        List<IGameAction> initialPlacements = Arrays.asList(
                new TransitionAction(TransitionType.Start, TransitionTarget.Turn),
                new BanishmentAction(CharacterCard.Acrobat, TeamColor.Black),
                new WarningAction(WarningType.Barrage, TeamColor.White, -1)
        );

        GameConfig config =
                new GameConfig(
                        Arrays.asList(firstPlayer, secondPlayer),
                        firstPlayer,
                        GameMode.Discovery,
                        Arrays.asList(CharacterCard.Acrobat, CharacterCard.Bruiser),
                        initialPlacements
                );

        SerializerRoundTripTestSupport.assertRoundTrip(
                new GameConfigSerializer(),
                config,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
