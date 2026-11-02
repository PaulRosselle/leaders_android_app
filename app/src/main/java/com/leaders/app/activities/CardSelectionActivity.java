package com.leaders.app.activities;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.entities.CharacterSkins;
import com.leaders.app.enums.CharacterSkinType;
import com.leaders.app.views.character.CharacterDisplay;
import com.leaders.app.views.duel.CharacterCardSelectionView;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.enums.GamePhaseType;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.interactions.InteractionType;
import com.leaders.gamelogic.interactions.TargetCategory;

/**
 * Base class of the activities that let the player recruit/ban characters through a card selector
 * (full games, recruitment tutorial...): card selector handling, recruited character display,
 * recruitment cells and selectable cards animations.
 */
public abstract class CardSelectionActivity extends PlayableActivity implements
        CharacterCardSelectionView.OnCardSelectedListener {

    protected CharacterCardSelectionView ccsvCardSelector;
    protected CharacterDisplay chdNewCharacter;

    //region ABSTRACT METHODS

    @IdRes
    protected abstract int getCardSelectorViewId();

    //endregion

    //region HOOKS

    /** Skin used to display the character being recruited. */
    @NonNull
    protected CharacterSkinType getNewCharacterSkin(@NonNull Character character) {
        CharacterSkins skins = getCharacterSkins();
        return skins.getCharacterSkin(character);
    }

    /**
     * Called at the end of {@link #updateInteractionUI} when the player is NOT currently
     * choosing the destination of a recruited character.
     */
    protected void onRecruitmentPlacementNotRequested(@NonNull GameContext gameContext,
                                                      @NonNull InteractionRequest request) {
        // No default behavior
    }

    //endregion

    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        ccsvCardSelector = findViewById(getCardSelectorViewId());
        chdNewCharacter = new CharacterDisplay(this, ccsvCardSelector);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        // Non interactive element listeners
        ccsvCardSelector.setOnClickListener(this::onNonInteractiveElementClick);
        ccsvCardSelector.setOnScrollViewClickListener(this::onNonInteractiveElementClick);

        ccsvCardSelector.setOnCardSelectedListener(this);
        ccsvCardSelector.setOnPortraitLongClickListener(this::onPortraitLongClick);
    }

    //endregion

    //region VIEW LISTENER METHODS

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

    protected void setNewCharacterVisible(@Nullable Character newCharacter, boolean visible) {
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

    //endregion

    //region INTERACTION METHODS

    @Override
    protected void clearInteractionUI(@NonNull GameContext gameContext) {
        super.clearInteractionUI(gameContext);

        ccsvCardSelector.applyCards(gameContext.getAvailableCharacterCards());

        ccsvCardSelector.setPortraitsVisible(true);
        setNewCharacterVisible(null, false);
    }

    /**
     * Handles {@link InteractionType#SelectableCharacterCardExpected} and
     * {@link InteractionType#PositionExpected}. Subclasses supporting other request types
     * must handle them and delegate the rest to this method.
     */
    @Override
    protected void applyInteractionTargets(@NonNull GameContext gameContext,
                                           @NonNull InteractionRequest request) {
        switch (request.getRequestType()) {
            case SelectableCharacterCardExpected:
                ccsvCardSelector.applyTargets(request.getLegalTargets());
                break;
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

        if (request.getRequestType() == InteractionType.PositionExpected &&
                gameContext.getGamePhase().getPhaseType() == GamePhaseType.Recruitment) {
            ccsvCardSelector.setPortraitsVisible(false);
            setNewCharacterVisible(request.getContext().getTargetCharacter(), true);
        } else {
            onRecruitmentPlacementNotRequested(gameContext, request);
        }
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
}