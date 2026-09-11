package com.leaders.app.activities.rules.tutorial;

import android.app.AlertDialog;
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
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.rules.TutorialNavigationView;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

import java.util.Objects;

public abstract class RulesTutorialActivity extends PlayableActivity implements TutorialNavigationView.IRulesNavigation {
    private MaterialButton btnInfo;
    private TutorialNavigationView tnvNavigation;

    private TextView txvBefore;
    private TextView txvAfter;

    private MaterialButton btnReset;
    private MaterialButton btnNextChapter;

    private HighlightView hlvNextChapter;

    private GameHistory startHistory;
    private String startHistoryHash;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnInfo = findViewById(R.id.btnInfo_actRulesTutorial);
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

        btnInfo.setOnClickListener(this::onInfoClick);
        tnvNavigation.setNavigator(this);

        btnReset.setOnClickListener(this::onResetClick);
        btnNextChapter.setOnClickListener(this::onNextChapterClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        setBtnNextChapterEnabled(false);
        txvAfter.setVisibility(View.GONE);

        txvBefore.setText(getChapter().getTextBeforeResId());
        txvAfter.setText(getChapter().getTextAfterResId());

        startHistory = JsonUtils.loadTutorialChapter(this, getChapter());
        startHistoryHash = getHistoryHash(startHistory);

        controller = new GameController(this);
        controller.restartGame(new GameHistory(startHistory));
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

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorial;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region INTERACTION UI METHODS

    @Override
    protected boolean canUndoLastAction() {
        return super.canUndoLastAction() && !isStartHistory();
    }

    private boolean isStartHistory() {
        return getHistoryHash(controller.getHistory()).equals(startHistoryHash);
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
        throw new IllegalStateException("Phase change is not supported within default tutorial activies");
    }

    //endregion

    //region CHAPTER NAVIGATION

    public void goToChapter(@NonNull TutorialChapter chapter) {
        goToActivity(chapter.getActivityType(), ActivityTransitionType.Fade);
    }

    //endregion

    //region VIEW LISTENER METHODS

    private void onInfoClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);

        builder.setIcon(getChapter().getInfoIconResId());
        builder.setTitle(R.string.extra_infos);
        builder.setMessage(getChapter().getInfoTextResId());
        builder.setPositiveButton(R.string.ok, null);

        builder.show();
    }

    private void onResetClick(View v) {
        controller.restartGame(new GameHistory(startHistory));
    }

    private void onNextChapterClick(View v) {
        goToChapter(Objects.requireNonNull(getChapter().getNext(),
                "Navigation impossible: no next chapter")
        );
    }

    //endregion
}