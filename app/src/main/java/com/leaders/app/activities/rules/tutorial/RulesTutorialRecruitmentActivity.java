package com.leaders.app.activities.rules.tutorial;

import android.app.AlertDialog;
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
import com.leaders.app.enums.TutorialChapter;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.app.views.rules.TutorialNavigationView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.Objects;

public class RulesTutorialRecruitmentActivity extends PlayableActivity implements
        TutorialNavigationView.IRulesNavigation,
        CharacterCardSelectionView.OnCardSelectedListener{
    private CharacterCardSelectionView ccsvCardSelector;
    private CharacterDisplay chdNewCharacter;

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
    protected void initViews() {
        super.initViews();

        ccsvCardSelector = findViewById(R.id.ccsvCardSelector_actRulesTutorialRecruitment);
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);

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

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actRulesTutorialRecruitment)).setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnScrollViewClickListener(this::onNonInteractiveElementClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

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
        controller.restartGame(new GameHistory(startHistory));
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

    //region INTERACTION METHODS

    @Override
    protected void clearInteractionUI(@NonNull GameContext gameContext) {
        super.clearInteractionUI(gameContext);

        ccsvCardSelector.applyCards(gameContext.getAvailableCharacterCards());

        ccsvCardSelector.setPortraitsVisible(true);
        setNewCharacterVisible(null, false);
    }

    @Override
    protected void applyInteractionTargets(@NonNull GameContext gameContext,
                                           @NonNull InteractionRequest request) {
        switch (request.getRequestType()) {
            case SelectableCharacterCardExpected:
                ccsvCardSelector.applyTargets(request.getLegalTargets());
                break;
            case PositionExpected: {
                bdvBoard.applyTargets(request.getLegalTargets(), request.getContext(), gameContext.getBoard());
            } break;
            default:
                throw new IllegalStateException("Unexpected request type: " + request.getRequestType());
        }
    }

    @Override
    protected void updateInteractionUI(@NonNull GameContext gameContext,
                                       @NonNull InteractionRequest request) {
        super.updateInteractionUI(gameContext, request);

        highlightSelectableCards(gameContext, request);

        if (request.getRequestType() == InteractionType.PositionExpected &&
                gameContext.getGamePhase().getPhaseType() == GamePhaseType.Recruitment) {
            ccsvCardSelector.setPortraitsVisible(false);
            setNewCharacterVisible(request.getContext().getCharacter(), true);
        } else if (hasRecruitedCharacter(gameContext)) {
            showSecondRecruitmentDialog();
        }
    }

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

    public void onRecruitmentCardSelected(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }
    public void onBanishmentCardSelected() {
        throw new IllegalStateException("Banishment is not supported within recruitment tutorial activity");
    }

    @Override
    public void onNotSelectableCardClick() {
        controller.cancelAction();
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

    @Override
    protected void highlightPlayableCharacters(@NonNull GameContext gameContext,
                                               @NonNull InteractionRequest request) {
        super.highlightPlayableCharacters(gameContext, request);

        if (request.getLegalTargets().stream()
                .anyMatch(target -> target.getCategory() == TargetCategory.RecruitmentDestination)) {
            bdvBoard.startRecruitmentCellsAnimation();
        } else {
            bdvBoard.stopRecruitmentCellsAnimation();
        }
    }


    private void highlightSelectableCards(@NonNull GameContext gameContext,
                                          @NonNull InteractionRequest request) {
        GamePhase gamePhase = gameContext.getGamePhase();

        boolean isValidPhase = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;
        boolean selectableCardRequest = request.getRequestType() == InteractionType.SelectableCharacterCardExpected;

        if (isValidPhase && selectableCardRequest) {
            ccsvCardSelector.startShineAnimation();
        } else {
            ccsvCardSelector.stopShineAnimation();
        }
    }

    private void setNewCharacterVisible(@Nullable Character newCharacter, boolean visible) {
        chdNewCharacter.getCharacterView().setVisibility(visible ? View.VISIBLE : View.GONE);
        chdNewCharacter.setIsHighlighted(visible, false);

        if (visible) {
            int size = bdvBoard.getCharacterDisplaySize();
            chdNewCharacter.setSize(size);
            chdNewCharacter.setPosition(
                    (ccsvCardSelector.getWidth() - size) / 2f,
                    (ccsvCardSelector.getHeight() - size) / 2f
            );

            if (newCharacter != null) {
                chdNewCharacter.getCharacterView().setCharacter(newCharacter);
            }
            chdNewCharacter.startHighlightAnimation();
        } else {
            chdNewCharacter.stopHighlightAnimation();
        }
    }

    //endregion

    //region GAME CONTROLLER LISTENER METHODS

    @Override
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            GameContext gameContext = controller.getCurrentContext();

            bdvBoard.setBoard(game.getBoard());
            ccsvCardSelector.applyGameModeParams(gameContext.getGameMode());

            clearInteractionUI(gameContext);
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        throw new IllegalStateException("End game is not supported within recruitment tutorial activity");
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        ccsvCardSelector.setPortraitsVisible(false);
        txvAfter.setVisibility(View.VISIBLE);
        setBtnNextChapterEnabled(true);
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {
        // Playable character action requests are ignored in the activity
        if (request.getRequestType() == InteractionType.PlayableCharacterExpected) {
            return;
        }

        super.onInteractionRequired(request);
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

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((PortraitView) v).getPortraitCard());
        return true;
    }

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