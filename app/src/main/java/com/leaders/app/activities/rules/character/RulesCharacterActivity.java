package com.leaders.app.activities.rules.character;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

public final class RulesCharacterActivity extends PlayableActivity {
    private TextView txvName;
    private TextView txvDescription;
    private ImageView imvAbility;
    private ImageView imvArtwork;

    private MaterialButton btnReset;


    private GameHistory startHistory;
    private String startHistoryHash;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        txvName = findViewById(R.id.txvName_actRulesCharacter);
        txvDescription = findViewById(R.id.txvDescription_actRulesCharacter);
        imvAbility = findViewById(R.id.imvAbility_actRulesCharacter);
        imvArtwork = findViewById(R.id.imvArtwork_actRulesCharacter);

        btnReset = findViewById(R.id.btnReset_actRulesCharacter);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actRulesCharacter)).setOnClickListener(this::onNonInteractiveElementClick);

        btnReset.setOnClickListener(this::onResetClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        CharacterCard characterCard = CharacterCard.valueOf(getIntent().getStringExtra(ExtraUtils.EXTRA_CHARACTER_CARD));
        initCharacter(characterCard);

        startHistory = JsonUtils.loadCharacterDemo(this, characterCard);
        startHistoryHash = getHistoryHash(startHistory);

        controller = new GameController(this);
        controller.startGame(new GameHistory(startHistory));
    }

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesCharacter;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesCharacter;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesCharacter;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesCharacter;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_character;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesCharacter;
    }

    @NonNull
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesCharacter;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesCharacter;
    }

    @Override
    protected void doOnBackPressed() {
        goToActivity(ActivityType.RulesCharacterMenu, ActivityTransitionType.SlideLeft);
    }

    //endregion

    //region CHARACTER INFOS INITIALIZATION

    private void initCharacter(@NonNull CharacterCard card) {
        txvName.setText(CharacterCardUtils.getFormattedNameId(card));
        txvDescription.setText(CharacterCardUtils.getDescriptionId(card));
        imvAbility.setImageResource(getAbilityResId(card));
        imvArtwork.setImageResource(getArtworkResId(card));
    }

    private int getAbilityResId(@NonNull CharacterCard card) {
        if (card.isLeader()) {
            return R.drawable.icon_leader;
        }

        AbilityType[] abilityTypes = card.getAbilityTypes();
        if (abilityTypes.length < 1) {
            throw new IllegalStateException("Non leader cards must have an ability (" + card + ")");
        }

        AbilityType abilityType = abilityTypes[0];
        switch (abilityType) {
            case Active: return R.drawable.icon_ability_active;
            case Passive: return R.drawable.icon_ability_passive;
            case Special: return R.drawable.icon_ability_special;
            default: throw new IllegalStateException("Unexpected ability type: " + abilityType);
        }
    }
    
    private static int getArtworkResId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.drawable.character_artwork_acrobat;
            case Archer: return R.drawable.character_artwork_archer;
            case Assassin: return R.drawable.character_artwork_assassin;
            case Brewmaster: return R.drawable.character_artwork_brewmaster;
            case Bruiser: return R.drawable.character_artwork_bruiser;
            case ClawLauncher: return R.drawable.character_artwork_claw_launcher;
            case HermitAndCub: return R.drawable.character_artwork_hermit_and_cub;
            case Illusionist: return R.drawable.character_artwork_illusionist;
            case Jailer: return R.drawable.character_artwork_jailer;
            case LeaderKing: return R.drawable.character_artwork_leader_king;
            case LeaderQueen: return R.drawable.character_artwork_leader_queen;
            case Manipulator: return R.drawable.character_artwork_manipulator;
            case Nemesis: return R.drawable.character_artwork_nemesis;
            case Protector: return R.drawable.character_artwork_protector;
            case Rider: return R.drawable.character_artwork_rider;
            case RoyalGuard: return R.drawable.character_artwork_royal_guard;
            case Vizier: return R.drawable.character_artwork_vizier;
            case Wanderer: return R.drawable.character_artwork_wanderer;
            default: throw new IllegalArgumentException("No formatted name found for character card: " + characterCard);
        }
    }

    //region INTERACTION UI METHODS

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
    public void onGameEnded(@NonNull Player winner) {
        runOnUiThread(() -> showEndGame(winner));
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        throw new IllegalStateException("Phase change is not supported within character demo");
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private void onResetClick(View v) {
        controller.restartGame(new GameHistory(startHistory));
    }

    //endregion
}