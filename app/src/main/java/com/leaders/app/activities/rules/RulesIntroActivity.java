package com.leaders.app.activities.rules;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.entities.replay.ReplayTimelineController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.RulesChapter;
import com.leaders.app.utilities.GameActionUtils;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.rules.RulesPlayButton;
import com.leaders.app.views.rules.RulesQuestionButton;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import java.util.ArrayList;

public final class RulesIntroActivity extends RulesActivity {
    private final static int EXAMPLE_REPLAY_END_DELAY = 400; // ms

    private enum DisplayState {
        Start,
        Example
    }

    private RulesPlayButton btnPlay;
    private RulesQuestionButton btnNextStep;
    private TextView txbAboveBoard;
    private TextView txvBelowBoard;
    private ReadOnlyBoardView bdvBoard;

    private Board startBoard;
    private ReplayTimelineController replayController;

    private DisplayState displayState;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnPlay = findViewById(R.id.btnPlay_actRulesIntro);
        btnNextStep = findViewById(R.id.btnNextStep_actRUlesIntro);

        txbAboveBoard = findViewById(R.id.txvAboveBoard_actRulesIntro);
        txvBelowBoard = findViewById(R.id.txvBelowBoard_actRulesIntro);

        bdvBoard = findViewById(R.id.bdvBoard_actRulesIntro);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnPlay.setOnClickListener(this::onPlayClick);
        btnNextStep.setOnClickListener(this::onNextStepClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        // TODO - load intro game
        GameHistory demoHistory = PuzzleEditionUtils.getDefaultHistory();
        GameHistory startHistory = new GameHistory(demoHistory.getConfig(), new ArrayList<>());

        startBoard = GameFactory.create(startHistory).getBoard();
        replayController = new ReplayTimelineController(demoHistory);

        bdvBoard.post(() -> setDisplayState(DisplayState.Start));
    }

    @Override
    protected int getRnvNavigationResId() {
        return R.id.rnvNavigation_actRulesIntro;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_intro;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesIntro;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesIntro;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesIntro;
    }

    @Override
    public RulesChapter getChapter() {
        return RulesChapter.Introduction;
    }

    //endregion

    //region VIEW LISTENER METHODS

    private void onPlayClick(View v) {
        btnPlay.setVisibility(View.GONE);
        bdvBoard.setBoard(startBoard);

        replayController.reset();
        playNextAction();
    }

    private void playNextAction() {
        if (!replayController.hasNextAction()) {
            endPlay();
            return;
        }

        IGameAction action = replayController.moveToNextAction();
        GameActionUtils.animate(bdvBoard, action, this::playNextAction, AnimationSpeed.Slow);
    }

    private void endPlay() {
        btnPlay.setIconResource(R.drawable.icon_restart);
        (new Handler(Looper.getMainLooper())).postDelayed(
                () -> btnPlay.setVisibility(View.VISIBLE),
                EXAMPLE_REPLAY_END_DELAY
        );
    }

    private void onNextStepClick(View v) {
        if (displayState == DisplayState.Start) {
            setDisplayState(DisplayState.Example);
            return;
        }

        // TODO - replace Main par RulesGameEnd when its implemented
        goToActivity(ActivityType.Main, ActivityTransitionType.Fade);
    }

    //endregion

    //region DISPLAY UPDATE

    private void setDisplayState(@NonNull DisplayState displayState) {
        this.displayState = displayState;

        switch (displayState) {
            case Start: updateDisplayStart(); break;
            case Example: updateDisplayExample(); break;
            default:  throw new IllegalStateException("Display state not handled: " + displayState);
        }
    }

    private void updateDisplayStart() {
        txbAboveBoard.setText(R.string.rules_intro_principle);
        txvBelowBoard.setText(R.string.rules_intro_game_start);

        btnPlay.setVisibility(View.GONE);
        btnNextStep.setText(R.string.what_is_next);

        bdvBoard.setBoard(startBoard);
    }

    private void updateDisplayExample() {
        txbAboveBoard.setText(R.string.rules_intro_example_above);
        txvBelowBoard.setText(R.string.rules_intro_example_below);

        btnPlay.setVisibility(View.VISIBLE);
        btnNextStep.setText(R.string.how_to_block_a_leader);

        bdvBoard.setBoard(startBoard);
    }

    //endregion
}