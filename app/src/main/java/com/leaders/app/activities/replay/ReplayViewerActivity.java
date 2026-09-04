package com.leaders.app.activities.replay;

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
import com.leaders.app.enums.LeaderType;
import com.leaders.app.views.ActionsMenuView;
import com.leaders.app.views.board.ReadOnlyBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerTopView;
import com.leaders.app.views.replay.ReplayControlsView;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReplayViewerActivity extends BaseActivity {
    private ReadOnlyBoardView bdvBoard;
    private ReplayControlsView rcvControls;

    private TextView txvReplayName;
    private PlayerTopView ptvTopPlayer;
    private PlayerBottomView pbvBottomPlayer;

    private MaterialButton btnActions;
    private ActionsMenuView amvActions;
    private CharacterNotificationView cnvCardInfo;
    private View vwDialogBg;


    private ReplaySave replay;


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
        amvActions = findViewById(R.id.amvActions_actDuelPlayer);
        // TODO - actions menu buttons
        cnvCardInfo = findViewById(R.id.cnvCardInfo_actReplayViewer);
        vwDialogBg = findViewById(R.id.vwDialogBg_actReplayViewer);
    }


    @Override
    protected void initListeners() {
        super.initListeners();

        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);
        // TODO - replay control listener

        btnActions.setOnClickListener(this::onActionsClick);
        cnvCardInfo.setOnClickListener(this::onCardInfoClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);
    }


    @Override
    protected void initDatas() {
        super.initDatas();

        // TODO - get replay from save
        ReplaySave loadedReplay = new ReplaySave(
                "RalzMaw vs The World",
                LocalDate.now(),
                getTestGameHistory()
        );

        bdvBoard.post(() -> {
            loadReplay(loadedReplay);
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

    // TODO - remove
    private RecruitmentActionMotion getTestCharacter(@NonNull CharacterType characterType,
                                                     @NonNull TeamColor teamColor,
                                                     int x, int y) {
        return new RecruitmentActionMotion(
                RecruitmentMotionType.Add,
                Character.create(characterType, teamColor),
                new Position(x, y)
        );
    }

    // TODO - remove
    private GameHistory getTestGameHistory() {
        // The black player represents the human player (official puzzles convention)
        Player playerBlack = new Player(TeamColor.Black, "the world");
        Player playerWhite = new Player(TeamColor.White, "Ralzmaw");

        // The default game history is initialized each leader at their starting position

        List<IGameAction> initialActions = new ArrayList<>();
        initialActions.add(new RecruitmentAction(Arrays.asList(
                getTestCharacter(CharacterType.LeaderKing, TeamColor.White, 1, 1),
                getTestCharacter(CharacterType.Wanderer, TeamColor.White, 1, 2),
                getTestCharacter(CharacterType.Brewmaster, TeamColor.White, 1, 3),
                getTestCharacter(CharacterType.Bruiser, TeamColor.White, 2, 1),
                getTestCharacter(CharacterType.ClawLauncher, TeamColor.White, 2, 3),
                getTestCharacter(CharacterType.Acrobat, TeamColor.Black, 2, 2),
                getTestCharacter(CharacterType.Protector, TeamColor.Black, 3, 2),
                getTestCharacter(CharacterType.RoyalGuard, TeamColor.Black, 3, 3),
                getTestCharacter(CharacterType.Manipulator, TeamColor.Black, 3, 4),
                getTestCharacter(CharacterType.LeaderQueen, TeamColor.Black, 4, 3)
        )));

        GameConfig gameConfig =new GameConfig(
                List.of(playerBlack, playerWhite),
                playerWhite,
                GameMode.Strategist,
                Collections.emptyList(),
                initialActions
        );

        return new GameHistory(gameConfig, new ArrayList<>());
    }

    //region UI UPDATE METHODS

    private void loadReplay(@NonNull ReplaySave replay) {
        this.replay = replay;

        txvReplayName.setText(replay.getName());
        ptvTopPlayer.setPlayer(replay.getPlayers().get(1), LeaderType.King);
        pbvBottomPlayer.setPlayer(replay.getPlayers().get(0), LeaderType.Queen);
        bdvBoard.setBoard(GameFactory.create(replay.getPuzzleGameHistory()).getBoard());

        updateControls();
    }

    private void updateControls() {
        // TODO - call a dedicated method within rcvControls
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private boolean onCharacterLongClick(View v) {
        // TODO - show CharacterNotificationView
        return false;
    }

    private void onActionsClick(View v) {
        // TODO - show amvActions
    }

    private void onCardInfoClick(View v) {
        // TODO - hide cnvCardInfo
    }

    private void onDialogBgClick(View v) {
        // TODO - hide vwDialogBg and amvActions
    }

    //endregion

    //region ACTIONS METHODS

    // TODO - add methods for subelements of amvActions

    //endregion

    //region REPLAY CONTROL METHODS

    // TODO - add methods from ReplayControlsView listener

    //endregion
}