package com.leaders.app.activities.puzzle;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.board.CellView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.app.views.puzzle.CharacterEditorView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.app.views.board.PuzzleEditorBoardView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PuzzleEditorActivity extends BaseActivity {
    private CharacterNotificationView cnvCardInfo;
    private PuzzleEditorBoardView bdvBoard;
    private CharacterEditorView cevCharacterEditor;

    private Board board;
    private boolean hasInteractionInProgress;
    private List<CustomPuzzleSave> customPuzzleSaves;
    private Integer puzzleIdx;
    private PlayableCharacter selectedBoardCharacter;

    protected void initViews() {
        super.initViews();

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzleEditor);
        bdvBoard = findViewById(R.id.bdvBoard_actPuzzleEditor);
        cevCharacterEditor = findViewById(R.id.cevCharacterEditor_actPuzzleEditor);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // TODO - comment
        findViewById(R.id.clyMain_actPuzzleEditor).setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnPortraitsScrollViewClick(this::onNonInteractiveElementClick);

        // TODO - comment
        bdvBoard.setOnCellClickListener(this::onBoardCellClick);
        bdvBoard.setOnCharacterClickListener(this::onBoardCharacterClick);
        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);

        // TODO - comment
        cevCharacterEditor.setOnCardPortraitClick(this::onCardPortraitClick);
        cevCharacterEditor.setOnCardPortraitLongClick(this::onCardPortraitLongClick);
        cevCharacterEditor.setOnSwitchColorClick(this::onSwitchColorClick);
        cevCharacterEditor.setOnRemoveClick(this::onRemoveClick);

        cnvCardInfo.setOnClickListener(v -> cnvCardInfo.hide());
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        customPuzzleSaves = JsonUtils.loadCustomPuzzles(this);

        // When editing an existing puzzle, its index within customPuzzleSaves is sent through the intent
        Intent intent = getIntent();
        int intentPuzzleIdx = intent.getIntExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, -1);
        puzzleIdx = intentPuzzleIdx != -1 ? intentPuzzleIdx : null;

        // We load the current state of the board using the puzzle save game history
        GameHistory puzzleGameHistory = puzzleIdx != null ?
                customPuzzleSaves.get(puzzleIdx).getPuzzleGameHistory() :
                PuzzleEditionUtils.getDefaultHistory();
        board = GameFactory.create(puzzleGameHistory).getBoard();

        bdvBoard.post(() -> {
            bdvBoard.setBoard(board);
            // TODO - comment
            applyDefaultInteractionState();
        });
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

    private void showCardDescriptionNotification(@NonNull CharacterCard characterCard) {
        if (cnvCardInfo.getCharacterCard() == characterCard) {
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(characterCard);
            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
        }
    }

    private boolean interruptActionIfNeeded() {
        // TODO - 2 responsabilités distinctes
        // 1. Empêcher les méthodes ayant un impact sur le plateau de s'éxecuter
        //    pendant qu'une action est déjà en cours

        // 1. end

        // 2. Annuler l'interaction en cours ->
        bdvBoard.clearTargets();
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

    //region INTERACTION METHODS

    private void applyDefaultInteractionState() {
        selectedBoardCharacter = null;
        bdvBoard.applyCharacterTargets(board);
        cevCharacterEditor.startSelectCardMode();

        hasInteractionInProgress = false;
    }

    private void cancelInteractionInProgress() {
        if (selectedBoardCharacter != null) {
            bdvBoard.unselectCharacterAt(selectedBoardCharacter.getPosition());
        }
        applyDefaultInteractionState();
    }

    private void addNewCharacter(@NonNull Position position) {
        Character character = cevCharacterEditor.getSelectedNewCharacter();
        if (character == null) {
            throw new IllegalStateException("A new character must be selected to be added to the board");
        }

        // TODO - replace by an animation
        board.getCell(position).setCharacter(character);
        bdvBoard.setBoard(board);

        // TODO - delay after animation end
        applyDefaultInteractionState();
    }

    private void moveCharacter(@NonNull Position position) {
        // TODO
    }

    private void swapCharacters(@NonNull PlayableCharacter destCharacter) {
        // TODO
    }

    //endregion

    //region LISTENER METHODS

    private void onCardPortraitClick(View v) {
        if (interruptActionIfNeeded()) {
            return;
        }

        // TODO - comment
        hasInteractionInProgress = true;
        bdvBoard.clearTargets();

        CharacterCard portraitCard = ((CharacterCardPortraitView) v).getPortraitCard();
        List<TeamColor> addableColors = new ArrayList<>();
        String errors = PuzzleEditionUtils.getCardAdditionErrors(this, board, portraitCard, addableColors);
        if (!errors.isEmpty()) {
            Toast.makeText(this, errors, Toast.LENGTH_SHORT).show();
            hasInteractionInProgress = false;
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
                bdvBoard.getCharacterDisplaySize(),
                this::onNewCharacterClick,
                this::onCharacterLongClick
        );
        bdvBoard.applyCellTargets();
    }

    private boolean onCardPortraitLongClick(View v) {
        showCardDescriptionNotification(((CharacterCardPortraitView) v).getPortraitCard());
        return true;
    }

    private void onNewCharacterClick(View v) {
        cevCharacterEditor.selectNewCharacter((CharacterView) v);
    }

    private boolean onCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        showCardDescriptionNotification(characterType.getCharacterCard());
        return true;
    }

    private void onSwitchColorClick(View v) {
        // TODO
        // TODO - hasActionInProgress = false à la fin du traitement
    }

    private void onRemoveClick(View v) {
        // TODO
        // TODO - hasActionInProgress = false à la fin du traitement
    }

    private void onBoardCellClick(View v) {
        // TODO - comment
        if (!hasInteractionInProgress) {
            return;
        }

        // TODO - comment
        InteractionTarget target = ((CellView) v).getTarget();
        if (target == null) {
            cancelInteractionInProgress();
            return;
        }

        // TODO - comment
        bdvBoard.clearTargets();
        Position position = Objects.requireNonNull(target.getChosenPosition(), "Invalid cell target : position missing");
        if (selectedBoardCharacter != null) {
            moveCharacter(position);
        } else {
            addNewCharacter(position);
        }
    }

    private void onBoardCharacterClick(View v) {
        // TODO
    }

    private void onNonInteractiveElementClick(View v) {
        if (hasInteractionInProgress) {
            cancelInteractionInProgress();
        }
    }

    // endregion
}