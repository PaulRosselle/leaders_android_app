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
import com.leaders.app.views.character.CharacterActionAnimator;
import com.leaders.app.views.character.CharacterDisplay;
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
import java.util.concurrent.atomic.AtomicInteger;

public final class PuzzleEditorActivity extends BaseActivity {
    enum EditorState {
        Default,
        AddingCharacter,
        EditingCharacter,
        Animating
    }

    private CharacterNotificationView cnvCardInfo;
    private PuzzleEditorBoardView bdvBoard;
    private CharacterEditorView cevCharacterEditor;

    private Board board;
    private EditorState editorState;
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

        // Non interactive element listeners
        findViewById(R.id.clyMain_actPuzzleEditor).setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnClickListener(this::onNonInteractiveElementClick);
        cevCharacterEditor.setOnPortraitsScrollViewClick(this::onNonInteractiveElementClick);

        // Board element listeners
        bdvBoard.setOnCellClickListener(this::onBoardCellClick);
        bdvBoard.setOnCharacterClickListener(this::onBoardCharacterClick);
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
            // Once the board has been loaded in the view, we can apply the default editorState
            bdvBoard.setBoard(board);
            applyDefaultEditorState();
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
            applyDefaultEditorState();
        });
    }

    private void replaceByNewCharacter(@NonNull PlayableCharacter destCharacter) {
        editorState = EditorState.Animating;

        Character character = cevCharacterEditor.getSelectedNewCharacter();
        if (character == null) {
            throw new IllegalStateException("A new character must be selected to be added to the board");
        }

        CharacterDisplay characterDisplay = bdvBoard.getCharacterDisplay(destCharacter.getPosition());
        characterDisplay.getCharacterView().animateSetCharacter(character, () -> {
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

    private void onBoardCharacterClick(View v) {
        if (editorState == EditorState.Animating) {
            return;
        }

        InteractionTarget target = Objects.requireNonNull(((CharacterView) v).getTarget(),
                "Target missing on character click in state: " + editorState);
        PlayableCharacter playableCharacter = Objects.requireNonNull(target.getChosenPlayableCharacter(), "Invalid character target : playable character missing");


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
}