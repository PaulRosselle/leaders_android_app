package com.leaders.app.activities.puzzle;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.PuzzleSource;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.GameHandler;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.interactions.IGameFlowListener;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionResult;
import com.leaders.gamelogic.interactions.InteractionResultType;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PuzzlePlayerActivity extends BaseActivity implements PlayableBoardView.OnTargetClickListener, IGameFlowListener {
    private MaterialButton btnPuzzleActions;
    private View vwDialogBg;

    private MaterialButton btnReset;
    private MaterialButton btnUndoLastAction;

    private CharacterNotificationView cnvCardInfo;

    private PlayableBoardView bdvBoard;


    private PuzzleSource puzzleSource;
    private List<? extends PuzzleSave> puzzleSaves;
    private PuzzleSave puzzleSave;

    private GameHandler gameHandler;
    private InteractionRequest pendingRequest;
    private CompletableFuture<InteractionResult> pendingRequestFuture;

    private boolean isCancellationAllowed;

    private final ExecutorService gameHandlerExecutor = Executors.newSingleThreadExecutor();

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

        clearInteractionUI();

        // We call runAsync to start the "game". The whenComplete code allow us to handle
        // exceptions within subsequent CompletableFuture like every other exception
        gameHandlerExecutor.execute(() -> {
            gameHandler = new GameHandler(puzzleGameHistory, this);
            gameHandler.runAsync().whenComplete((result, throwable) -> {
                if (throwable != null) {
                    Thread thread = Thread.currentThread();
                    Thread.UncaughtExceptionHandler exceptionHandler = thread.getUncaughtExceptionHandler();
                    if (exceptionHandler != null) {
                        exceptionHandler.uncaughtException(thread, throwable);
                    }
                }
            });;
        });
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
        // TODO
    }

    private void onUndoLastAction(View v) {
        // TODO
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
        onEmptyClick();
    }

    //endregion

    //region TARGET CLICK LISTENER METHODS

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        if (pendingRequest == null || pendingRequestFuture == null) {
            throw new IllegalStateException("Targets should not exist outside of a valid request context");
        }

        if (!isLegalTarget(target)) {
            throw new IllegalArgumentException("Invalid target :" + target);
        }

        completeInteraction(getTargetResult(pendingRequest, target));
    }

    @Override
    public void onEmptyClick() {
        if (!isCancellationAllowed || pendingRequest == null || pendingRequestFuture == null) {
            return;
        }

        completeInteraction(getCancelResult(pendingRequest));
    }

    //endregion

    //region INTERACTION METHODS

    private void updateInteractionUI(@NonNull InteractionRequest request) {
        List<InteractionResultType> legalResults = request.getLegalResults();
        isCancellationAllowed = legalResults.contains(InteractionResultType.CancelAction);

        bdvBoard.applyTargets(
                request.getLegalTargets(),
                request.getContext(),
                gameHandler.getCurrentGame().getBoard()
        );

        btnUndoLastAction.setEnabled(legalResults.contains(InteractionResultType.UndoLastAction));
    }

    private void clearInteractionUI() {
        isCancellationAllowed = false;

        bdvBoard.clearTargets();

        ButtonUtils.setButtonEnabled(btnUndoLastAction, false);
    }

    private void completeInteraction(@NonNull InteractionResult result) {
        if (pendingRequestFuture == null || pendingRequestFuture.isDone()) {
            return;
        }

        CompletableFuture<InteractionResult> future = pendingRequestFuture;

        pendingRequest = null;
        pendingRequestFuture = null;

        clearInteractionUI();

        future.complete(result);
    }

    private boolean isLegalTarget(@NonNull InteractionTarget target) {
        return pendingRequest.getLegalTargets().contains(target);
    }

    private InteractionResult getTargetResult(@NonNull InteractionRequest request,
                                              @NonNull InteractionTarget target) {
        return new InteractionResult(
                target.getCategory().getResultType(),
                request.getContext(),
                target
        );
    }

    private InteractionResult getCancelResult(@NonNull InteractionRequest request) {
        return new InteractionResult(
                InteractionResultType.CancelAction,
                request.getContext(),
                null
        );
    }

    //endregion

    //region GAME FLOW LISTENERS

    @NonNull
    @Override
    public CompletableFuture<Void> onGameStarted(@NonNull Game game) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        runOnUiThread(() -> {
            bdvBoard.setBoard(game.getBoard());
            future.complete(null);
        });

        return future;
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onGameEnded(@NonNull Player winner) {
        // TODO - handle game end
        return CompletableFuture.completedFuture(null);
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onPhaseChanged(@NonNull GamePhase phase) {
        throw new IllegalStateException("Phase change is not supported within the puzzle player");
    }

    @NonNull
    @Override
    public CompletableFuture<InteractionResult> onInputRequired(@NonNull InteractionRequest request) {
        pendingRequest = request;
        pendingRequestFuture = new CompletableFuture<>();

        runOnUiThread(() -> updateInteractionUI(request));

        return pendingRequestFuture;
    }

    @NonNull
    @Override
    public CompletableFuture<Void> onFeedback(@NonNull InteractionFeedback feedback) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        runOnUiThread(() -> bdvBoard.animateFeedback(feedback, () -> future.complete(null)));

        return future;
    }

    //endregion

    @Override
    protected void onDestroy() {
        gameHandlerExecutor.shutdownNow();
        super.onDestroy();
    }
}