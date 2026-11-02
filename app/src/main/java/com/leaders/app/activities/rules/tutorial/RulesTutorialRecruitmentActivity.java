package com.leaders.app.activities.rules.tutorial;

import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.CardSelectionActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.rules.TutorialNavigationView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.Objects;

public class RulesTutorialRecruitmentActivity extends CardSelectionActivity implements
        TutorialNavigationView.IRulesNavigation {

    private MaterialButton btnInfo;
    private TutorialNavigationView tnvNavigation;

    private TextView txvBefore;
    private TextView txvAfter;

    private MaterialButton btnReset;
    private MaterialButton btnNextChapter;

    private HighlightView hlvNextChapter;

    private GameHistory startHistory;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected int getMainLayoutId() {
        return R.id.clyMain_actRulesTutorialRecruitment;
    }

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard.setAlwaysUseDefaultSkins(true);

        btnInfo = findViewById(R.id.btnInfo_actRulesTutorialRecruitment);
        tnvNavigation = findViewById(R.id.tnvNavigation_actRulesTutorielRecruitment);

        txvBefore = findViewById(R.id.txvBefore_actRulesTutorialRecruitment);
        txvAfter = findViewById(R.id.txvAfter_actRulesTutorialRecruitment);

        btnReset = findViewById(R.id.btnReset_actRulesTutorialRecruitment);
        btnNextChapter = findViewById(R.id.btnNextChapter_actRulesTutorialRecruitment);

        hlvNextChapter = findViewById(R.id.hlvNextChapter_actRulesTutorialRecruitment);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

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

        controller = new GameController(this);
        bdvBoard.post(() -> controller.restartGame(new GameHistory(startHistory)));
    }

    @Override
    protected int getCardSelectorViewId() {
        return R.id.ccsvCardSelector_actRulesTutorialRecruitment;
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesTutorialRecruitment;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesTutorialRecruitment;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesTutorialRecruitment;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesTutorialRecruitment;
    }

    @Override
    protected CharacterSkins getCharacterSkins() {
        return new CharacterSkins();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_tutorial_recruitment;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesTutorialRecruitment;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorialRecruitment;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialRecruitment;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region RECRUITMENT ACTIVITY OVERRIDEN METHODS

    /** Characters always use their default skin in tutorials. */
    @NonNull
    @Override
    protected CharacterSkinType getNewCharacterSkin(@NonNull Character character) {
        return CharacterSkinType.Default;
    }

    @Override
    protected void onRecruitmentPlacementNotRequested(@NonNull GameContext gameContext,
                                                      @NonNull InteractionRequest request) {
        if (hasRecruitedCharacter(gameContext)) {
            showSecondRecruitmentDialog();
        }
    }

    //endregion

    //region INTERACTION METHODS

    private boolean hasRecruitedCharacter(@NonNull GameContext gameContext) {
        return BoardQuery.getRecruitmentCells(gameContext.getBoard(), TeamColor.Black).size() < 7;
    }

    private void showSecondRecruitmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
        builder.setTitle(R.string.new_rule);
        builder.setMessage(R.string.second_player_recruitment);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }

    @Override
    public void onBanishmentCardSelected() {
        throw new IllegalStateException("Banishment is not supported within recruitment tutorial activity");
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request, @NonNull GameContext context) {
        // Playable character action requests are ignored in the activity
        if (request.getRequestType() == InteractionType.PlayableCharacterExpected) {
            return;
        }

        super.onInteractionRequired(request, context);
    }

    //endregion

    //region UI STATE METHODS

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
    public void onGameStarted(@NonNull GameContext context) {
        runOnUiThread(() -> {
            bdvBoard.setBoard(context.getBoard());
            ccsvCardSelector.applyGameModeParams(context.getGameMode());

            clearInteractionUI(context);
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner, @NonNull GameContext context) {
        throw new IllegalStateException("End game is not supported within recruitment tutorial activity");
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase, @NonNull GameContext context) {
        runOnUiThread(() -> {
            ccsvCardSelector.setPortraitsVisible(false);
            txvAfter.setVisibility(View.VISIBLE);
            setBtnNextChapterEnabled(true);
        });
    }

    //endregion

    //region CHAPTER NAVIGATION

    @Override
    public TutorialChapter getChapter() {
        return TutorialChapter.Recruitment;
    }

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
        ccsvCardSelector.setPortraitsVisible(true);
        txvAfter.setVisibility(View.GONE);
        controller.restartGame(new GameHistory(startHistory));
    }

    private void onNextChapterClick(View v) {
        goToChapter(Objects.requireNonNull(getChapter().getNext(),
                "Navigation impossible: no next chapter")
        );
    }

    //endregion
}