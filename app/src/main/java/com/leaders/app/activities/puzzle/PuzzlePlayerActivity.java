package com.leaders.app.activities.puzzle;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.gamelogic.interactions.InteractionTarget;

public class PuzzlePlayerActivity extends BaseActivity implements PlayableBoardView.OnTargetClickListener {
    private MaterialButton btnReset;
    private MaterialButton btnUndoLastAction;
    private MaterialButton btnPuzzleActions;
    private View vwDialogBg;

    private CharacterNotificationView cnvCardInfo;

    private PlayableBoardView bdvBoard;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        btnReset = findViewById(R.id.btnReset_actPuzzlePlayer);
        btnUndoLastAction = findViewById(R.id.btnUndoLastAction_actPuzzlePlayer);
        btnPuzzleActions = findViewById(R.id.btnPuzzleActions_actPuzzleEditor);
        vwDialogBg = findViewById(R.id.vwDialogBg_actPuzzlePlayer);

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzlePlayer);

        bdvBoard = findViewById(R.id.bdvBoard_actPuzzleSolver);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnReset.setOnClickListener(this::onResetClick);
        btnUndoLastAction.setOnClickListener(this::onUndoLastAction);
        btnPuzzleActions.setOnClickListener(this::onPuzzleActionsClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);

        cnvCardInfo.setOnClickListener(this::onCardInfoClick);

        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);
    }

    @Override
    protected void initDatas() {
        // TODO
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_puzzle_player;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actPuzzlePlayer;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actPuzzlePlayer;
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
        return ActivityType.PuzzlePlayer;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.PuzzleSelection, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region VIEWS CLICK LISTENER METHODS

    private void onResetClick(View v) {
        // TODO
    }

    private void onUndoLastAction(View v) {
        // TODO
    }

    private void onDialogBgClick(View v) {
        // TODO
    }

    private void onCardInfoClick(View v) {
        // TODO
    }

    private void onPuzzleActionsClick(View v) {
        // TODO
    }

    private boolean onCharacterLongClick(View v) {
        // TODO
        return false;
    }

    //endregion

    //region TARGET CLICK LISTENER METHODS

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        // TODO
    }

    @Override
    public void onEmptyClick() {
        // TODO
    }

    //endregion
}