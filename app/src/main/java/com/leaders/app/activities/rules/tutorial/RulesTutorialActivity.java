package com.leaders.app.activities.rules.tutorial;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.rules.TutorialNavigationView;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import org.json.JSONException;

public final class RulesTutorialActivity extends PlayableActivity implements TutorialNavigationView.IRulesNavigation {
    private TutorialNavigationView tnvNavigation;

    private TextView txvBefore;
    private TextView txvAfter;

    private MaterialButton btnReset;
    private MaterialButton btnNextChapter;

    private HighlightView hlvNextChapter;

    private TutorialChapter chapter;

    private GameHistory chapterHistory;
    private String chapterHistoryHash;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        tnvNavigation = findViewById(R.id.tnvNavigation_actRulesTutoriel);

        txvBefore = findViewById(R.id.txvBefore_actRulesTutorial);
        txvAfter = findViewById(R.id.txvAfter_actRulesTutorial);

        btnReset = findViewById(R.id.btnReset_actRulesTutorial);
        btnNextChapter = findViewById(R.id.btnNextChapter_actRulesTutorial);

        hlvNextChapter = findViewById(R.id.hlvNextChapter_actRulesTutorial);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actRulesTutorial)).setOnClickListener(this::onNonInteractiveElementClick);

        btnReset.setOnClickListener(this::onResetClick);
        btnNextChapter.setOnClickListener(this::onNextChapterClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        setBtnNextChapterEnabled(false);

        TutorialChapter intentChapter = TutorialChapter.valueOf(
                getIntent().getStringExtra(ExtraUtils.EXTRA_TUTORIAL_CHAPTER)
        );

        controller = new GameController(this);
        loadChapter(intentChapter);
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesTutorial;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesTutorial;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesTutorial;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesTutorial;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_tutorial;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesTutorial;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorial;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorial;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region INTERACTION UI METHODS

    @Override
    protected boolean canUndoLastAction() {
        return super.canUndoLastAction() && !getChapterHistory();
    }

    private boolean getChapterHistory() {
        return getHistoryHash(controller.getHistory()).equals(chapterHistoryHash);
    }

    private String getHistoryHash(@NonNull GameHistory gameHistory) {
        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            return serializer.getAsJson(gameHistory).toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void showEndGame(@NonNull Player winner) {
        GameContext gameContext = controller.getCurrentContext();

        showEndGame(gameContext,
                winner.getTeamColor(),
                EndGameType.Victory,
                getString(R.string.victory_title),
                getString(R.string.victory_subtitle)
        );
    }

    private void setBtnNextChapterEnabled(boolean enabled) {
        ButtonUtils.setEnabled(btnNextChapter, enabled);
        hlvNextChapter.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (enabled) {
            hlvNextChapter.startAnimation();
        } else {
            hlvNextChapter.stopAnimation();
        }
    }

    //endregion

    //region GAME CONTROLLER LISTENER METHODS

    @Override
    public void onGameEnded(@NonNull Player winner) {
        runOnUiThread(() -> {
            showEndGame(winner);

            txvAfter.setVisibility(View.VISIBLE);
            setBtnNextChapterEnabled(true);
        });
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        // TODO - handle recruitment
    }

    //endregion

    //region CHAPTER LOADING

    public TutorialChapter getChapter() {
        return chapter;
    }

    public void loadChapter(@NonNull TutorialChapter chapter) {
        this.chapter = chapter;

        tnvNavigation.setNavigator(this);

        setBtnNextChapterEnabled(false);

        txvBefore.setText(chapter.getTextBeforeResId());
        txvAfter.setText(chapter.getTextAfterResId());

        txvAfter.setVisibility(View.GONE);

        // TODO - load tutorial history from Json
        chapterHistory = PuzzleEditionUtils.getDefaultHistory();
        chapterHistoryHash = getHistoryHash(chapterHistory);

        controller.restartGame(new GameHistory(chapterHistory));
    }

    //endregion

    //region VIEW LISTENER METHODS

    private void onResetClick(View v) {
        controller.restartGame(new GameHistory(chapterHistory));
    }

    private void onNextChapterClick(View v) {
        TutorialChapter nextChapter = chapter.getNext();
        if (nextChapter != null) {
            loadChapter(nextChapter);
        } else {
            // The last chapter leads to the character demo menu
            goToActivity(ActivityType.RulesCharacterMenu);
        }
    }

    //endregion
}