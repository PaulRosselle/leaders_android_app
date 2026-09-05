package com.leaders.app.activities.replay;

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
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.GameActionUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.replay.ReplayControlsView;
import com.leaders.app.views.settings.AnimationSpeedView;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.entities.Board;

import java.util.List;

public class ReplayViewerActivity extends BaseActivity implements ReplayControlsView.ReplayControlsListener {
    private ReadOnlyBoardView bdvBoard;
    private ReplayControlsView rcvControls;

    private TextView txvReplayName;
    private PlayerTopView ptvTopPlayer;
    private PlayerBottomView pbvBottomPlayer;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private CharacterNotificationView cnvCardInfo;
    private AnimationSpeedView asvAnimationSpeed;
    private View vwDialogBg;

    private List<ReplaySave> replaySaves;
    private ReplaySave replaySave;

    private AnimationSpeed animationSpeed;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actReplayViewer);
        rcvControls = findViewById(R.id.rcvControls_actReplayViewer);

        txvReplayName = findViewById(R.id.txvReplayName_actReplayViewer);
        ptvTopPlayer = findViewById(R.id.ptvTopPlayer_actReplayViewer);
        pbvBottomPlayer = findViewById(R.id.pbvBottomPlayer_actReplayViewer);

        btnActions = findViewById(R.id.btnActions_actReplayViewer);
        amvActions = findViewById(R.id.amvActions_actReplayViewer);
        amvActions.addActionButton(R.drawable.icon_speed, R.string.animation_speed, 0, this::onAnimationSpeedClick);
        cnvCardInfo = findViewById(R.id.cnvCardInfo_actReplayViewer);
        asvAnimationSpeed = findViewById(R.id.asvAnimationSpeed_actReplayViewer);
        asvAnimationSpeed.setAvailableSpeeds(AnimationSpeed.getAllSpeedsWithMultiplier());
        vwDialogBg = findViewById(R.id.vwDialogBg_actReplayViewer);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        rcvControls.setControlsListener(this);

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

        replaySaves = JsonUtils.loadReplays(this);

        Intent intent = getIntent();
        int replayIndex = intent.getIntExtra(ExtraUtils.EXTRA_REPLAY_INDEX, -1);
        if (replayIndex < 0 || replayIndex >= replaySaves.size()) {
            throw new IllegalArgumentException("Invalid replay viewer intent: replay index missing");
        }

        ReplaySave intentReplaySave = replaySaves.get(replayIndex);

        bdvBoard.post(() -> {
            loadReplay(intentReplaySave);
            setupBoardAndPlayers();
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

    //endregion

    public void setupBoardAndPlayers() {
        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvBottomPlayer.getLayoutParams();

        // When recruiting, every view is aligned on top of each other
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
    }

    //region UI UPDATE METHODS

    private void loadReplay(@NonNull ReplaySave replaySave) {
        this.replaySave = replaySave;

        txvReplayName.setText(replaySave.getName());
        ptvTopPlayer.setPlayer(replaySave.getPlayers().get(1), LeaderType.King);
        pbvBottomPlayer.setPlayer(replaySave.getPlayers().get(0), LeaderType.Queen);

        bdvBoard.post(() -> rcvControls.loadReplay(replaySave));
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private boolean onCharacterLongClick(View v) {
        // TODO - show CharacterNotificationView
        return false;
    }

    private void onActionsClick(View v) {
        rcvControls.doPause();
        setActionsVisible(true);
    }

    private void onCardInfoClick(View v) {
        // TODO - hide cnvCardInfo
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

    private void onAnimationSpeedClick(View v) {
        amvActions.setVisibility(View.GONE);
        setAnimationSpeedVisible(true);
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
    }

    //endregion
}