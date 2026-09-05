package com.leaders.app.activities.replay;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.GameActionUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.utilities.TeamColorUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.replay.RecruitableCardsView;
import com.leaders.app.views.replay.ReplayControlsView;
import com.leaders.app.views.settings.AnimationSpeedView;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.List;
import java.util.Objects;

public class ReplayViewerActivity extends BaseActivity implements ReplayControlsView.ReplayControlsListener {
    private enum ReplayViewerAction {
        ChangeAnimationSpeed,
        ChangePlayerPerspective,
        DisplayCellPositions,
        ShowRecruitableCards;

        private int getIconResId() {
            switch (this) {
                case ChangeAnimationSpeed: return R.drawable.icon_speed;
                case ChangePlayerPerspective: return R.drawable.icon_swap;
                case DisplayCellPositions: return R.drawable.icon_position;
                case ShowRecruitableCards: return R.drawable.icon_cards;
                default: throw new IllegalStateException("No icon found for replay viewer action: " + this);
            }
        }

        private int getTextResId() {
            switch (this) {
                case ChangeAnimationSpeed: return R.string.animation_speed;
                case ChangePlayerPerspective: return R.string.switch_side;
                case DisplayCellPositions: return R.string.board_coordinates;
                case ShowRecruitableCards: return R.string.recruitable_cards;
                default: throw new IllegalStateException("No text found for replay viewer action: " + this);
            }
        }

        private View.OnClickListener getOnClickListener(ReplayViewerActivity activity) {
            switch (this) {
                case ChangeAnimationSpeed: return activity::onChangeAnimationSpeedClick;
                case ChangePlayerPerspective: return activity::onChangeBoardOrientationClick;
                case DisplayCellPositions: return activity::onDisplayCellPositionsClick;
                case ShowRecruitableCards: return activity::onShowRecruitableCardsClick;
                default: throw new IllegalStateException("No click listener found for replay viewer action: " + this);
            }
        }
    }

    private ReadOnlyBoardView bdvBoard;
    private ReplayControlsView rcvControls;
    private RecruitableCardsView rtvRecruitableCards;

    private TextView txvReplayName;
    private PlayerTopView ptvTopPlayer;
    private PlayerBottomView pbvBottomPlayer;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private CharacterNotificationView cnvCardInfo;
    private AnimationSpeedView asvAnimationSpeed;
    private View vwDialogBg;

    private ReplaySave replaySave;

