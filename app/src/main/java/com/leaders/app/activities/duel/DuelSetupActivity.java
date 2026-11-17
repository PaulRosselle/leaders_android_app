package com.leaders.app.activities.duel;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.entities.CharactersSetup;
import com.leaders.app.entities.DuelSetup;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.PlayerSetup;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.DuelStartUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.duel.PlayerSetupView;
import com.leaders.app.views.duel.setup.CharactersSetupFormView;
import com.leaders.app.views.duel.setup.CharactersSetupView;
import com.leaders.app.views.duel.setup.FirstPlayerView;
import com.leaders.app.views.duel.setup.GameModeView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DuelSetupActivity extends BaseActivity implements PlayerSetupView.PlayerSetupWatcher, CharactersSetupFormView.Listener {
    private PlayerSetupView psvFirst, psvSecond;
    private GameModeView gmvGameMode;
    private FirstPlayerView fpvFirstPlayer;
    private View vwDialogBg;
    private CharactersSetupView csvCharacters;
    private CharactersSetupFormView csfvCharacters;
    private CharacterNotificationView cnvCardInfo;

    private final Random random = new Random();
    private Settings settings;

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        psvFirst = findViewById(R.id.psvFirst_actDuelSetup);
        psvSecond = findViewById(R.id.psvSecond_actDuelSetup);

        fpvFirstPlayer = findViewById(R.id.fpvFirstPlayer_actDuelSetup);
        gmvGameMode = findViewById(R.id.gmvGameMode_actDuelSetup);

        vwDialogBg = findViewById(R.id.vwDialogBg_actDuelSetup);
        csvCharacters = findViewById(R.id.csvCharacters_actDuelSetup);
        csfvCharacters = findViewById(R.id.csfvCharacters_actDuelSetup);
        cnvCardInfo = findViewById(R.id.cnvCardInfo_actDuelSetup);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        psvFirst.setPlayerSetupWatcher(this);
        psvSecond.setPlayerSetupWatcher(this);

        (findViewById(R.id.btnStartGame_actDuelSetup)).setOnClickListener(this::onStartGameClick);

        cnvCardInfo.setOnClickListener(v -> cnvCardInfo.hide());
        csvCharacters.setOpenFormListener(this::openCharactersForm);
        csvCharacters.setOnPortraitLongClick(this::onPortraitLongClick);

        csfvCharacters.setListener(this);
        csfvCharacters.setOnPortraitLongClick(this::onPortraitLongClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        settings = ((LeadersApplication) getApplication()).getSettings();

        initPlayerSetup(settings.getDuelSetup().getPlayerSetup1(), psvFirst);
        if (!settings.getUserName().isEmpty()) {
            psvFirst.setHint(settings.getUserName());
        }
        initPlayerSetup(settings.getDuelSetup().getPlayerSetup2(), psvSecond);
        gmvGameMode.setGameMode(settings.getDuelSetup().getGameMode());
        fpvFirstPlayer.setTeam(settings.getDuelSetup().getFirstPlayerTeam());

        csvCharacters.setCharacterSkins(settings.getCharacterSkins());
        csvCharacters.applySetup(settings.getDuelSetup().getCharactersSetup());
        cnvCardInfo.setCharacterSkins(settings.getCharacterSkins());
    }

    private void initPlayerSetup(@NonNull PlayerSetup playerSetup,
                                 @NonNull PlayerSetupView playerSetupView) {
        Player player = playerSetup.createPlayer();
        Character leader = playerSetup.createLeader();

        playerSetupView.setName(player.getName());
        playerSetupView.setTeamColor(player.getTeamColor());
        playerSetupView.setLeaderType(LeaderType.getFromCharacter(leader));
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
        return true;
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
                DuelStartUtils.getInitialCards(csvCharacters.getSetup(), random)
        );

        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            intent.putExtra(ExtraUtils.EXTRA_DUEL_GAME_DATAS, serializer.getAsJson(gameHistory).toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        saveSettings();
        goToActivity(intent);
    }

    private void saveSettings() {
        settings.setDuelSetup(new DuelSetup(
                psvFirst.getPlayerSetup(),
                psvSecond.getPlayerSetup(),
                gmvGameMode.getGameMode(),
                fpvFirstPlayer.getTeam(),
                csvCharacters.getSetup()
        ));
        JsonUtils.saveSettings(this, settings);
    }

    //endregion

    //region CHARACTERS SETUP

    @Override
    public void onSaveCharactersSetup(@NonNull CharactersSetup setup) {
        csvCharacters.applySetup(setup);
        closeCharactersForm();
    }

    @Override
    public void onCancelCharactersSetupChange() {
        closeCharactersForm();
    }

    private void openCharactersForm(@NonNull CharactersSetup setup) {
        vwDialogBg.setVisibility(View.VISIBLE);
        csfvCharacters.setVisibility(View.VISIBLE);
        csfvCharacters.applySetup(setup);
    }

    private void closeCharactersForm() {
        vwDialogBg.setVisibility(View.GONE);
        csfvCharacters.setVisibility(View.GONE);
    }

    private boolean onPortraitLongClick(View v) {
        CharacterCard portraitCard = ((PortraitView) v).getPortraitCard();
        if (cnvCardInfo.getCharacterCard() == portraitCard) {
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.show(portraitCard);
        }

        return true;
    }

    //endregion
}