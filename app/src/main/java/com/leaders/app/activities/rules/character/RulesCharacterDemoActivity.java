package com.leaders.app.activities.rules.character;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.character.HighlightView;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.factories.GameFactory;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.queries.GameQuery;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

public final class RulesCharacterDemoActivity extends PlayableActivity {
    private TextView txvName;
    private TextView txvDescription;
    private ImageView imvAbility;
    private ImageView imvArtwork;

    private MaterialButton btnReset;
    private MaterialButton btnNextPhase;
    private HighlightView hlvNextPhase;

    private CharacterCard card;
    private GameHistory startHistory;
    private String startHistoryHash;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected int getMainLayoutId() {
        return R.id.clyMain_actRulesCharacterDemo;
    }

    @Override
    protected void initViews() {
        super.initViews();

        bdvBoard.setAlwaysUseDefaultSkins(true);

        txvName = findViewById(R.id.txvName_actRulesCharacterDemo);
        txvDescription = findViewById(R.id.txvDescription_actRulesCharacterDemo);
        imvAbility = findViewById(R.id.imvAbility_actRulesCharacterDemo);
        imvArtwork = findViewById(R.id.imvArtwork_actRulesCharacterDemo);

        btnReset = findViewById(R.id.btnReset_actRulesCharacterDemo);
        btnNextPhase = findViewById(R.id.btnNextPhase_actRulesCharacterDemo);
        hlvNextPhase = findViewById(R.id.hlvNextPhase_actRulesCharacterDemo);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        btnReset.setOnClickListener(this::onResetClick);
        btnNextPhase.setOnClickListener(this::onNextPhaseClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        card = CharacterCard.valueOf(getIntent().getStringExtra(ExtraUtils.EXTRA_CHARACTER_CARD));
        initCharacter();

        startHistory = JsonUtils.loadCharacterDemo(this, card);
        startHistoryHash = getHistoryHash(startHistory);

        controller = new GameController(this);
        GameHistory demoHistory = new GameHistory(startHistory);
        bdvBoard.post(() -> controller.startGame(demoHistory));
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesCharacterDemo;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesCharacterDemo;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesCharacterDemo;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesCharacterDemo;
    }

    @Override
    protected CharacterSkins getCharacterSkins() {
        return new CharacterSkins();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_character_demo;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesCharacterDemo;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesCharacterDemo;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesCharacterDemo;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesCharacterMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region CHARACTER INFOS INITIALIZATION

    private void initCharacter() {
        txvName.setText(CharacterCardUtils.getFormattedNameId(card));
        txvDescription.setText(CharacterCardUtils.getDescriptionId(card));
        imvAbility.setImageResource(CharacterCardUtils.getAbilityResId(card));
        imvArtwork.setImageResource(CharacterCardUtils.getArtworkResId(card));

        btnNextPhase.setVisibility(canEndTurn() ? View.VISIBLE : View.GONE);
    }

    //region END TURN METHODS

    private boolean canEndTurn() {
        return card == CharacterCard.Sniper;
    }

    private void endTurn() {
        GameHistory history = controller.getHistory();

        Player player = history.getConfig().getFirstPlayer();
        Game game = GameFactory.create(history);
        TeamColor opponentTeamColor = player.getTeamColor().getOpposite();
        GamePhase gamePhase = new GamePhase(GamePhaseType.TurnStart, player);

        if (GameQuery.isLeaderCaptured(game, gamePhase, opponentTeamColor)) {
            showEndGame(player);
        } else {
            showEndGame(controller.getCurrentContext(),
                    opponentTeamColor,
                    EndGameType.Defeat,
                    getString(R.string.defeat_title),
                    getString(R.string.demo_defeat_subtitle)
            );
        }
    }

    //endregion

    //region INTERACTION UI METHODS

    @Override
    protected void updateInteractionUI(@NonNull GameContext gameContext,
                                       @NonNull InteractionRequest request) {
        super.updateInteractionUI(gameContext, request);

        if (canEndTurn()) {
            setBtnNextPhaseEnabled(controller.canEndPhase(), request.getLegalTargets().isEmpty());
        }
    }

    private void setBtnNextPhaseEnabled(boolean enabled, boolean highlight) {
        ButtonUtils.setEnabled(btnNextPhase, enabled);
        hlvNextPhase.setVisibility(highlight ? View.VISIBLE : View.GONE);

        if (highlight) {
            hlvNextPhase.startAnimation();
        } else {
            hlvNextPhase.stopAnimation();
        }
    }

    @Override
    protected boolean canUndoLastAction() {
        return super.canUndoLastAction() && !isStartHistory();
    }

    private boolean isStartHistory() {
        return getHistoryHash(controller.getHistory()).equals(startHistoryHash);
    }

    private String getHistoryHash(@NonNull GameHistory gameHistory) {
        GameHistorySerializer serializer = new GameHistorySerializer();
        try {
            return serializer.getAsJson(gameHistory).toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void showEndGame(@NonNull Player winner) {
        GameContext gameContext = controller.getCurrentContext();

        showEndGame(gameContext,
                winner.getTeamColor(),
                EndGameType.Victory,
                getString(R.string.victory_title),
                getString(R.string.victory_subtitle)
        );
    }

    //endregion

    //region CONTROLLER METHODS

    @Override
    public void onGameEnded(@NonNull Player winner, @NonNull GameContext context) {
        runOnUiThread(() -> showEndGame(winner));
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase, @NonNull GameContext context) {
        throw new IllegalStateException("Phase change is not supported within character demo");
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private void onResetClick(View v) {
        controller.restartGame(new GameHistory(startHistory));
    }

    private void onNextPhaseClick(View v) {
        setBtnNextPhaseEnabled(false, false);
        endTurn();
    }

    //endregion
}