    private AnimationSpeed animationSpeed;
    private TeamColor playerPerspective;
    private boolean showRecruitableCards;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actReplayViewer);
        rcvControls = findViewById(R.id.rcvControls_actReplayViewer);
        rtvRecruitableCards = findViewById(R.id.rtvRecruitableCards_actReplayViewer);

        txvReplayName = findViewById(R.id.txvReplayName_actReplayViewer);
        ptvTopPlayer = findViewById(R.id.ptvTopPlayer_actReplayViewer);
        pbvBottomPlayer = findViewById(R.id.pbvBottomPlayer_actReplayViewer);

        btnActions = findViewById(R.id.btnActions_actReplayViewer);
        amvActions = findViewById(R.id.amvActions_actReplayViewer);
        for (ReplayViewerAction action : ReplayViewerAction.values()) {
            amvActions.addActionButton(
                    action.getIconResId(),
                    action.getTextResId(),
                    action.ordinal(),
                    action.getOnClickListener(this)
            );
        }
        cnvCardInfo = findViewById(R.id.cnvCardInfo_actReplayViewer);
        asvAnimationSpeed = findViewById(R.id.asvAnimationSpeed_actReplayViewer);
        asvAnimationSpeed.setAvailableSpeeds(AnimationSpeed.getAllSpeedsWithMultiplier());
        vwDialogBg = findViewById(R.id.vwDialogBg_actReplayViewer);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        rcvControls.setControlsListener(this);
        rtvRecruitableCards.setOnCardPortraitLongClick(this::onPortraitLongClick);

        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);

        btnActions.setOnClickListener(this::onActionsClick);
        asvAnimationSpeed.setChangeListener(this::onAnimationSpeedChange);
        asvAnimationSpeed.setOnClickListener(this::onAsvAnimationBgClick);
        cnvCardInfo.setOnClickListener(this::onCardInfoClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        animationSpeed = AnimationSpeed.Normal;
        playerPerspective = TeamColor.Black;
        showRecruitableCards = false;

        List<ReplaySave> replaySaves = JsonUtils.loadReplays(this);

        Intent intent = getIntent();
        int replayIndex = intent.getIntExtra(ExtraUtils.EXTRA_REPLAY_INDEX, -1);
        if (replayIndex < 0 || replayIndex >= replaySaves.size()) {
            throw new IllegalArgumentException("Invalid replay viewer intent: replay index missing");
        }

        ReplaySave intentReplaySave = replaySaves.get(replayIndex);

        bdvBoard.post(() -> {
            loadReplay(intentReplaySave);
            realignBoardView(false, false);
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_replay_viewer;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actReplayViewer;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actReplayViewer;
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
        return ActivityType.ReplayViewer;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.ReplaySelection, ActivityTransitionType.SlideLeft);
    }

    //endregion

    public void realignBoardView(boolean alignBottom, boolean animate) {
        if (animate) {
            bdvBoard.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            pbvBottomPlayer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }

        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvBottomPlayer.getLayoutParams();

        if (alignBottom) {
            boardParams.verticalBias = 0f;
            playerViewParams.verticalBias = 0f;

            float dpRatio = getResources().getDisplayMetrics().density;
            int boardHeight = bdvBoard.getMeasuredHeight();
            float playerHeaderHeight = boardHeight * (72f / 1177f);

            int boardMargin = 16;
            int playerViewMargin = boardMargin + 8;

            boardParams.topMargin = (int) (playerHeaderHeight + boardMargin * dpRatio);
            playerViewParams.topMargin = (int) (boardHeight - pbvBottomPlayer.getMeasuredHeight() +
                    playerHeaderHeight * 2 + playerViewMargin * dpRatio);

            bdvBoard.setLayoutParams(boardParams);
            pbvBottomPlayer.setLayoutParams(playerViewParams);

        } else {
            boardParams.verticalBias = 0.5f;
            playerViewParams.verticalBias = 1f;
            boardParams.topMargin = 0;
            playerViewParams.topMargin = 0;
        }

        if (animate) {
            // The requestLayout calls start the layout transition animation
            bdvBoard.requestLayout();
            pbvBottomPlayer.requestLayout();
        }
    }

    //region UI UPDATE METHODS

    private void loadReplay(@NonNull ReplaySave replaySave) {
        this.replaySave = replaySave;

        txvReplayName.setText(replaySave.getName());
        setPlayerPerspective(playerPerspective);

        rcvControls.loadReplay(replaySave);
    }

    @NonNull
    private Player getPlayerFromTeam(@NonNull TeamColor teamColor) {
        for (Player player : replaySave.getPlayers()) {
            if (player.getTeamColor() == teamColor) {
                return player;
            }
        }

        throw new IllegalStateException("No player found for team " + teamColor);
    }

    private LeaderType getPlayerLeaderType(@NonNull TeamColor teamColor) {
        for (IGameAction action : replaySave.getReplayGameHistory().getConfig().getInitialPlacements()) {
            if (action.getActionType() != GameActionType.Recruitment) {
                continue;
            }

            for (RecruitmentActionMotion motion : ((RecruitmentAction) action).getMotions()) {
                if (motion.getMotionType() == RecruitmentMotionType.Add &&
                        motion.getCharacter().getCharacterType().getCharacterCard().isLeader() &&
                        motion.getCharacter().getTeamColor() == teamColor) {
                    return LeaderType.getFromCharacterType(motion.getCharacter().getCharacterType());
                }
            }
        }

        throw new IllegalStateException("No leader found for team " + teamColor);
    }

    private void setPlayerPerspective(@NonNull TeamColor playerPerspective) {
        this.playerPerspective = playerPerspective;
        bdvBoard.setOrientation(TeamColorUtils.getOrientation(playerPerspective));

        Player bottomPlayer = getPlayerFromTeam(playerPerspective);
        Player topPlayer = getPlayerFromTeam(playerPerspective.getOpposite());

        ptvTopPlayer.setPlayer(topPlayer, getPlayerLeaderType(topPlayer.getTeamColor()));
        pbvBottomPlayer.setPlayer(bottomPlayer, getPlayerLeaderType(bottomPlayer.getTeamColor()));
    }

    private void showCardDescriptionNotification(@NonNull CharacterCard characterCard) {
        if (cnvCardInfo.getCharacterCard() == characterCard) {
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(characterCard);
            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
        }
    }

    private void updateRecruitableCards() {
        if (!showRecruitableCards) {
            return;
        }

        rtvRecruitableCards.updateRecruitableCards(
                rcvControls.getReplayGame(),
                replaySave.getGameMode()
        );
    }

    private void setShowRecruitableCards(boolean showRecruitableCards) {
        this.showRecruitableCards = showRecruitableCards;

        realignBoardView(showRecruitableCards, true);

        if (showRecruitableCards) {
            rtvRecruitableCards.show(true);
            updateRecruitableCards();
        } else {
            rtvRecruitableCards.hide();
        }
    }


    //endregion

    //region VIEWS LISTENER METHODS

    private boolean onCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        showCardDescriptionNotification(characterType.getCharacterCard());

        return true;
    }

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((CharacterCardPortraitView) v).getPortraitCard());

        return true;
    }

    private void onActionsClick(View v) {
        rcvControls.doPause();
        setActionsVisible(true);
    }

    private void onCardInfoClick(View v) {
        cnvCardInfo.hide();
    }

    private void onDialogBgClick(View v) {
        if (asvAnimationSpeed.getVisibility() == View.VISIBLE) {
            setAnimationSpeedVisible(false);
        } else {
            setActionsVisible(false);
        }
    }

    private void onAsvAnimationBgClick(View v) {
        // Dummy on click listener to prevent a "onDialogBgClick"
    }

    private void onAnimationSpeedChange(@NonNull AnimationSpeed speed) {
        animationSpeed = speed;
    }

    //endregion

    //region ACTIONS METHODS

    private void onChangeAnimationSpeedClick(View v) {
        amvActions.setVisibility(View.GONE);
        setAnimationSpeedVisible(true);
    }

    private void onChangeBoardOrientationClick(View v) {
        setPlayerPerspective(playerPerspective.getOpposite());
        setActionsVisible(false);
    }

    private void onDisplayCellPositionsClick(View v) {
        bdvBoard.setCellPositionVisible(!bdvBoard.isCellPositionVisible());
        setActionsVisible(false);
    }

    private void onShowRecruitableCardsClick(View v) {
        setShowRecruitableCards(!showRecruitableCards);
        setActionsVisible(false);
    }

    private void setAnimationSpeedVisible(boolean visible) {
        asvAnimationSpeed.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setActionsVisible(boolean visible) {
        amvActions.setVisibility(visible ? View.VISIBLE : View.GONE);
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion

    //region REPLAY CONTROL METHODS

    @Override
    public void onReplayLoaded(@NonNull Board board) {
        bdvBoard.setBoard(board);
        updateRecruitableCards();
    }

    @Override
    public void onActionPlayed(@NonNull IGameAction action, boolean playInReverse, @NonNull Runnable onActionEnd) {
        if (!GameActionUtils.isAnimatable(action)) {
            return;
        }

        IGameAction actionToPlay;
        if (playInReverse && GameActionUtils.isReversible(action)) {
            actionToPlay = GameActionUtils.reverse(action);
        } else {
            actionToPlay = action;
        }

        GameActionUtils.animate(bdvBoard, actionToPlay, onActionEnd, animationSpeed);
        updateRecruitableCards();
    }

    //endregion
}