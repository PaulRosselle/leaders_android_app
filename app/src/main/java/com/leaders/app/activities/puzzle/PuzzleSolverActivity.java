package com.leaders.app.activities.puzzle;

import android.content.Intent;
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
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PuzzleSolverActivity extends BaseActivity {
    private ReadOnlyBoardView bdvBoard;
    private LinearLayout llyActions;
    private SeekBar skbSolutions;
    private ProgressBar pgbSolutionsSearch;
    private TextView txvSolutionsFound;
    private MaterialButton btnPlayNextAction, btnPlayPreviousAction;
    private int puzzleIdx;
    private GameHistory puzzleGameHistory;

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
        // TODO
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        // First, we recover intent datas
        puzzleIdx = getIntent().getIntExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, -1);
        try {
            String datas = getIntent().getStringExtra(ExtraUtils.EXTRA_PUZZLE_DATAS);
            JSONObject joGameHistory = new JSONObject(datas);
            GameHistorySerializer serializer = new GameHistorySerializer();
            puzzleGameHistory = serializer.getFromJson(joGameHistory, new SerializationContext());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        bdvBoard.post(() -> bdvBoard.setBoard(GameFactory.create(puzzleGameHistory).getBoard()));
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
        Intent intent = ActivityType.PuzzleEditor.getIntent(this);
        intent.putExtra(ExtraUtils.EXTRA_PUZZLE_INDEX, puzzleIdx);
        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            intent.putExtra(ExtraUtils.EXTRA_PUZZLE_DATAS, serializer.getAsJson(puzzleGameHistory).toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        goToActivity(intent, ActivityTransitionType.SlideLeft);
    }
}