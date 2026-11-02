package com.leaders.app.activities;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.utilities.AlignmentUtils;
import com.leaders.app.utilities.ButtonUtils;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.character.HighlightView;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.duel.PlayerBottomView;
import com.leaders.app.views.duel.PlayerHaloView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.entities.PlayerWarningState;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class of the activities displaying a full game (duel, tutorial game vs bot...):
 * card selector, "next phase" button, bottom player view and end of game handling.
 */
public abstract class FullGameActivity extends PlayableActivity implements
        CharacterCardSelectionView.OnCardSelectedListener {

    protected CharacterCardSelectionView ccsvCardSelector;
    protected CharacterDisplay chdNewCharacter;

    protected PlayerBottomView pbvBottomPlayer;
    protected PlayerHaloView phvBottomPlayer;

    protected MaterialButton btnCards;
    protected MaterialButton btnNextPhase;
    protected HighlightView hlvNextPhase;

    //region ABSTRACT METHODS

    @IdRes
    protected abstract int getCardSelectorViewId();

    @IdRes
    protected abstract int getBottomPlayerViewId();

    @IdRes
    protected abstract int getBottomPlayerHaloViewId();

    @IdRes
    protected abstract int getCardsButtonId();

    @IdRes
    protected abstract int getNextPhaseButtonId();

    @IdRes
    protected abstract int getNextPhaseHighlightViewId();

    /** Updates the player related views (names, warnings, current player halo, rotation...). */
    protected abstract void applyPlayerChange(@NonNull GameContext gameContext);

    /** Displays the end game screen. */
    protected abstract void showEndGame(@NonNull GameContext gameContext, @NonNull Player winner);

    //endregion

    //region HOOKS

    /** Called on the UI thread when the game starts, before the board is initialized. */
    protected void onGameStartedUi(@NonNull GameContext context) { }

    /** Called on the UI thread when the game ends, before the end game screen is displayed. */
    protected void onGameEndedUi(@NonNull GameContext context, @NonNull Player winner) { }

    /** Whether the card selector can be shown/hidden automatically on a phase change. */
    protected boolean canChangeSelectableCardsVisibility(@NonNull GameContext context) {
        return true;
    }

    /** Skin used to display the character being recruited. */
    @NonNull
    protected CharacterSkinType getNewCharacterSkin(@NonNull Character character) {
        CharacterSkins skins = getCharacterSkins();
        return skins.getCharacterSkin(character);
    }

    //endregion

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        ccsvCardSelector = findViewById(getCardSelectorViewId());
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);

        pbvBottomPlayer = findViewById(getBottomPlayerViewId());
        phvBottomPlayer = findViewById(getBottomPlayerHaloViewId());

        btnCards = findViewById(getCardsButtonId());
        btnNextPhase = findViewById(getNextPhaseButtonId());
        hlvNextPhase = findViewById(getNextPhaseHighlightViewId());
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        ccsvCardSelector.setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnScrollViewClickListener(this::onNonInteractiveElementClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);

        btnCards.setOnClickListener(this::onCardsClick);
        btnNextPhase.setOnClickListener(this::onNextPhaseClick);
    }

    //endregion

    //region VIEW LISTENER METHODS

    protected void onCardsClick(View v) {
        setCardSelectorVisible(ccsvCardSelector.getVisibility() != View.VISIBLE);
    }

    protected void onNextPhaseClick(View v) {
        GameContext gameContext = controller.getCurrentContext();

        if (gameContext.getGamePhase().getPhaseType() == GamePhaseType.Banishment) {
            controller.selectTarget(Objects.requireNonNull(
                    ccsvCardSelector.getSelectedTarget(),
                    "Cannot end banishment phase without a valid target"
            ));
        } else {
            controller.endPhase();
        }
    }

    protected boolean onPortraitLongClick(View v) {
        showCardDescriptionNotification(((PortraitView) v).getPortraitCard());
        return true;
    }

    //endregion

    //region CARD SELECTION LISTENER METHODS

    @Override
    public void onRecruitmentCardSelected(@NonNull InteractionTarget target) {
        controller.selectTarget(target);
    }

    @Override
    public void onNotSelectableCardClick() {
        controller.cancelAction();
    }

    //endregion

    //region UI STATE METHODS

    protected void setCardSelectorVisible(boolean visible) {
        AlignmentUtils.alignBoardView(
                visible,
                bdvBoard,
                pbvBottomPlayer,
                Collections.singletonList(phvBottomPlayer),
                visible ? Collections.singletonList(ccsvCardSelector) : Collections.emptyList(),
                true
        );

        ccsvCardSelector.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected void setBtnNextPhaseEnabled(boolean enabled, boolean highlight) {
        ButtonUtils.setEnabled(btnNextPhase, enabled);
        hlvNextPhase.setVisibility(highlight ? View.VISIBLE : View.GONE);

        if (highlight) {
            hlvNextPhase.startAnimation();
        } else {
            hlvNextPhase.stopAnimation();
        }
    }

    protected void applyPhaseChange(@NonNull GamePhase gamePhase,
                                    boolean canChangeSelectableCardsVisibility) {
        ccsvCardSelector.applyTurnPhase(gamePhase.getPhaseType());
        pbvBottomPlayer.setInfo(gamePhase.getPhaseType());

        boolean lockSelectableCardsView = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;

        ButtonUtils.setEnabled(btnCards, !lockSelectableCardsView);

        if (canChangeSelectableCardsVisibility) {
            setCardSelectorVisible(lockSelectableCardsView);
        }
    }

    protected void setNewCharacterVisible(@androidx.annotation.Nullable Character newCharacter,
                                          boolean visible) {
        chdNewCharacter.getCharacterView().setVisibility(visible ? View.VISIBLE : View.GONE);
        chdNewCharacter.setIsHighlighted(visible, false);

        if (visible) {
            int size = bdvBoard.getCharacterDisplaySize();
            chdNewCharacter.setSize(size);

            chdNewCharacter.setPosition(
                    (ccsvCardSelector.getWidth() - size) / 2f,
                    (ccsvCardSelector.getHeight() - size) / 2f
            );

            if (newCharacter != null) {
                chdNewCharacter.getCharacterView().setCharacter(
                        newCharacter,
                        getNewCharacterSkin(newCharacter)
                );
            }

            chdNewCharacter.startHighlightAnimation();
        } else {
            chdNewCharacter.stopHighlightAnimation();
        }
    }

    @NonNull
    protected LeaderType getPlayerLeaderType(@NonNull Player player, @NonNull Board board) {
        Cell leaderCell = Objects.requireNonNull(
                BoardQuery.findLeaderCell(board, player.getTeamColor()),
                "No leader found for player: " + player
        );

        return LeaderType.getFromCharacter(leaderCell.getCharacter());
    }

    protected boolean playerHasWarnings(@NonNull Player player,
                                        @NonNull List<PlayerWarningState> warningStates) {
        for (PlayerWarningState warningState : warningStates) {
            if (warningState.getPlayerTeamColor() == player.getTeamColor() &&
                    warningState.getWarningCount() > 0) {
                return true;
            }
        }

        return false;
    }

    //endregion

    //region INTERACTION METHODS

    @Override
    protected void clearInteractionUI(@NonNull GameContext gameContext) {
        super.clearInteractionUI(gameContext);

        ccsvCardSelector.applyCards(gameContext.getAvailableCharacterCards());

        ccsvCardSelector.setPortraitsVisible(true);
        setNewCharacterVisible(null, false);

        setBtnNextPhaseEnabled(false, false);
    }

    @Override
    protected void applyInteractionTargets(@NonNull GameContext gameContext,
                                           @NonNull InteractionRequest request) {
        switch (request.getRequestType()) {
            case NoTargetExpected:
                clearInteractionUI(gameContext); // Only require to choose a result within legalResults
                break;
            case SelectableCharacterCardExpected:
                ccsvCardSelector.applyTargets(request.getLegalTargets());
                break;
            case PlayableCharacterExpected:
            case PositionExpected: {
                bdvBoard.applyTargets(request.getLegalTargets(), request.getContext(), gameContext.getBoard());
            } break;
            default:
                throw new IllegalStateException("Unexpected request type: " + request.getRequestType());
        }
    }

    @Override
    protected void updateInteractionUI(@NonNull GameContext gameContext,
                                       @NonNull InteractionRequest request) {
        super.updateInteractionUI(gameContext, request);

        highlightSelectableCards(gameContext, request);

        setBtnNextPhaseEnabled(controller.canEndPhase(), request.getLegalTargets().isEmpty());

        if (request.getRequestType() == InteractionType.PositionExpected &&
                gameContext.getGamePhase().getPhaseType() == GamePhaseType.Recruitment) {
            ccsvCardSelector.setPortraitsVisible(false);
            setNewCharacterVisible(request.getContext().getTargetCharacter(), true);
        }

        applyPlayerChange(gameContext);

        applyPhaseChange(gameContext.getGamePhase(), false);
    }

    @Override
    protected void highlightPlayableCharacters(@NonNull GameContext gameContext,
                                               @NonNull InteractionRequest request) {
        super.highlightPlayableCharacters(gameContext, request);

        if (!animatePlayableItems) {
            return;
        }

        if (request.getLegalTargets().stream()
                .anyMatch(target -> target.getCategory() == TargetCategory.RecruitmentDestination)) {
            bdvBoard.startRecruitmentCellsAnimation();
        } else {
            bdvBoard.stopRecruitmentCellsAnimation();
        }
    }

    protected void highlightSelectableCards(@NonNull GameContext gameContext,
                                            @NonNull InteractionRequest request) {
        GamePhase gamePhase = gameContext.getGamePhase();

        boolean isValidPhase = gamePhase.getPhaseType() == GamePhaseType.Recruitment ||
                gamePhase.getPhaseType() == GamePhaseType.Banishment;
        boolean selectableCardRequest = request.getRequestType() == InteractionType.SelectableCharacterCardExpected;

        if (!animatePlayableItems) {
            return;
        }

        if (isValidPhase && selectableCardRequest) {
            ccsvCardSelector.startShineAnimation();
        } else {
            ccsvCardSelector.stopShineAnimation();
        }
    }

    //endregion

    //region GAME CONTROLLER METHODS

    @Override
    public void onGameStarted(@NonNull GameContext context) {
        runOnUiThread(() -> {
            onGameStartedUi(context);

            bdvBoard.setBoard(context.getBoard());
            ccsvCardSelector.applyGameModeParams(context.getGameMode());
            applyPlayerChange(context);
            applyPhaseChange(context.getGamePhase(), true);
            clearInteractionUI(context);
        });
    }

    @Override
    public void onGameEnded(@NonNull Player winner, @NonNull GameContext context) {
        runOnUiThread(() -> {
            clearInteractionUI(context);
            ButtonUtils.setEnabled(btnCards, true);
            pbvBottomPlayer.setInfo(com.leaders.R.string.game_ended, com.leaders.R.drawable.player_bottom_bg_info);

            onGameEndedUi(context, winner);
            showEndGame(context, winner);
        });
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase, @NonNull GameContext context) {
        runOnUiThread(() -> {
            clearInteractionUI(context);
            applyPlayerChange(context);
            applyPhaseChange(phase, canChangeSelectableCardsVisibility(context));
        });
    }

    //endregion
}