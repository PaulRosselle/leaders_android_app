package com.leaders.app.activities.puzzle;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.PuzzleEditionUtils;
import com.leaders.app.views.CharacterCardPortraitView;
import com.leaders.app.views.CharacterEditorView;
import com.leaders.app.views.CharacterNotificationView;
import com.leaders.app.views.board.CharacterView;
import com.leaders.app.views.board.PuzzleEditorBoardView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterCard;
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
    private CharacterNotificationView cnvCardInfo;
    private PuzzleEditorBoardView pebvBoard;
    private CharacterEditorView cevCharacterEditor;

    private Board board;
    private boolean hasActionInProgress;

    protected void initViews() {
        super.initViews();

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzleEditor);
        pebvBoard = findViewById(R.id.pebvBoard_actPuzzleEditor);
        cevCharacterEditor = findViewById(R.id.cevCharacterEditor_actPuzzleEditor);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        cevCharacterEditor.setOnCardPortraitClick(this::onCardPortraitClick);
        cevCharacterEditor.setOnCardPortraitLongClick(this::onCardPortraitLongClick);
        cevCharacterEditor.setOnSwitchColorClick(this::onSwitchColorClick);
        cevCharacterEditor.setOnRemoveClick(this::onRemoveClick);

        cnvCardInfo.setOnClickListener(v -> cnvCardInfo.hide());
    }

    @Override
    protected void initDatas() {
        super.initDatas();

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

        board = GameFactory.create(gameHistory).getBoard();
        pebvBoard.post(() -> pebvBoard.setBoard(board));
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

    private void onCardPortraitClick(View v) {
        if (interruptActionIfNeeded()) {
            return;
        }

        hasActionInProgress = true;

        CharacterCard portraitCard = ((CharacterCardPortraitView) v).getPortraitCard();
        List<TeamColor> addableColors = new ArrayList<>();
        String errors = PuzzleEditionUtils.getCardAdditionErrors(this, board, portraitCard, addableColors);
        if (!errors.isEmpty()) {
            Toast.makeText(this, errors, Toast.LENGTH_SHORT).show();
            hasActionInProgress = false;
            return;
        }

        ArrayList<Character> addableCharacers = new ArrayList<>();
        // We add every character linked with the portrait card in every color available
        for (TeamColor teamColor : addableColors) {
            for (CharacterType characterType : CharacterType.getCharacterTypesMatchingCard(portraitCard)) {
                Character token = Character.create(characterType, teamColor);
                addableCharacers.add(token);
            }
        }
        cevCharacterEditor.startAddCardCharactersMode(
                addableCharacers,
                pebvBoard.getCharacterDisplaySize(),
                this::onNewCharacterClick
        );
    }

    private boolean onCardPortraitLongClick(View v) {
        CharacterCard portraitCard = ((CharacterCardPortraitView) v).getPortraitCard();

        if (cnvCardInfo.getCharacterCard() == portraitCard) {
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(portraitCard);
            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
        }

        return true;
    }

    private void onNewCharacterClick(View v) {
        cevCharacterEditor.selectNewCharacter((CharacterView) v);
    }

    private void onSwitchColorClick(View v) {
        // TODO
        // TODO - hasActionInProgress = false à la fin du traitement
    }

    private void onRemoveClick(View v) {
        // TODO
        // TODO - hasActionInProgress = false à la fin du traitement
    }

    private boolean interruptActionIfNeeded() {
        // TODO - 2 responsabilités distinctes
        // 1. Empêcher les méthodes ayant un impact sur le plateau de s'éxecuter
        //    pendant qu'une action est déjà en cours

        // 1. end

        // 2. Annuler l'interaction en cours ->
        pebvBoard.clearTargets();
        cevCharacterEditor.startSelectCardMode();
        // 2. end

        // An action can't be canceled while its animation is in progress
        /*if (FAddingNewToken || FBdvMain.isAnimatingTokenAction() ||
                FBoard.hasActionInProgress() || FTcvCreation.getVisibility() != View.GONE) {
            // If an action was in progress, we cancel it
            if (!FBdvMain.isAnimatingTokenAction()) {
                if (FBoard.hasActionInProgress()) {
                    FBoard.cancelActionInProgress();
                    FBdvMain.resetTilesMarkers();
                    for (ArrayList<Tile> tiles : FBoard.getTiles()) {
                        for (Tile tile : tiles) {
                            TileView tileView = FBdvMain.getTileView(tile);
                            if (tileView.isHighlighted()) {
                                tileView.stopAnimateHighlight();
                                tileView.setHighlight(false, false);
                            }
                        }
                    }
                }
                onTokenActionInProgressCancelled();
            }
            return true;
        }*/
        return false;
    }
}