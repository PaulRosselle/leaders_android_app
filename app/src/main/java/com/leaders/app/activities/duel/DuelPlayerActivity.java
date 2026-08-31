package com.leaders.app.activities.duel;

import android.animation.LayoutTransition;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.Objects;

public class DuelPlayerActivity extends BaseActivity implements
        PlayableBoardView.OnTargetClickListener,
        CharacterCardSelectionView.OnCardSelectedListener {
    private PlayableBoardView bdvBoard;
    private CharacterCardSelectionView ccsvCardSelector;
    private PlayerBottomView pbvCurrentPlayer;
    private PlayerTopView ptvOpposingPlayer;

    private CharacterNotificationView cnvCardInfo;

    private MaterialButton btnActions;
    private MaterialButton btnCards;
    private MaterialButton btnUndoLastAction;
    private MaterialButton btnNextPhase;
    private ImageView imvNextPhase;
    

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard = findViewById(R.id.bdvBoard_actDuelPlayer);

        ccsvCardSelector = findViewById(R.id.ccsvCardSelector);

        pbvCurrentPlayer = findViewById(R.id.pbvCurrentPlayer_actDuelPlayer);
        ptvOpposingPlayer = findViewById(R.id.ptvOpposingPlayer_actDuelPlayer);

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actPuzzlePlayer);

        btnActions = findViewById(R.id.btnActions_actDuelPlayer);
        btnCards = findViewById(R.id.btnCards_actDuelPlayer);
        btnUndoLastAction = findViewById(R.id.btnUndoLastAction_actDuelPlayer);
        btnNextPhase = findViewById(R.id.btnNextPhase_actDuelPlayer);
        imvNextPhase = findViewById(R.id.imvNextPhase_actDuelPlayer);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onBoardCharacterLongClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

        btnActions.setOnClickListener(this::onActionsClick);
        btnCards.setOnClickListener(this::onCardsClick);
        btnUndoLastAction.setOnClickListener(this::onUndoLastActionClick);
        btnNextPhase.setOnClickListener(this::onNextPhaseClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        // TODO
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_duel_player;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actDuelPlayer;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actDuelPlayer;
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
        return true;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.DuelPlayer;
    }

    //endregion

    //region VIEW LISTENER METHODS

    private void onCardsClick(View v) {
        setCardSelectorVisible(ccsvCardSelector.getVisibility() != View.VISIBLE);
    }

    private void onUndoLastActionClick(View v) {
        // TODO - undo last action
    }

    private void onNextPhaseClick(View v) {
        // TODO - next phase
    }

    private void onActionsClick(View v) {
        // TODO - show actions menu
    }

    private boolean onBoardCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        showCardDescriptionNotification(characterType.getCharacterCard());
        return true;
    }

    private boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((CharacterCardPortraitView) v).getPortraitCard());
        return true;
    }

    //endregion

    //region INTERACTION METHODS

    @Override
    public void onEmptyClick() {

    }

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        // TODO - handle actions phase
    }

    public void onRecruitmentCardSelected(@NonNull InteractionTarget target) {
        // TODO - handle recruitment phase
    }
    public void onBanishmentCardSelected(@NonNull InteractionTarget target) {
        // TODO - handle banishment phase
    }

    //endregion

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

    private void setCardSelectorVisible(boolean visible) {
        // When recruiting, we display the cardSelector view below the current player view.
        // The layout transition is animated for both the board and current player view
        bdvBoard.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        pbvCurrentPlayer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvCurrentPlayer.getLayoutParams();
        // When recruiting, every view is aligned on top of each other
        if (visible) {
            boardParams.verticalBias = 0f;
            playerViewParams.verticalBias = 0f;
            float dpRatio = getResources().getDisplayMetrics().density;
            int boardHeight = bdvBoard.getMeasuredHeight();
            float playerHeaderHeight = boardHeight * (72f / 1177f);
            int boardMargin = 16;
            int playerViewMargin = boardMargin + 8;

            boardParams.topMargin = (int) (playerHeaderHeight + boardMargin * dpRatio);
            playerViewParams.topMargin = (int) (boardHeight - pbvCurrentPlayer.getMeasuredHeight() +
                    playerHeaderHeight * 2 + playerViewMargin * dpRatio);
            ccsvCardSelector.show(true);

            // By default, each playerView is on a vertical extremity while the board is centered
        } else {
            boardParams.verticalBias = 0.5f;
            playerViewParams.verticalBias = 1f;
            boardParams.topMargin = 0;
            playerViewParams.topMargin = 0;
            ccsvCardSelector.hide();
        }
        bdvBoard.setLayoutParams(boardParams);
        pbvCurrentPlayer.setLayoutParams(playerViewParams);

        // The requestLayout calls start the layout transition animation
        bdvBoard.requestLayout();
        pbvCurrentPlayer.requestLayout();
    }
}