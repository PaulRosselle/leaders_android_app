package com.leaders.app.activities.puzzle;

import android.content.Intent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.characteraction.CharacterActionTimelineView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameActionHandlerFactory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.handlers.GameActionHandler;
import com.leaders.gamelogic.historyentries.IPhase;
import com.leaders.gamelogic.historyentries.segments.ActionsPhase;
import com.leaders.gamelogic.queries.GameHistoryQuery;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;
import com.leaders.puzzlelogic.utilities.solver.PuzzleSolverUtils;
import com.leaders.puzzlelogic.utilities.solver.SolutionComparatorUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class PuzzleSolverActivity extends BaseActivity {
    @NonNull
    private final List<List<CharacterAction>> solutions = new ArrayList<>();

    @NonNull
    private final Object solutionsLock = new Object();

    private ReadOnlyBoardView bdvBoard;
    private ProgressBar pgbSolutionsSearch;
    private TextView txvSolutionsFound;
    private MaterialButton btnPreviousSolution;
    private MaterialButton btnNextSolution;
    private CharacterActionTimelineView catvActions;
    private HorizontalScrollView scvActions;
    private MaterialButton btnPuzzleActions;
    private ActionsMenuView amvPuzzleActions;
    private View vwDialogBg;

    private int puzzleIdx;
    private GameHistory puzzleGameHistory;

    private ExecutorService solverExecutor;
    private Thread solutionConsumerThread;
    private CountDownLatch solverWorkersLatch;
    private BlockingQueue<List<CharacterAction>> solutionQueue;

    @Nullable
    private List<CharacterAction> displayedSolution;

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actPuzzleSolver);
        btnPreviousSolution = findViewById(R.id.btnPreviousSolution_actPuzzleSolver);
        btnNextSolution = findViewById(R.id.btnNextSolution_actPuzzleSolver);
        pgbSolutionsSearch = findViewById(R.id.pgbSolutionsLoading_actPuzzleSolver);
        txvSolutionsFound = findViewById(R.id.txvSolutionsFound_actPuzzleSolver);
        catvActions = findViewById(R.id.catvActions_actPuzzleSolver);
        scvActions = findViewById(R.id.scvActions_actPuzzleSolver);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleSolver);
        amvPuzzleActions = findViewById(R.id.amvPuzzleActions_actPuzzleSolver);
        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzleSolver);

        amvPuzzleActions.addActionButton(R.drawable.icon_position, R.string.board_coordinates, 0, this::btnDisplayCellPosition);
    }

    @Override
    protected void initListeners() {
        super.initListeners();


        // Puzzle actions listeners
        btnPuzzleActions.setOnClickListener(v ->
                setActionsMenuVisible(amvPuzzleActions.getVisibility() != View.VISIBLE));
        vwDialogBg.setOnClickListener(this::vwDialogBgClick);

        catvActions.setOnMarkerSelectedListener(this::onActionTimelineMarkerSelect);

        btnNextSolution.setOnClickListener(this::btnChangeDisplayedSolutionClick);
        btnPreviousSolution.setOnClickListener(this::btnChangeDisplayedSolutionClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        displayedSolution = null;

        puzzleIdx = getIntent().getIntExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, -1);

        String datas = getIntent().getStringExtra(ExtraUtils.EXTRA_PUZZLE_DATAS);
        if (datas == null || datas.isEmpty()) {
            throw new IllegalStateException("Puzzle solver requires puzzle history data");
        }

        try {
            JSONObject joGameHistory = new JSONObject(datas);
            GameHistorySerializer serializer = new GameHistorySerializer();
            puzzleGameHistory = serializer.getFromJson(
                    joGameHistory,
                    new SerializationContext()
            );
        } catch (JSONException e) {
            throw new RuntimeException("Unable to deserialize puzzle history", e);
        }

        Game initialGame = GameFactory.create(puzzleGameHistory);
        bdvBoard.post(() -> bdvBoard.setBoard(initialGame.getBoard()));

        searchSolutions();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_solver;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzleSolver;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzleSolver;
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
        return ActivityType.PuzzleSolver;
    }

    @Override
    protected void doOnBackPressed() {
        shutdownSearch();

        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);

        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            intent.putExtra(
                    ExtraUtils.EXTRA_PUZZLE_DATAS,
                    serializer.getAsJson(puzzleGameHistory).toString()
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        goToActivity(intent, ActivityTransitionType.SlideLeft);
    }

    @Override
    protected void onDestroy() {
        shutdownSearch();
        super.onDestroy();
    }

    /**
     * Starts the parallel search and consumes solutions as they are discovered.
     *
     * <p>The solver works on permutations of the characters belonging to the
     * active team. Each worker gets its own Game and GameHistory copy because
     * PuzzleSolverUtils mutates both objects while exploring a branch.</p>
     */
    private void searchSolutions() {
        Game baseGame = GameFactory.create(puzzleGameHistory);

        IPhase currentPhase = GameHistoryQuery.findCurrentPhase(puzzleGameHistory);
        if (!(currentPhase instanceof ActionsPhase)) {
            throw new IllegalStateException("Puzzle solver requires an ActionsPhase");
        }

        TeamColor activeTeam = ((ActionsPhase) currentPhase).getTurnTeamColor();
        List<Character> characters = getActiveTeamCharacters(baseGame, activeTeam);

        long permutationCount = PuzzleSolverUtils.getPermutationCount(characters);
        if (permutationCount == 0) {
            throw new IllegalStateException("Invalid puzzle : no playable character");
        }

        int workerCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        workerCount = (int) Math.min(workerCount, permutationCount);

        solutionQueue = new LinkedBlockingQueue<>();
        solverWorkersLatch = new CountDownLatch(workerCount);
        solverExecutor = Executors.newFixedThreadPool(workerCount);

        pgbSolutionsSearch.setVisibility(View.VISIBLE);
        txvSolutionsFound.setText(R.string.searching_solution);

        // nextPermutation is incremented within "runSolverWorker"
        AtomicLong nextPermutation = new AtomicLong(0);
        for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
            // We assign a part of the search to each thread in the "solverExecutor" pool
            solverExecutor.execute(() -> runSolverWorker(
                    baseGame,
                    characters,
                    permutationCount,
                    nextPermutation
            ));
        }

        solutionConsumerThread = new Thread(this::consumeSolutions, "PuzzleSolverConsumer");
        solutionConsumerThread.start();
    }

    @NonNull
    private List<Character> getActiveTeamCharacters(@NonNull Game game,
                                                    @NonNull TeamColor activeTeam) {
        List<Character> characters = new ArrayList<>();

        for (Cell cell : game.getBoard().getCells().values()) {
            Character character = cell.getCharacter();
            // Nemesis cannot use her action normally and is therefore not included within
            // the active team characters (she will still react to an enemy leader movement)
            if (character != null && character.getTeamColor() == activeTeam &&
                character.getCharacterType() != CharacterType.Nemesis) {
                characters.add(character);
            }
        }

        return characters;
    }

    private void runSolverWorker(@NonNull Game baseGame,
                                 @NonNull List<Character> characters,
                                 long permutationCount,
                                 @NonNull AtomicLong nextPermutation) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                long permutationIndex = nextPermutation.getAndIncrement();
                if (permutationIndex >= permutationCount) {
                    break;
                }

                // Each worker must use its own copy of the game history and projection since
                // the solver simulates action then backtrack to explore every possible action
                Game workerGame = new Game(baseGame);
                GameHistory workerHistory = new GameHistory(puzzleGameHistory);

                PuzzleSolverUtils.solve(
                        workerGame,
                        workerHistory,
                        characters,
                        permutationIndex,
                        solutionQueue
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            solverWorkersLatch.countDown();
        }
    }

    private void consumeSolutions() {
        try {
            while (solverWorkersLatch.getCount() > 0 || !solutionQueue.isEmpty()) {
                List<CharacterAction> solution = solutionQueue.poll(100, TimeUnit.MILLISECONDS);

                if (solution != null) {
                    addSolution(solution);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            solverExecutor.shutdown();
            runOnUiThread(this::finishSolutionSearch);
        }
    }

    private void addSolution(@NonNull List<CharacterAction> newSolution) {
        List<CharacterAction> solutionToDisplay = null;

        synchronized (solutionsLock) {
            List<List<CharacterAction>> solutionsToRemove = new ArrayList<>();

            for (List<CharacterAction> existingSolution : solutions) {
                SolutionComparatorUtils.SolutionCompareValue comparison =
                        SolutionComparatorUtils.compareSolutions(
                                newSolution,
                                existingSolution
                        );

                // If an identical or better solution already exist, we don't add this one
                if (comparison == SolutionComparatorUtils.SolutionCompareValue.StructurallyEqual ||
                        comparison == SolutionComparatorUtils.SolutionCompareValue.SecondIsBetter) {
                    return;
                }

                // If worst version of the new solution exist, we will remove them when adding it
                if (comparison == SolutionComparatorUtils.SolutionCompareValue.FirstIsBetter) {
                    solutionsToRemove.add(existingSolution);
                }
            }

            // We display the new solution in two scenarios :
            // 1. The new solution is the first one
            // 2. The displayed solution will be replaced by the new one
            if (solutions.isEmpty() ||
                    (displayedSolution != null && solutionsToRemove.contains(displayedSolution))) {
                solutionToDisplay = newSolution;
            }

            solutions.removeAll(solutionsToRemove);
            solutions.add(newSolution);
        }

        List<CharacterAction> finalSolutionToDisplay = solutionToDisplay;
        runOnUiThread(() -> {
            updateSolutions();
            if (finalSolutionToDisplay != null) {
                setDisplayedSolution(finalSolutionToDisplay);
            }
        });
    }

    private void finishSolutionSearch() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        boolean hasNoSolution;
        synchronized (solutionsLock) {
            hasNoSolution = solutions.isEmpty();
        }

        // If the search ended without finding a solution, we update the search display
        if (hasNoSolution) {
            txvSolutionsFound.setText(R.string.no_solution_found);
            updateSolutionButtons();
        }

        pgbSolutionsSearch.setVisibility(View.GONE);
    }

    private void updateSolutions() {
        updateSolutionTextView();
        updateSolutionButtons();
    }

    private void setDisplayedSolution(@NonNull List<CharacterAction> solutionToDisplay) {
        displayedSolution = solutionToDisplay;

        catvActions.setActions(displayedSolution);

        showBoardState(0);

        catvActions.post(this::updateActionsScrollView);
        updateSolutionTextView();
        updateSolutionButtons();
    }


    private void setActionsMenuVisible(boolean visible) {
        amvPuzzleActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void btnChangeDisplayedSolutionClick(View v) {
        int incValue = v == btnNextSolution ? 1 : -1;

        List<CharacterAction> previousSolution;
        synchronized (solutionsLock) {
            previousSolution = solutions.get(solutions.indexOf(displayedSolution) + incValue);
        }

        setDisplayedSolution(previousSolution);
    }

    private void btnDisplayCellPosition(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());

        setActionsMenuVisible(false);
    }

    private void vwDialogBgClick(View v) {
        setActionsMenuVisible(false);
    }

    private void updateSolutionTextView() {
        int solutionIndex;
        int solutionCount;

        synchronized (solutionsLock) {
            solutionIndex = solutions.indexOf(displayedSolution) + 1;
            solutionCount = solutions.size();
        }

        if (solutionCount == 0) {
            throw new IllegalStateException("Solution textView updated without solution");
        }

        if (solutionCount == 1) {
            txvSolutionsFound.setText(R.string.one_solution_found);
        } else {
            txvSolutionsFound.setText(getString(R.string.solution_x_out_of_n, solutionIndex, solutionCount));
        }
    }

    private void updateSolutionButtons() {
        int solutionIndex;
        int solutionsCount;
        synchronized (solutionsLock) {
            solutionIndex = solutions.indexOf(displayedSolution);
            solutionsCount = solutions.size();
        }

        boolean hasSolutions = solutionsCount > 1;
        btnNextSolution.setVisibility(hasSolutions ? View.VISIBLE : View.INVISIBLE);
        btnPreviousSolution.setVisibility(hasSolutions ? View.VISIBLE : View.INVISIBLE);

        if (hasSolutions) {
            ButtonUtils.setButtonEnabled(btnNextSolution, solutionIndex < solutionsCount - 1);
            ButtonUtils.setButtonEnabled(btnPreviousSolution, solutionIndex > 0);
        }
    }

    private void shutdownSearch() {
        solverExecutor.shutdownNow();
        solutionConsumerThread.interrupt();
    }


    /**
     * Displays the board state resulting from applying the displayed solution up
     * to the specified timeline marker.
     *
     * @param timelineMarkerIndex the number of actions to apply, in
     *                            {@code [0, displayedSolution.size()]}
     * @throws IllegalStateException if no solution is available to display
     * @throws IllegalArgumentException if {@code timelineMarkerIndex} is outside
     *                                  the valid range
     */
    private void showBoardState(int timelineMarkerIndex) {
        if (displayedSolution == null) {
            throw new IllegalStateException("No solution found matching actions timeline");
        }

        if (timelineMarkerIndex < 0 || timelineMarkerIndex > displayedSolution.size()) {
            throw new IllegalArgumentException("Invalid timeline marker index: " + timelineMarkerIndex);
        }

        Game displayGame = GameFactory.create(puzzleGameHistory);

        for (int i = 0; i < timelineMarkerIndex; i++) {
            CharacterAction action = displayedSolution.get(i);
            GameActionHandler handler = GameActionHandlerFactory.create(displayGame, action);
            handler.doAction();
        }

        bdvBoard.setBoard(displayGame.getBoard());
    }

    private void onActionTimelineMarkerSelect(int markerIndex) {
        showBoardState(markerIndex);
    }

    private void updateActionsScrollView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) scvActions.getLayoutParams();

        int availableWidth = scvActions.getMeasuredWidth();
        if (availableWidth <= 0) {
            scvActions.post(this::updateActionsScrollView);
            return;
        }

        int timelineWidth = catvActions.getMeasuredWidth();

        if (timelineWidth <= availableWidth) {
            // The whole timeline fits on screen.
            // Let the ScrollView have its natural width and center it.
            params.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        } else {
            // The timeline is wider than the screen.
            // Make the ScrollView fill the available width so it can scroll.
            params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;

        }

        scvActions.setLayoutParams(params);
    }
}