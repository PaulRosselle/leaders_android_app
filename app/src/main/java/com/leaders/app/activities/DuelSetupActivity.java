package com.leaders.app.activities;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.views.duel.PlayerSetupView;
import com.leaders.app.views.character.CharacterHighlightView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.gamelogic.enums.TeamColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DuelSetupActivity extends BaseActivity implements PlayerSetupView.PlayerSetupWatcher {
    private PlayerSetupView psvFirst, psvSecond;
    private CharacterView chvTeamBlack, chvTeamWhite;
    private CharacterHighlightView chvTeamColorHighlight;
    private MaterialButton btnGameModeDiscovery, btnGameModeStrategist;
    private TextView txvGameModeSummary;

    private TeamColor firstTeamColor;
    private GameMode gameMode;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        psvFirst = findViewById(R.id.psvFirst_actDuelSetup);
        psvSecond = findViewById(R.id.psvSecond_actDuelSetup);

        chvTeamBlack = findViewById(R.id.chvTeamBlack_actDuelSetup);
        chvTeamWhite = findViewById(R.id.chvTeamWhite_actDuelSetup);
        chvTeamColorHighlight = findViewById(R.id.chvTeamColorHighlight_actDuelSetup);

        btnGameModeDiscovery = findViewById(R.id.btnGameModeDiscovery_actDuelSetup);
        btnGameModeStrategist = findViewById(R.id.btnGameModeStrategist_actDuelSetup);
        txvGameModeSummary = findViewById(R.id.txvGameModeSummary_actDuelSetup);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        psvFirst.setPlayerSetupWatcher(this);
        psvSecond.setPlayerSetupWatcher(this);

        chvTeamBlack.setOnClickListener(this::onFirstPlayerTeamClick);
        chvTeamWhite.setOnClickListener(this::onFirstPlayerTeamClick);

        btnGameModeDiscovery.setOnClickListener(this::onGameModeClick);
        btnGameModeStrategist.setOnClickListener(this::onGameModeClick);

        (findViewById(R.id.btnStartGame_actDuelSetup)).setOnClickListener(this::onStartGameClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        firstTeamColor = null;
        gameMode = null;

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

        // Game mode
        updateGameMode(btnGameModeDiscovery);

        // First player initialization needs to wait until team color views are loaded
        chvTeamColorHighlight.post(() ->
                updateFirstTeamColor(random.nextBoolean() ? chvTeamBlack : chvTeamWhite));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_duel_setup;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actDuelSetup;
    }

    @Nullable
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

    private void updateFirstTeamColor(@NonNull CharacterView characterView) {
        if (characterView.getTeamColor() == firstTeamColor) {
            return;
        }

        CharacterView otherChv = characterView == chvTeamBlack ? chvTeamWhite : chvTeamBlack;

        characterView.scaleForHighlight(true, true);
        otherChv.scaleForHighlight(false, true);

        this.firstTeamColor = characterView.getTeamColor();
        chvTeamColorHighlight.setX(characterView.getX());
        chvTeamColorHighlight.setY(characterView.getY());
    }

    private void updateGameMode(@NonNull MaterialButton btnGameMode) {
        boolean isDiscovery = btnGameMode == btnGameModeDiscovery;
        GameMode newGameMode = isDiscovery ? GameMode.Discovery : GameMode.Strategist;

        if (newGameMode == gameMode) {
            return;
        }

        this.gameMode = newGameMode;
        // We initialize color state lists for selected and unselected buttons
        ColorStateList selectedColor = AppCompatResources.getColorStateList(this, R.color.selected_golden);
        ColorStateList unselectedColor = AppCompatResources.getColorStateList(this, R.color.darker_background);

        // Then we update the buttons appearances based on the selected gameMode
        btnGameMode.setBackgroundTintList(selectedColor);
        MaterialButton btnOtherGameMode = isDiscovery ? btnGameModeStrategist : btnGameModeDiscovery;
        btnOtherGameMode.setBackgroundTintList(unselectedColor);

        // Finally we update the game mode summary
        txvGameModeSummary.setText(isDiscovery ? R.string.discovery_mode_summary : R.string.strategist_mode_summary);
    }

    private PlayerSetupView getOtherPlayerSetup(@NonNull PlayerSetupView playerSetupView) {
        return playerSetupView == psvFirst ? psvSecond : psvFirst;
    }

    //region VIEWS LISTENERS

    private void onFirstPlayerTeamClick(View v) {
        updateFirstTeamColor((CharacterView) v);
    }

    private void onGameModeClick(View v) {
        updateGameMode((MaterialButton) v);
    }

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
        // TODO
    }

    //endregion
}