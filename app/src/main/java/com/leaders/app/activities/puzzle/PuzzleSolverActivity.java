package com.leaders.app.activities.puzzle;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.characteraction.CharacterActionView;
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

public class PuzzleSolverActivity extends BaseActivity {
    @NonNull
    private final List<List<CharacterAction>> solutions = new ArrayList<>();

    @NonNull
    private final Object solutionsLock = new Object();

    private ReadOnlyBoardView bdvBoard;
    private LinearLayout llyActions;
    private SeekBar skbSolutions;
    private ProgressBar pgbSolutionsSearch;
    private TextView txvSolutionsFound;
    private MaterialButton btnPlayNextAction;
    private MaterialButton btnPlayPreviousAction;

    private int puzzleIdx;
    private GameHistory puzzleGameHistory;

    private ExecutorService solverExecutor;
    private Thread solutionConsumerThread;
    private CountDownLatch solverWorkersLatch;
    private BlockingQueue<List<CharacterAction>> solutionQueue;

    @Nullable
    private List<CharacterAction> displayedSolution;
    private int displayedActionIndex;

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actPuzzleSolver);
        btnPlayNextAction = findViewById(R.id.btnPlayNextAction_actPuzzleSolver);
        btnPlayPreviousAction = findViewById(R.id.btnPlayPreviousAction_actPuzzleSolver);
        llyActions = findViewById(R.id.llyActions_actPuzzleSolver);
        skbSolutions = findViewById(R.id.skbSolutions_actPuzzleSolver);
        pgbSolutionsSearch = findViewById(R.id.pgbSolutionsLoading_actPuzzleSolver);
        txvSolutionsFound = findViewById(R.id.txvSolutionsFound_actPuzzleSolver);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        skbSolutions.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                List<CharacterAction> solutionToDisplay;
                synchronized (solutionsLock) {
                    solutionToDisplay = new ArrayList<>(solutions.get(progress));
                }

                setDisplayedSolution(solutionToDisplay, false);
            }

            @Override
            public void onStartTrackingTouch(@NonNull SeekBar seekBar) {
                // Nothing to do.
            }

            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                // Nothing to do.
            }
        });

        btnPlayPreviousAction.setOnClickListener(v -> playPreviousAction());
        btnPlayNextAction.setOnClickListener(v -> playNextAction());
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        displayedSolution = null;
        displayedActionIndex = -1;

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

    @Nullable
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
                setDisplayedSolution(finalSolutionToDisplay, true);
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
            skbSolutions.setVisibility(View.GONE);
            updatePlayButtons();
        }

        pgbSolutionsSearch.setVisibility(View.GONE);
    }

    private void updateSolutions() {
        int solutionCount;

        synchronized (solutionsLock) {
            solutionCount = solutions.size();
        }

        if (solutionCount == 1) {
            txvSolutionsFound.setText(R.string.one_solution_found);
            skbSolutions.setVisibility(View.GONE);
        } else {
            txvSolutionsFound.setText(getString(R.string.x_solutions_found, solutionCount));
            skbSolutions.setMax(solutionCount - 1);
            skbSolutions.setVisibility(View.VISIBLE);
        }

        updatePlayButtons();
    }

    private void setDisplayedSolution(@NonNull List<CharacterAction> solutionToDisplay,
                                      boolean updateSeekbarProgress) {
        displayedSolution = solutionToDisplay;
        displayedActionIndex = -1;

        llyActions.removeAllViews();

        for (int i = 0; i < displayedSolution.size(); i++) {
            CharacterActionView actionView = new CharacterActionView(this, i, displayedSolution.get(i));

            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
            int verticalMargin = (int) (2 * getResources().getDisplayMetrics().density);
            layoutParams.topMargin = verticalMargin;
            layoutParams.bottomMargin = verticalMargin;

            llyActions.addView(actionView, layoutParams);
        }

        showBoardStateAtAction(-1);
        updatePlayButtons();

        if (updateSeekbarProgress) {
            int solutionIndex;
            synchronized (solutionsLock) {
                solutionIndex = solutions.indexOf(displayedSolution);
            }
            skbSolutions.setProgress(solutionIndex);
        }
    }

    private void playNextAction() {
        if (displayedSolution == null || displayedActionIndex >= displayedSolution.size() - 1) {
            return;
        }

        displayedActionIndex++;
        showBoardStateAtAction(displayedActionIndex);
        updatePlayButtons();
    }

    private void playPreviousAction() {
        if (displayedActionIndex < 0) {
            return;
        }

        displayedActionIndex--;
        showBoardStateAtAction(displayedActionIndex);
        updatePlayButtons();
    }

    /**
     * Rebuilds the displayed board from the original puzzle and applies the
     * selected prefix of the solution through the same action handlers used by
     * the game logic.
     */
    private void showBoardStateAtAction(int actionIndex) {
        if (displayedSolution == null) {
            return;
        }

        Game displayGame = GameFactory.create(puzzleGameHistory);

        int lastAction = Math.min(actionIndex, displayedSolution.size() - 1);
        for (int i = 0; i <= lastAction; i++) {
            CharacterAction action = displayedSolution.get(i);
            GameActionHandler handler =
                    GameActionHandlerFactory.create(displayGame, action);
            handler.doAction();
        }

        bdvBoard.setBoard(displayGame.getBoard());
    }

    private void updatePlayButtons() {
        ButtonUtils.setButtonEnabled(btnPlayPreviousAction,
                displayedSolution != null && displayedActionIndex >= 0);
        ButtonUtils.setButtonEnabled(btnPlayNextAction,
                displayedSolution != null && displayedActionIndex < displayedSolution.size() - 1);
    }

    private void shutdownSearch() {
        solverExecutor.shutdownNow();
        solutionConsumerThread.interrupt();
    }
}