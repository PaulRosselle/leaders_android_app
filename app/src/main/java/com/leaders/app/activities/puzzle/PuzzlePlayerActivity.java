package com.leaders.app.activities.puzzle;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.controllers.PuzzlePlayerController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.PuzzleSource;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.interactions.InteractionContext;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public final class PuzzlePlayerActivity extends BaseActivity
        implements PlayableBoardView.OnTargetClickListener, PuzzlePlayerController.Listener {
    private MaterialButton btnPuzzleActions;
    private View vwDialogBg;

    private MaterialButton btnReset;
    private MaterialButton btnUndoLastAction;

    private CharacterNotificationView cnvCardInfo;

    private PlayableBoardView bdvBoard;


    private PuzzleSource puzzleSource;
    private List<? extends PuzzleSave> puzzleSaves;
    private PuzzleSave puzzleSave;


    private PuzzlePlayerController controller;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzlePlayer);
        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzlePlayer);

        btnReset = findViewById(R.id.btnReset_actPuzzlePlayer);
        btnUndoLastAction = findViewById(R.id.btnUndoLastAction_actPuzzlePlayer);

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzlePlayer);

        bdvBoard = findViewById(R.id.bdvBoard_actPuzzlePlayer);
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
        btnUndoLastAction.setOnClickListener(this::onUndoLastAction);

        cnvCardInfo.setOnClickListener(this::onCardInfoClick);

        // Board element listeners
        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);
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


        controller = new PuzzlePlayerController(this);
        controller.startGame(puzzleGameHistory);
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
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
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
            goToActivity(ActivityType.PuzzleSelection, ActivityTransitionType.SlideLeft);
        }
    }

    //endregion

    //region VIEWS CLICK LISTENER METHODS

    private void onResetClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.new_attempt);
        builder.setMessage(R.string.restart_puzzle);
        builder.setPositiveButton(R.string.start_over, (dialogInterface, i) -> {
            controller.restartGame(puzzleSave.getPuzzleGameHistory());
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void onUndoLastAction(View v) {
        controller.undoLastAction();
    }

    private void onDialogBgClick(View v) {
        // TODO
    }

    private void onCardInfoClick(View v) {
        cnvCardInfo.hide();
    }

    private void onPuzzleActionsClick(View v) {
        // TODO
    }

    private boolean onCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        CharacterCard characterCard = characterType.getCharacterCard();

        if (cnvCardInfo.getCharacterCard() == characterCard) {
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(characterCard);
            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
        }

        return false;
    }

    private void onNonInteractiveElementClick(View v) {
        controller.cancelAction();
    }

    //endregion

    //region TARGET CLICK LISTENER METHODS

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }

    @Override
    public void onEmptyClick() {
        controller.cancelAction();
    }

    private void clearInteractionUI() {
        bdvBoard.clearTargets();
        ButtonUtils.setEnabled(btnUndoLastAction, false);
    }

    private void updatePlayableCharacters(@NonNull InteractionContext context) {
        bdvBoard.highlightPlayableCharacters(
                controller.getPlayableCharacters(),
                context.getCharacter(),
                controller.getCurrentGame().getBoard()
        );
    }

    //endregion

    //region CONTROLLER METHODS

    @Override
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            clearInteractionUI();
            bdvBoard.setBoard(game.getBoard());
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        runOnUiThread(() -> {
            // TODO afficher la victoire
        });
    }

    @Override
    public void onActionUndone(@NonNull Game game) {
        runOnUiThread(() -> bdvBoard.setBoard(game.getBoard()));
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {

        runOnUiThread(() -> {
            bdvBoard.applyTargets(
                    request.getLegalTargets(),
                    request.getContext(),
                    controller.getCurrentGame().getBoard()
            );

            updatePlayableCharacters(request.getContext());

            ButtonUtils.setEnabled(btnUndoLastAction, controller.canUndoLastAction());
        });
    }

    @Override
    public void onFeedback(@NonNull InteractionFeedback feedback,
                           @NonNull PuzzlePlayerController.InteractionCompletion completion) {
        runOnUiThread(() -> bdvBoard.animateFeedback(feedback, completion::complete));
    }

    @Override
    public void onInteractionCleared() {
        runOnUiThread(this::clearInteractionUI);
    }

    //endregion

    @Override
    protected void onDestroy() {
        if (controller != null) {
            controller.shutdown();
        }

        super.onDestroy();
    }
}