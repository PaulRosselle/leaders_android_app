package com.leaders.puzzlelogic.serializers.entities;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.enums.TransitionTarget;
import com.leaders.gamelogic.enums.TransitionType;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.BanishmentPhase;
import com.leaders.puzzlelogic.serializers.SerializerRoundTripTestSupport;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameHistorySerializerTest {
    @Test
    public void roundTrip_shouldPreserveJson() throws Exception {
        Player firstPlayer = new Player(TeamColor.Black, "Alice");
        GameConfig config =
                new GameConfig(
                        Arrays.asList(firstPlayer),
                        firstPlayer,
                        GameMode.Strategist,
                        Arrays.asList(CharacterCard.Acrobat),
                        Arrays.asList(
                                (IGameAction) new TransitionAction(
                                        TransitionType.Start,
                                        TransitionTarget.Turn
                                )
                        )
                );

        BanishmentPhase banishmentPhase =
                new BanishmentPhase(
                        new TransitionAction(
                                TransitionType.Start,
                                TransitionTarget.BanishmentPhase
                        ),
                        new TransitionAction(
                                TransitionType.End,
                                TransitionTarget.BanishmentPhase
                        ),
                        TeamColor.Black
                );

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        entries.add(banishmentPhase);

        GameHistory history = new GameHistory(config, entries);

        SerializerRoundTripTestSupport.assertRoundTrip(
                new GameHistorySerializer(),
                history,
                SerializerRoundTripTestSupport.contextWith()
        );
    }
}
