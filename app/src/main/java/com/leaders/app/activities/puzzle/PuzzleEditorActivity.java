package com.leaders.app.activities.puzzle;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.PuzzleSource;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.PuzzleExportUtils;
import com.leaders.app.utilities.PuzzleImportUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.board.CellView;
import com.leaders.app.views.animators.CharacterActionAnimator;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.puzzle.PuzzleSaveView;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;
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
import com.leaders.puzzlelogic.utilities.solver.PuzzleSolverUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class PuzzleEditorActivity extends BaseActivity {
    enum EditorState {
        Default,
        AddingCharacter,
        EditingCharacter,
        Animating
    }

    private enum PuzzleCreationAction {
        Save,
        Play,
        SearchForSolution,
        ImportCode,
        ExportCode,
        DisplayCellPositions;

        private int getIconResId() {
            switch (this) {
                case Save: return R.drawable.icon_save;
                case Play: return R.drawable.icon_arrow_head;
                case SearchForSolution: return R.drawable.icon_search;
                case ImportCode: return R.drawable.icon_import;
                case ExportCode: return R.drawable.icon_export;
                case DisplayCellPositions: return R.drawable.icon_position;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case Save: return R.string.save;
                case Play: return R.string.play;
                case SearchForSolution: return R.string.search_for_solutions;
                case ImportCode: return R.string.import_puzzle;
                case ExportCode: return R.string.export_puzzle;
                case DisplayCellPositions: return R.string.board_coordinates;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(@NonNull PuzzleEditorActivity activity) {
            switch (this) {
                case Save: return activity::btnSaveClick;
                case Play: return activity::btnPlayClick;
                case SearchForSolution: return activity::btnSearchForSolutionsClick;
                case ImportCode: return activity::btnImportClick;
                case ExportCode: return activity::btnExportClick;
                case DisplayCellPositions: return activity::btnDisplayCellPosition;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private View vwDialogBg;
    private MaterialButton btnPuzzleActions;
    private ActionsMenuView amvPuzzleActions;
    private PuzzleSaveView psvSave;

    private CharacterNotificationView cnvCardInfo;
    private PuzzleEditorBoardView bdvBoard;
    private CharacterEditorView cevCharacterEditor;

    private Board initialBoard;
    private Board board;
    private EditorState editorState;
    private List<CustomPuzzleSave> puzzleSaves;
    private CustomPuzzleSave puzzleSave;
    private PlayableCharacter selectedBoardCharacter;

    protected void initViews() {
        super.initViews();

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzleEditor);
        bdvBoard = findViewById(R.id.bdvBoard_actPuzzleEditor);
        cevCharacterEditor = findViewById(R.id.cevCharacterEditor_actPuzzleEditor);

        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzleEditor);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleEditor);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzleEditor);
        for (PuzzleCreationAction action : PuzzleCreationAction.values()) {
            amvPuzzleActions.addActionButton(action.getIconResId(), action.getTextResId(),
                    action.ordinal(), action.getOnClickListener(this));
        }
        psvSave = findViewById(R.id.psvSave_actPuzzleEditor);
        ViewCompat.setOnApplyWindowInsetsListener(psvSave, (v, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                psvSave.setTranslationY(-imeInsets.bottom);
                return insets;
        });
        ViewCompat.requestApplyInsets(psvSave);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Puzzle actions listeners
        btnPuzzleActions.setOnClickListener(v ->
                setActionsMenuVisible(amvPuzzleActions.getVisibility() != View.VISIBLE));
        vwDialogBg.setOnClickListener(this::vwDialogBgClick);

        // Save dialog listeners
        psvSave.setOnBtnSaveClick(this::onSaveConfirmClick);
        psvSave.setOnBtnCancelClick(this::onSaveCancelClick);

        // Non interactive element listeners
        findViewById(R.id.clyMain_actPuzzleEditor).setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnPortraitsScrollViewClick(this::onNonInteractiveElementClick);

        // Board element listeners
        bdvBoard.setOnCellClickListener(this::onBoardCellClick);
        bdvBoard.setOnCharacterDisplayClickListener(this::onBoardCharacterClick);
        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);

        // Character editor listeners
        cevCharacterEditor.setOnCardPortraitClick(this::onCardPortraitClick);
        cevCharacterEditor.setOnCardPortraitLongClick(this::onCardPortraitLongClick);
        cevCharacterEditor.setOnSwitchColorClick(this::onSwitchColorClick);
        cevCharacterEditor.setOnRemoveClick(this::onRemoveClick);

        cnvCardInfo.setOnClickListener(v -> cnvCardInfo.hide());
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        puzzleSaves = JsonUtils.loadCustomPuzzles(this);

        // When editing an existing puzzle, its index within customPuzzleSaves is sent through the intent
        Intent intent = getIntent();
        int puzzleIdx = intent.getIntExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, -1);
        boolean isImported = intent.getBooleanExtra(ExtraUtils.EXTRA_PUZZLE_IMPORTED, false);

        // We load the current state of the board using the puzzle save game history
        puzzleSave = puzzleIdx != -1 ? puzzleSaves.get(puzzleIdx) : null;

        GameHistory puzzleGameHistory;

        // If puzzle datas were received, we use them directly
        String puzzleDatas = getIntent().getStringExtra(ExtraUtils.EXTRA_PUZZLE_DATAS);
        if (puzzleDatas != null && !puzzleDatas.isEmpty()) {
            try {

                JSONObject joGameHistory = new JSONObject(puzzleDatas);
                GameHistorySerializer serializer = new GameHistorySerializer();
                puzzleGameHistory = serializer.getFromJson(joGameHistory, new SerializationContext());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Without explicit puzzle datas, we try to use the datas within the puzzle save
            puzzleGameHistory = puzzleSave != null ?
                    puzzleSave.getPuzzleGameHistory() : PuzzleEditionUtils.getDefaultHistory();
        }

        // An imported puzzle is only saved temporarely so it can be transmitted to the editor,
        // we display the save dialog in case the user wants to name it and save it
        if (isImported) {
            puzzleSaves.remove(puzzleSave);
            JsonUtils.saveCustomPuzzles(this, puzzleSaves);
            puzzleSave = null;
            openSavePuzzleDialog();
        }

        bdvBoard.post(() -> loadPuzzleFromHistory(puzzleGameHistory));
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
            builder.setTitle(R.string.unsaved_puzzle_changes);
            builder.setMessage(R.string.go_back_to_puzzle_menu);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> goBackToPuzzlesMenuActivity());
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            goBackToPuzzlesMenuActivity();
        }
    }

    private void goBackToPuzzlesMenuActivity() {
        goToActivity(ActivityType.PuzzleSelection, ActivityTransitionType.SlideLeft);
    }

    private boolean hasPuzzleBeenEdited() {
        return !PuzzleExportUtils.getLbeUrl(initialBoard).equals(PuzzleExportUtils.getLbeUrl(board));
    }

    private void loadPuzzleFromHistory(@NonNull GameHistory gameHistory) {
        initialBoard = GameFactory.create(gameHistory).getBoard();
        board = new Board(initialBoard);
        bdvBoard.setBoard(board);
        applyDefaultEditorState();
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

    //region INTERACTION METHODS

    private void applyDefaultEditorState() {
        selectedBoardCharacter = null;
        bdvBoard.clearCharacterSelection();

        bdvBoard.clearTargets();
        bdvBoard.applyCharacterTargets(board);

        cevCharacterEditor.startSelectCardMode();

        editorState = EditorState.Default;
    }

    private void addNewCharacter(@NonNull Position position) {
        editorState = EditorState.Animating;

        Character character = cevCharacterEditor.getSelectedNewCharacter();
        if (character == null) {
            throw new IllegalStateException("A new character must be selected to be added to the board");
        }

        CharacterActionMotion actionMotion = new CharacterActionMotion(
                CharacterMotionType.Add,
                List.of(new CharacterActionTarget(character, null, position))
        );

        CharacterActionAnimator.animate(bdvBoard, actionMotion, () -> {
            board.getCell(position).setCharacter(character);

            // If there are still characters to add, we continue in AddingCharacter mode
            cevCharacterEditor.removeNewCharactersMatching(
                    character.getTeamColor().getOpposite(),
                    character.getCharacterType()
            );
            if (cevCharacterEditor.hasCharactersToAdd()) {
                bdvBoard.applyCellTargets();
                bdvBoard.applyCharacterTargets(board);
                editorState = EditorState.AddingCharacter;
            } else {
                applyDefaultEditorState();
            }
        });
    }

    private void replaceByNewCharacter(@NonNull PlayableCharacter destCharacter) {
        editorState = EditorState.Animating;

        Character character = cevCharacterEditor.getSelectedNewCharacter();
        if (character == null) {
            throw new IllegalStateException("A new character must be selected to be added to the board");
        }

        CharacterActionMotion motion = new CharacterActionMotion(CharacterMotionType.Transform, List.of(
                new CharacterActionTarget(destCharacter.getCharacter(), destCharacter.getPosition(), null),
                new CharacterActionTarget(character, null, destCharacter.getPosition())
        ));

        CharacterActionAnimator.animate(bdvBoard, motion, () -> {
            board.getCell(destCharacter.getPosition()).setCharacter(character);
            applyDefaultEditorState();
        });
    }

    private void moveCharacter(@NonNull Position position) {
        editorState = EditorState.Animating;

        CharacterActionMotion actionMotion = new CharacterActionMotion(
                CharacterMotionType.Move,
                List.of(new CharacterActionTarget(
                        selectedBoardCharacter.getCharacter(),
                        selectedBoardCharacter.getPosition(),
                        position
                ))
        );

        CharacterActionAnimator.animate(bdvBoard, actionMotion, () -> {
            board.getCell(selectedBoardCharacter.getPosition()).setCharacter(null);
            board.getCell(position).setCharacter(selectedBoardCharacter.getCharacter());
            applyDefaultEditorState();
        });
    }

    private void swapCharacters(@NonNull PlayableCharacter destCharacter) {
        editorState = EditorState.Animating;

        CharacterActionMotion actionMotion = new CharacterActionMotion(
                CharacterMotionType.Swap,
                List.of(new CharacterActionTarget(selectedBoardCharacter.getCharacter(),
                                selectedBoardCharacter.getPosition(),
                                destCharacter.getPosition()),
                        new CharacterActionTarget(destCharacter.getCharacter(),
                                destCharacter.getPosition(),
                                selectedBoardCharacter.getPosition())
                )
        );

        CharacterActionAnimator.animate(bdvBoard, actionMotion, () -> {
            board.getCell(selectedBoardCharacter.getPosition()).setCharacter(destCharacter.getCharacter());
            board.getCell(destCharacter.getPosition()).setCharacter(selectedBoardCharacter.getCharacter());
            applyDefaultEditorState();
        });
    }

    private void switchCharacterColor() {
        editorState = EditorState.Animating;

        Character character = selectedBoardCharacter.getCharacter();
        CharacterType characterType = character.getCharacterType();
        CharacterCard characterCard = characterType.getCharacterCard();
        TeamColor teamColor = character.getTeamColor().getOpposite();
        Position position = selectedBoardCharacter.getPosition();

        // Leaders and some characters are restricted to one per team.
        // When the user switches the color of one of them, we must switch the color of the other
        Cell otherCharacterCell = null;
        if (characterCard.isLeader()) {
            Cell leaderCell = BoardQuery.findLeaderCell(board, teamColor);
            if (leaderCell != null) {
                otherCharacterCell = leaderCell;
            }
        }
        if (PuzzleEditionUtils.isCardRestrictedToOnePerTeam(characterCard)) {
            List<Cell> otherCharacterCells = BoardQuery.findCharacterCells(board, teamColor, characterType);
            if (!otherCharacterCells.isEmpty()) {
                otherCharacterCell = otherCharacterCells.get(0);
            }
        }

        AtomicInteger remaining = new AtomicInteger(otherCharacterCell != null ? 2 : 1);
        Runnable onAnimationEnd = () -> {
            if (remaining.decrementAndGet() == 0) {
                applyDefaultEditorState();
            }
        };

        animateSwitchTeamColor(character, position, teamColor, true, onAnimationEnd);
        if (otherCharacterCell != null) {
            animateSwitchTeamColor(otherCharacterCell.getCharacter(),
                    otherCharacterCell.getPosition(),
                    teamColor.getOpposite(), false, onAnimationEnd);
        }
    }

    private void animateSwitchTeamColor(@NonNull Character character, @NonNull Position position,
                                        @NonNull TeamColor newTeamColor, boolean cleaHighlight,
                                        @NonNull Runnable onAnimationEnd) {
        CharacterDisplay characterDisplay = bdvBoard.getCharacterDisplay(position);

        if (cleaHighlight) {
            characterDisplay.stopHighlightAnimation();
            characterDisplay.getHighlightView().setVisibility(View.GONE);
            characterDisplay.getCharacterView().scaleForHighlight(false, true);
        }

        CharacterType characterType = character.getCharacterType();
        characterDisplay.getCharacterView().animateSetCharacter(characterType, newTeamColor, () -> {
            board.getCell(position).setCharacter(Character.transform(character, characterType, newTeamColor));
            onAnimationEnd.run();
        });
    }

    private void removeCharacter() {
        editorState = EditorState.Animating;

        Character character = selectedBoardCharacter.getCharacter();
        Position position = selectedBoardCharacter.getPosition();

        CharacterActionMotion actionMotion = new CharacterActionMotion(
                CharacterMotionType.Remove,
                List.of(new CharacterActionTarget(character, position, null))
        );

        CharacterActionAnimator.animate(bdvBoard, actionMotion, () -> {
            board.getCell(position).setCharacter(null);
            applyDefaultEditorState();
        });
    }

    //endregion

    //region LISTENER METHODS

    private void onCardPortraitClick(View v) {
        if (editorState != EditorState.Default) {
            if (editorState != EditorState.Animating) {
                applyDefaultEditorState();
            }
            return;
        }

        // Some characters addition are restricted must be prevented here
        CharacterCard portraitCard = ((CharacterCardPortraitView) v).getPortraitCard();
        List<TeamColor> addableColors = new ArrayList<>();
        String errors = PuzzleEditionUtils.getCardAdditionErrors(this, board, portraitCard, addableColors);
        if (!errors.isEmpty()) {
            Toast.makeText(this, errors, Toast.LENGTH_SHORT).show();
            return;
        }

        editorState = EditorState.AddingCharacter;
        bdvBoard.clearTargets();

        ArrayList<Character> addableCharacters = new ArrayList<>();
        // We add every character linked with the portrait card in every color available
        for (TeamColor teamColor : addableColors) {
            for (CharacterType characterType : CharacterType.getCharacterTypesMatchingCard(portraitCard)) {
                Character token = Character.create(characterType, teamColor);
                addableCharacters.add(token);
            }
        }

        cevCharacterEditor.startAddCardCharactersMode(
                addableCharacters,
                bdvBoard.getCharacterDisplaySize(),
                this::onNewCharacterClick,
                this::onCharacterLongClick
        );

        bdvBoard.applyCellTargets();
        bdvBoard.applyCharacterTargets(board);
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
        if (editorState == EditorState.AddingCharacter) {
            return;
        }

        switchCharacterColor();
    }

    private void onRemoveClick(View v) {
        if (editorState == EditorState.AddingCharacter) {
            return;
        }

        removeCharacter();
    }

    private void onBoardCellClick(View v) {
        if (editorState == EditorState.Default || editorState == EditorState.Animating) {
            return;
        }

        InteractionTarget target = Objects.requireNonNull(((CellView) v).getTarget(),
                "Target missing on cell click in state: " + editorState);
        Position position = Objects.requireNonNull(target.getChosenPosition(), "Invalid cell target : position missing");

        if (selectedBoardCharacter != null) {
            moveCharacter(position);
        } else {
            addNewCharacter(position);
        }
    }

    private void onBoardCharacterClick(@NonNull CharacterDisplay characterDisplay) {
        if (editorState == EditorState.Animating) {
            return;
        }

        InteractionTarget target = Objects.requireNonNull(characterDisplay.getCharacterView().getTarget(),
                "Target missing on character click in state: " + editorState);
        PlayableCharacter playableCharacter = Objects.requireNonNull(target.getChosenPlayableCharacter(),
                "Invalid character target : playable character missing");


        if (editorState != EditorState.Default) {
            if (selectedBoardCharacter == null) {
                replaceByNewCharacter(playableCharacter);
            } else if (!selectedBoardCharacter.getPosition().equals(playableCharacter.getPosition())) {
                swapCharacters(playableCharacter);
            } else {
                applyDefaultEditorState();
            }
        } else {
            editorState = EditorState.EditingCharacter;
            bdvBoard.clearTargets();

            selectedBoardCharacter = playableCharacter;
            bdvBoard.selectCharacterAt(selectedBoardCharacter.getPosition());

            cevCharacterEditor.startEditCharacterMode(selectedBoardCharacter.getCharacter());
            bdvBoard.applyCellTargets();
            bdvBoard.applyCharacterTargets(board);
        }
    }

    private void onNonInteractiveElementClick(View v) {
        if (editorState != EditorState.Default && editorState != EditorState.Animating) {
            applyDefaultEditorState();
        }
    }

    // endregion


    //region PUZZLE ACTIONS LISTENER METHODS

    private void btnSaveClick(View v) {
        amvPuzzleActions.setVisibility(View.GONE);
        openSavePuzzleDialog();
    }

    private void btnPlayClick(View v) {
        String validityErrors = PuzzleEditionUtils.getPuzzleValidityErrors(this,
                GameFactory.create(PuzzleEditionUtils.getDefaultHistory(board)));

        if (validityErrors.isEmpty()) {
            Intent intent = ActivityType.PuzzlePlayer.getIntent(this);

            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_SOURCE, PuzzleSource.Editor.name());
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleSaves.indexOf(puzzleSave));

            GameHistorySerializer serializer = new GameHistorySerializer();
            try {
                GameHistory gameHistory = PuzzleEditionUtils.getDefaultHistory(board);
                intent.putExtra(ExtraUtils.EXTRA_PUZZLE_DATAS, serializer.getAsJson(gameHistory).toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            goToActivity(intent);

        } else {
            new AlertDialog.Builder(this, R.style.alert_dialog_theme)
                    .setTitle(R.string.invalid_puzzle)
                    .setMessage(validityErrors)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }

        setActionsMenuVisible(false);
    }

    private void btnSearchForSolutionsClick(View v) {
        List<Cell> playerCharacterCells = BoardQuery.findCharacterCells(board,
                PuzzleEditionUtils.getPuzzlePlayerTeamColor(), null);
        if (playerCharacterCells.size() >= PuzzleSolverUtils.MAX_PLAYER_CHARACTER_COUNT) {
            new AlertDialog.Builder(this, R.style.alert_dialog_theme)
                    .setTitle(R.string.solution_search_not_recommended)
                    .setMessage(String.format(getString(R.string.search_with_x_characters_can_take_a_long_time),
                            PuzzleSolverUtils.MAX_PLAYER_CHARACTER_COUNT))
                    .setPositiveButton(R.string.proceed, (dialog, which) -> searchForSolution())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            searchForSolution();
        }

        setActionsMenuVisible(false);
    }

    private void searchForSolution() {
        String validityErrors = PuzzleEditionUtils.getPuzzleValidityErrors(this,
                GameFactory.create(PuzzleEditionUtils.getDefaultHistory(board)));

        if (validityErrors.isEmpty()) {
            Intent intent = ActivityType.PuzzleSolver.getIntent(this);
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleSaves.indexOf(puzzleSave));
            GameHistorySerializer serializer = new GameHistorySerializer();
            GameHistory gameHistory = PuzzleEditionUtils.getDefaultHistory(board);
            try {
                intent.putExtra(ExtraUtils.EXTRA_PUZZLE_DATAS, serializer.getAsJson(gameHistory).toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            goToActivity(intent);
        } else {
            new AlertDialog.Builder(this, R.style.alert_dialog_theme)
                    .setTitle(R.string.invalid_puzzle)
                    .setMessage(validityErrors)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }

    private void btnImportClick(View v) {
        GameHistory gameHistory = PuzzleImportUtils.importPuzzleFromClipboard(this);
        if (gameHistory != null) {
            loadPuzzleFromHistory(gameHistory);
        }

        setActionsMenuVisible(false);
    }

    private void btnExportClick(View v) {
        String validityErrors = PuzzleEditionUtils.getPuzzleValidityErrors(this,
                GameFactory.create(PuzzleEditionUtils.getDefaultHistory(board)));

        if (validityErrors.isEmpty()) {
            PuzzleExportUtils.exportAsTextIntent(this, PuzzleExportUtils.getLbeUrl(board));
            applyDefaultEditorState();
        } else {
            new AlertDialog.Builder(this, R.style.alert_dialog_theme)
                    .setTitle(R.string.invalid_puzzle)
                    .setMessage(validityErrors)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }

        setActionsMenuVisible(false);
    }

    private void btnDisplayCellPosition(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());

        setActionsMenuVisible(false);
    }

    private void vwDialogBgClick(View v) {
        if (amvPuzzleActions.getVisibility() == View.VISIBLE) {
            setActionsMenuVisible(false);
        } else if (psvSave.getVisibility() == View.VISIBLE) {
            setSaveDialogVisible(false);
        } else {
            vwDialogBg.setVisibility(View.GONE);
        }
    }

    private void setActionsMenuVisible(boolean visible) {
        amvPuzzleActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion

    //region SAVE DIALOG LISTENER METHODS

    private void openSavePuzzleDialog() {
        // In case the user overwrite the default value and want them back,
        // we apply them every time the save form is reopened
        psvSave.setDefaultPuzzleName(puzzleSave != null ? puzzleSave.getName() : "");
        psvSave.setDefaultPuzzleAuthor(puzzleSave != null ? puzzleSave.getAuthor() : "");
        setSaveDialogVisible(true);
    }

    private void onSaveCancelClick(View v) {
        setSaveDialogVisible(false);
    }

    private void onSaveConfirmClick(View v) {
        String puzzleName = psvSave.getPuzzleName();
        // We don't allow a puzzle to be saved without a name
        if (puzzleName.isEmpty()) {
            Toast.makeText(this, R.string.puzzle_name_is_mandatory, Toast.LENGTH_SHORT).show();
            return;
        }

        GameHistory gameHistory = PuzzleEditionUtils.getDefaultHistory(board);
        if (puzzleSave == null) {
            puzzleSave = CustomPuzzleSave.getDefault(gameHistory);
            puzzleSaves.add(puzzleSave);
        } else {
            puzzleSave.updatePuzzleGameHistory(gameHistory);
            puzzleSave.setSolved(false);
        }
        puzzleSave.setName(puzzleName);
        puzzleSave.setAuthor(psvSave.getPuzzleAuthor());

        JsonUtils.saveCustomPuzzles(this, puzzleSaves);
        Toast.makeText(this, R.string.puzzle_saved, Toast.LENGTH_SHORT).show();

        loadPuzzleFromHistory(gameHistory);

        setSaveDialogVisible(false);
    }

    private void setSaveDialogVisible(boolean visible) {
        psvSave.setVisibility(visible ? View.VISIBLE : View.GONE);
        cevCharacterEditor.setVisibility(visible ? View.GONE : View.VISIBLE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .hide(WindowInsetsCompat.Type.ime());
        }
    }

    //endregion
}