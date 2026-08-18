package com.leaders.app.activities.puzzle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.views.board.PuzzleEditorBoardView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.historyentries.IHistoryEntry;
import com.leaders.gamelogic.historyentries.segments.Turn;
import com.leaders.gamelogic.historyentries.segments.TurnPhase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PuzzleEditorActivity extends BaseActivity {
    private PuzzleEditorBoardView pebvBoard;

    protected void initViews() {
        super.initViews();

        // TODO - remove dummy GameHistory
        Character leaderBlack = Character.create(CharacterType.LeaderKing, TeamColor.Black);
        Character leaderWhite = Character.create(CharacterType.LeaderQueen, TeamColor.White);

        RecruitmentAction initialPlacement = new RecruitmentAction(Arrays.asList(
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderBlack, new Position(3, 6)),
                new RecruitmentActionMotion(RecruitmentMotionType.Add, leaderWhite, new Position(3, 0))
        ));

        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(TeamColor.Black, "Paul"));
        players.add(new Player(TeamColor.White, "Elise"));
        GameConfig gameConfig =new GameConfig(
                players,
                players.get(1), // firstPlayer
                GameMode.Discovery,
                new ArrayList<>(), // initialRecruitableCards
                List.of(initialPlacement) // initialPlacements
        );

        ArrayList<IHistoryEntry> entries = new ArrayList<>();
        Turn turn = new Turn(TeamColor.Black);
        TurnPhase turnStartPhase = turn.getSubPhase(GamePhaseType.TurnStart);
        turnStartPhase.start();
        turnStartPhase.end();
        TurnPhase turnActionsPhase = turn.getSubPhase(GamePhaseType.Actions);
        turnActionsPhase.start();
        turnActionsPhase.getActions().add(new CharacterAction(leaderBlack,
                List.of(new CharacterActionMotion(
                        CharacterMotionType.Move,
                        List.of(new CharacterActionTarget(leaderBlack,
                                new Position(3, 6), new Position(3, 5))
                        ))
                ))
        );
        turnActionsPhase.end();
        entries.add(turn);

        GameHistory gameHistory = new GameHistory(gameConfig, entries);

        pebvBoard = findViewById(R.id.pebvBoard_actPuzzleEditor);
        pebvBoard.post(() -> pebvBoard.setBoard(GameFactory.create(gameHistory).getBoard()));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_editor;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleEditor;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleEditor;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return true;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PuzzleEditor;
    }

    @Override
    protected void doOnBackPressed() {
        if (hasPuzzleBeenEdited()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
            builder.setTitle(R.string.back);
            builder.setMessage(R.string.go_back_to_puzzle_menu);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> goBackToPuzzlesMenuActivity());
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            goBackToPuzzlesMenuActivity();
        }
    }

    private void goBackToPuzzlesMenuActivity() {
        goToActivity(ActivityType.Main, ActivityTransitionType.SlideLeft);
    }

    private boolean hasPuzzleBeenEdited() {
        // TODO - detect when the loaded puzzle has been edited
        return false;
    }
}