package com.leaders.app.activities.duel;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.PlayerSetup;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.DuelStartUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.views.duel.PlayerSetupView;
import com.leaders.app.views.duel.setup.FirstPlayerView;
import com.leaders.app.views.duel.setup.GameModeView;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DuelSetupActivity extends BaseActivity implements PlayerSetupView.PlayerSetupWatcher {
    private PlayerSetupView psvFirst, psvSecond;
    private GameModeView gmvGameMode;
    private FirstPlayerView fpvFirstPlayer;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        psvFirst = findViewById(R.id.psvFirst_actDuelSetup);
        psvSecond = findViewById(R.id.psvSecond_actDuelSetup);

        fpvFirstPlayer = findViewById(R.id.fpvFirstPlayer_actDuelSetup);
        gmvGameMode = findViewById(R.id.gmvGameMode_actDuelSetup);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        psvFirst.setPlayerSetupWatcher(this);
        psvSecond.setPlayerSetupWatcher(this);

        (findViewById(R.id.btnStartGame_actDuelSetup)).setOnClickListener(this::onStartGameClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        Settings settings = ((LeadersApplication) getApplication()).getSettings();
        // First player setup is always loaded with the username
        psvFirst.setName(settings.getUserName());

        Random random = new Random();

        // Player team color
        TeamColor teamColor = random.nextBoolean() ? TeamColor.Black : TeamColor.White;
        psvFirst.setTeamColor(teamColor);
        psvSecond.setTeamColor(teamColor.getOpposite());

        // Player leader
        List<LeaderType> leaderTypes = new ArrayList<>(Arrays.asList(LeaderType.values()));
        Collections.shuffle(leaderTypes);
        psvFirst.setLeaderType(leaderTypes.remove(0));
        psvSecond.setLeaderType(leaderTypes.remove(0));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_duel_setup;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actDuelSetup;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actDuelSetup;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return false;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.DuelSetup;
    }

    //endregion

    private PlayerSetupView getOtherPlayerSetup(@NonNull PlayerSetupView playerSetupView) {
        return playerSetupView == psvFirst ? psvSecond : psvFirst;
    }

    //region VIEWS LISTENERS

    @Override
    public void onLeaderTypeChanged(@NonNull PlayerSetupView playerSetupView,
                                    @NonNull LeaderType leaderType) {
        PlayerSetupView psvOther = getOtherPlayerSetup(playerSetupView);
        if (psvOther.getLeaderType() == leaderType) {
            psvOther.setLeaderType(leaderType.getNext());
        }
    }

    @Override
    public void onTeamColorChanged(@NonNull PlayerSetupView playerSetupView,
                                   @NonNull TeamColor teamColor) {
        getOtherPlayerSetup(playerSetupView).setTeamColor(teamColor.getOpposite());
    }

    private void onStartGameClick(View v) {
        Intent intent = ActivityType.DuelPlayer.getIntent(this);

        List<PlayerSetup> playerSetups = new ArrayList<>();
        for (PlayerSetupView playerSetupView : List.of(psvFirst, psvSecond)) {
            playerSetups.add(playerSetupView.getPlayerSetup());
        }

        GameHistory gameHistory = DuelStartUtils.getDefaultHistory(
                playerSetups, fpvFirstPlayer.getTeam().getTeamColor(), gmvGameMode.getGameMode(),
                DuelStartUtils.getDefaultRecruitableCards()
        );

        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            intent.putExtra(ExtraUtils.EXTRA_DUEL_GAME_DATAS, serializer.getAsJson(gameHistory).toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        goToActivity(intent);
    }

    //endregion
}