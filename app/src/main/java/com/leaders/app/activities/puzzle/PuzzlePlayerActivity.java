package com.leaders.app.activities.puzzle;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.PuzzleSource;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleCategory;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class PuzzlePlayerActivity extends PlayableActivity {
    private enum PuzzlePlayerAction {
        GoToPreviousPuzzle,
        GoToNextPuzzle,
        DisplayCellPositions;

        private int getIconResId() {
            switch (this) {
                case GoToPreviousPuzzle: return R.drawable.icon_arrow_head_reversed;
                case GoToNextPuzzle: return R.drawable.icon_arrow_head;
                case DisplayCellPositions: return R.drawable.icon_position;
                default: throw new IllegalStateException("No icon found for puzzle action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case GoToPreviousPuzzle: return R.string.previous_puzzle;
                case GoToNextPuzzle: return R.string.next_puzzle;
                case DisplayCellPositions: return R.string.board_coordinates;
                default: throw new IllegalStateException("No text found for puzzle action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(@NonNull PuzzlePlayerActivity activity) {
            switch (this) {
                case GoToPreviousPuzzle: return activity::onPreviousPuzzleClick;
                case GoToNextPuzzle: return activity::onNextPuzzleClick;
                case DisplayCellPositions: return activity::onDisplayCellPositionClick;
                default: throw new IllegalStateException("No click listener found for puzzle action: " + this);
            }
        }
    }

    private MaterialButton btnPuzzleActions;
    private ActionsMenuView amvPuzzleActions;
    private View vwDialogBg;

    private MaterialButton btnReset;

    private TextView txvPuzzleName;
    private TextView txvAuthorName;


    private PuzzleSource puzzleSource;
    private List<? extends PuzzleSave> puzzleSaves;
    private PuzzleSave puzzleSave;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzlePlayer);
        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzlePlayer);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzlePlayer);
        for (PuzzlePlayerAction action : PuzzlePlayerAction.values()) {
            amvPuzzleActions.addActionButton(action.getIconResId(), action.getTextResId(),
                    action.ordinal(), action.getOnClickListener(this));
        }

        btnReset = findViewById(R.id.btnReset_actPuzzlePlayer);
        txvPuzzleName = findViewById(R.id.txvPuzzleName_actPuzzlePlayer);
        txvAuthorName = findViewById(R.id.txvAuthorName_actPuzzlePlayer);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actPuzzlePlayer)).setOnClickListener(this::onNonInteractiveElementClick);

        // Puzzle actions listeners
        btnPuzzleActions.setOnClickListener(this::onPuzzleActionsClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);

        btnReset.setOnClickListener(this::onResetClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        puzzleSource = PuzzleSource.valueOf(getIntent().getStringExtra(ExtraUtils.EXTRA_PUZZLE_SOURCE));

        if (puzzleSource == PuzzleSource.OfficialSelection) {
            puzzleSaves = JsonUtils.loadOfficialPuzzles(this);
        } else {
            puzzleSaves = JsonUtils.loadCustomPuzzles(this);
        }

        // When the player is loading a saved puzzle, its index is sent through the intent
        int puzzleIdx = getIntent().getIntExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, -1);

        // For unsaved puzzles, datas are directly sent through the intent
        String puzzleDatas = getIntent().getStringExtra(ExtraUtils.EXTRA_PUZZLE_DATAS);

        GameHistory puzzleGameHistory;
        if (puzzleDatas != null && !puzzleDatas.isEmpty()) {
            try {
                JSONObject joGameHistory = new JSONObject(puzzleDatas);
                puzzleGameHistory = (new GameHistorySerializer()).getFromJson(joGameHistory, new SerializationContext());
                puzzleSave = CustomPuzzleSave.getDefault(puzzleGameHistory);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (puzzleIdx != -1) {
            puzzleSave = puzzleSaves.get(puzzleIdx);
            puzzleGameHistory = puzzleSave.getPuzzleGameHistory();
        } else {
            throw new IllegalStateException("No puzzle data received by the player");
        }

        controller = new GameController(this);
        controller.startGame(puzzleGameHistory);
        updatePuzzleInfos();
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actPuzzlePlayer;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actPuzzlePlayer;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actPuzzlePlayer;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actPuzzlePlayer;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_player;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzlePlayer;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzlePlayer;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PuzzlePlayer;
    }

    @Override
    protected void doOnBackPressed() {
        if (puzzleSource == PuzzleSource.Editor) {
            Intent intent = ActivityType.PuzzleEditor.getIntent(this);

            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleSaves.indexOf(puzzleSave));
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_DATAS, puzzleSave.getDatas().toString());

            goToActivity(intent, ActivityTransitionType.SlideLeft);

        } else {
            goToActivity(ActivityType.PuzzleMenu, ActivityTransitionType.SlideLeft);
        }
    }

    //endregion

    //region VIEWS CLICK LISTENER METHODS

    private void onResetClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.new_attempt);
        builder.setMessage(R.string.restart_puzzle);
        builder.setPositiveButton(R.string.start_over, (dialogInterface, i) ->
                controller.restartGame(puzzleSave.getPuzzleGameHistory()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void onDialogBgClick(View v) {
        hidePuzzleActions();
    }

    private void onPuzzleActionsClick(View v) {
        // Before showing the actions menu, we must update the available actions based on the puzzle state
        updatePuzzleActions();

        amvPuzzleActions.setVisibility(View.VISIBLE);
        vwDialogBg.setVisibility(View.VISIBLE);
    }

    //endregion

    //region INTERACTION UI METHODS

    private void showEndGame(@NonNull TeamColor winnerColor, boolean isVictory) {
        GameContext gameContext = controller.getCurrentContext();

        EndGameType endGameType = isVictory ? EndGameType.Victory : EndGameType.Defeat;

        int titleId = isVictory ? R.string.victory_title : R.string.defeat_title;
        int subtitleId = isVictory ? R.string.victory_subtitle : R.string.defeat_subtitle;

        showEndGame(gameContext,
                winnerColor,
                endGameType,
                titleId,
                subtitleId
        );
    }

    private void saveProgress(boolean isSolved) {
        // Progression is never saved when a puzzle is tested in editor mode.
        if (puzzleSource == PuzzleSource.Editor) {
            return;
        }

        // We only save progress when the puzzle has been completed for the first time
        if (!isSolved && !puzzleSave.isSolved()) {
            return;
        }

        puzzleSave.setSolved(true);
        if (!puzzleSaves.contains(puzzleSave)) {
            throw new IllegalStateException("Cannot save progress for puzzle: " + puzzleSave.getName());
        }

        if (puzzleSave.getCategory() == PuzzleCategory.Official) {
            List<OfficialPuzzleSave> officialPuzzleSaves = new ArrayList<>();
            for (PuzzleSave officialPuzzleSave : puzzleSaves) {
                officialPuzzleSaves.add((OfficialPuzzleSave) officialPuzzleSave);
            }
            JsonUtils.saveOfficialPuzzles(this, officialPuzzleSaves);
        } else {
            List<CustomPuzzleSave> customPuzzleSaves = new ArrayList<>();
            for (PuzzleSave customPuzzleSave : puzzleSaves) {
                customPuzzleSaves.add((CustomPuzzleSave) customPuzzleSave);
            }
            JsonUtils.saveCustomPuzzles(this, customPuzzleSaves);
        }
    }

    //endregion

    //region CONTROLLER METHODS

    @Override
    public void onGameEnded(@NonNull Player winner) {
        runOnUiThread(() -> {
            TeamColor winnerColor = winner.getTeamColor();
            boolean isVictory = winnerColor == PuzzleEditionUtils.getPuzzlePlayerTeamColor();

            saveProgress(isVictory);
            showEndGame(winnerColor, isVictory);
        });
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        throw new IllegalStateException("Phase change is not supported within the puzzle player");
    }

    //endregion

    //region ACTIONS METHODS

    private void onNextPuzzleClick(View v) {
        loadPuzzle(puzzleSaves.get(puzzleSaves.indexOf(puzzleSave) + 1));
    }

    private void onPreviousPuzzleClick(View v) {
        loadPuzzle(puzzleSaves.get(puzzleSaves.indexOf(puzzleSave) - 1));
    }

    private void loadPuzzle(@NonNull PuzzleSave puzzleSave) {
        this.puzzleSave = puzzleSave;
        controller.restartGame(puzzleSave.getPuzzleGameHistory());
        updatePuzzleInfos();
        hidePuzzleActions();
    }

    private void onDisplayCellPositionClick(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());
        hidePuzzleActions();
    }

    private void updatePuzzleInfos() {
        String puzzleName = puzzleSave.getName();
        String authorName = puzzleSave.getAuthor();

        boolean showPuzzleName = !puzzleName.isEmpty();
        boolean showPuzzleAuthor = showPuzzleName && !authorName.isEmpty();

        txvPuzzleName.setVisibility(showPuzzleName ? View.VISIBLE : View.GONE);
        txvAuthorName.setVisibility(showPuzzleAuthor ? View.VISIBLE : View.GONE);

        txvPuzzleName.setText(puzzleName);
        txvAuthorName.setText(String.format(getString(R.string.by_author), authorName));
    }

    private void updatePuzzleActions() {
        boolean hasNextPuzzle = false;
        boolean hasPreviousPuzzle = false;
        if (puzzleSource != PuzzleSource.Editor) {
            int puzzleIdx = puzzleSaves.indexOf(puzzleSave);
            if (puzzleIdx != -1) {
                hasNextPuzzle = puzzleIdx < puzzleSaves.size() - 1;
                hasPreviousPuzzle = puzzleIdx > 0;
            }
        }

        amvPuzzleActions.setButtonEnabled(PuzzlePlayerAction.GoToNextPuzzle.ordinal(), hasNextPuzzle);
        amvPuzzleActions.setButtonEnabled(PuzzlePlayerAction.GoToPreviousPuzzle.ordinal(), hasPreviousPuzzle);
    }

    private void hidePuzzleActions() {
        amvPuzzleActions.setVisibility(View.GONE);
        vwDialogBg.setVisibility(View.GONE);
    }

    //endregion
}