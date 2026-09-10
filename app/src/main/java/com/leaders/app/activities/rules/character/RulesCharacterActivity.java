package com.leaders.app.activities.rules.character;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.controllers.GameController;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.utilities.CharacterCardUtils;
import com.leaders.app.utilities.ExtraUtils;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.EndGameView;
import com.leaders.app.views.board.PlayableBoardView;
import com.leaders.app.views.character.CharacterNotificationView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.AbilityType;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.gamelogic.interactions.InteractionFeedback;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.queries.BoardQuery;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONException;

import java.util.Objects;

public final class RulesCharacterActivity extends BaseActivity
        implements PlayableBoardView.OnTargetClickListener, GameController.Listener {
    private TextView txvName;
    private TextView txvDescription;
    private ImageView imvAbility;
    private ImageView imvArtwork;

    private MaterialButton btnReset;
    private MaterialButton btnUndoLastAction;

    private CharacterNotificationView cnvCardInfo;

    private EndGameView egvEndGame;

    private PlayableBoardView bdvBoard;


    private GameHistory startHistory;
    private String startHistoryHash;

    private GameController controller;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        txvName = findViewById(R.id.txvName_actRulesCharacter);
        txvDescription = findViewById(R.id.txvDescription_actRulesCharacter);
        imvAbility = findViewById(R.id.imvAbility_actRulesCharacter);
        imvArtwork = findViewById(R.id.imvArtwork_actRulesCharacter);

        btnReset = findViewById(R.id.btnReset_actRulesCharacter);
        btnUndoLastAction = findViewById(R.id.btnUndoLastAction_actRulesCharacter);

        cnvCardInfo = findViewById(R.id.cnvCardInfo_actRulesCharacter);

        bdvBoard = findViewById(R.id.bdvBoard_actRulesCharacter);

        egvEndGame = findViewById(R.id.egvEndGame_actRulesCharacter);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        (findViewById(R.id.clyMain_actRulesCharacter)).setOnClickListener(this::onNonInteractiveElementClick);

        btnReset.setOnClickListener(this::onResetClick);
        btnUndoLastAction.setOnClickListener(this::onUndoLastAction);

        cnvCardInfo.setOnClickListener(this::onCardInfoClick);

        egvEndGame.setOnClickListener(this::onEndGameClick);

        // Board element listeners
        bdvBoard.setOnTargetClickListener(this);
        bdvBoard.setOnCharacterLongClickListener(this::onCharacterLongClick);
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

    //region TARGET CLICK LISTENER METHODS

    @Override
    public void onTargetClick(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }

    @Override
    public void onEmptyClick() {
        controller.cancelAction();
    }

    //endregion

    //region INTERACTION UI METHODS

    private void clearInteractionUI() {
        bdvBoard.clearTargets();
        ButtonUtils.setEnabled(btnUndoLastAction, false);
    }

    private void highlightPlayableCharacters(@NonNull GameContext gameContext,
                                             @NonNull InteractionRequest request) {
        bdvBoard.highlightPlayableCharacters(
                gameContext.getPlayableCharacters(),
                request.getContext().getCharacter(),
                gameContext.getBoard()
        );

        if (request.getRequestType() == InteractionType.PlayableCharacterExpected) {
            bdvBoard.startPlayableCharactersShineAnimation();
        } else {
            bdvBoard.stopPlayableCharactersShineAnimation();
        }
    }

    private void updateInteractionUI(@NonNull GameContext gameContext,
                                     @NonNull InteractionRequest request) {

        bdvBoard.applyTargets(request.getLegalTargets(), request.getContext(), gameContext.getBoard());

        highlightPlayableCharacters(gameContext, request);

        ButtonUtils.setEnabled(btnUndoLastAction, controller.canUndoLastAction() && !isStartHistory());
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

        TeamColor winnerColor = winner.getTeamColor();
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(gameContext.getBoard(), winnerColor),
                "No leader found for team: " + winnerColor
        );

        egvEndGame.update(EndGameType.Victory,
                LeaderType.getFromCharacter(leaderCell.getCharacter()),
                getString(R.string.victory_title),
                getString(R.string.victory_subtitle)
        );
        egvEndGame.show();
    }

    //endregion

    //region CONTROLLER METHODS

    @Override
    public void onGameStarted(@NonNull Game game) {
        runOnUiThread(() -> {
            clearInteractionUI();
            bdvBoard.setBoard(game.getBoard());
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        runOnUiThread(() -> showEndGame(winner));
    }

    @Override
    public void onActionUndone(@NonNull Game game) {
        runOnUiThread(() -> bdvBoard.setBoard(game.getBoard()));
    }

    @Override
    public void onInteractionRequired(@NonNull InteractionRequest request) {
        runOnUiThread(() -> updateInteractionUI(controller.getCurrentContext(), request));
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        throw new IllegalStateException("Phase change is not supported within character demo");
    }

    @Override
    public void onFeedback(@NonNull InteractionFeedback feedback,
                           @NonNull GameController.InteractionCompletion completion) {
        runOnUiThread(() -> bdvBoard.animateFeedback(feedback, completion::complete));
    }

    @Override
    public void onInteractionCleared() {
        runOnUiThread(this::clearInteractionUI);
    }

    //endregion

    //region VIEWS LISTENER METHODS

    private void onNonInteractiveElementClick(View v) {
        controller.cancelAction();
    }

    private void onEndGameClick(View v) {
        egvEndGame.hide();
    }

    private void onResetClick(View v) {
        controller.restartGame(new GameHistory(startHistory));
    }

    private void onUndoLastAction(View v) {
        controller.undoLastAction();
    }

    private void onCardInfoClick(View v) {
        cnvCardInfo.hide();
    }

    private boolean onCharacterLongClick(View v) {
        CharacterType characterType = Objects.requireNonNull(((CharacterView) v).getCharacterType(),
                "An empty character piece is not authorized in the puzzle editor");
        CharacterCard characterCard = characterType.getCharacterCard();

        if (cnvCardInfo.getCharacterCard() == characterCard) {
            cnvCardInfo.setCharacterCard(null);
            cnvCardInfo.hide();
        } else {
            cnvCardInfo.setCharacterCard(characterCard);
            if (cnvCardInfo.getVisibility() != View.VISIBLE) {
                cnvCardInfo.show();
            }
        }

        return false;
    }

    //endregion

    @Override
    protected void onDestroy() {
        if (controller != null) {
            controller.shutdown();
        }

        super.onDestroy();
    }
}