package com.leaders.app.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.controllers.GameController;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameContext;
import com.leaders.gamelogic.entities.PlayableCharacter;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.SelectableCharacterCard;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.interactions.InteractionRequest;
import com.leaders.gamelogic.interactions.InteractionTarget;
import com.leaders.gamelogic.queries.BoardQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TutorialBotUtils {
    private TutorialBotUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void handleRequest(@NonNull GameController controller,
                                     @NonNull InteractionRequest request) {
        switch (request.getRequestType()) {
            case SelectableCharacterCardExpected:
                handleSelectCardRequest(controller, request);
                break;
            case PlayableCharacterExpected:
                handlePlayableCharacterRequest(controller, request);
                break;
            case PositionExpected:
                handlePositionRequest(controller, request);
                break;
            case NoTargetExpected:
                handleNoTargetRequest(controller);
                break;
            default:
                throw new IllegalStateException("Unexpected request type: " + request.getRequestType());
        }
    }

    private static void handleSelectCardRequest(@NonNull GameController controller,
                                                @NonNull InteractionRequest request) {
        InteractionTarget selectedTarget = null;

        for (InteractionTarget target : getShuffledTargets(request)) {
            if (selectedTarget == null || getTargetCardScore(target) > getTargetCardScore(selectedTarget)) {
                selectedTarget = target;
            }
        }

        if (selectedTarget == null) {
            throw new IllegalStateException("No valid selectable card found");
        }

        controller.selectTarget(selectedTarget);
    }

    private static void handlePlayableCharacterRequest(@NonNull GameController controller,
                                                       @NonNull InteractionRequest request) {
        if (request.getLegalTargets().isEmpty()) {
            handleNoTargetRequest(controller);
            return;
        }

        // The bot can currently be blocked since a character can be technically playable
        // without any legal target positions. A change in this direction may be made
        // in PlayabilityQuery or here to take this case into account
        InteractionTarget selectedTarget = null;

        Position enemyLeaderPos = getEnemyLeaderPos(controller);

        // We select the closest character to the enemy leader
        for (InteractionTarget target : getShuffledTargets(request)) {
            if (selectedTarget == null ||
                    getTargetCharacterDistance(target, enemyLeaderPos) <
                            getTargetCharacterDistance(selectedTarget, enemyLeaderPos)) {
                selectedTarget = target;
            }
        }

        if (selectedTarget == null) {
            throw new IllegalStateException("No valid playable character found");
        }

        controller.selectTarget(selectedTarget);
    }

    private static void handlePositionRequest(@NonNull GameController controller,
                                              @NonNull InteractionRequest request) {
        InteractionTarget selectedTarget = null;

        Position enemyLeaderPos = getEnemyLeaderPos(controller);

        boolean isLeaderRequest = isLeaderRequest(request);

        // We select the closest position to the enemy leader
        for (InteractionTarget target : getShuffledTargets(request)) {
            if (isBetterTargetPosition(target, selectedTarget, enemyLeaderPos, isLeaderRequest)) {
                selectedTarget = target;
            }
        }

        if (selectedTarget == null) {
            throw new IllegalStateException("No valid position found");
        }

        controller.selectTarget(selectedTarget);
    }

    private static void handleNoTargetRequest(@NonNull GameController controller) {
        if (controller.canEndPhase()) {
            controller.endPhase();
        } else {
            throw new IllegalArgumentException("Request without target not handled");
        }
    }

    private static boolean isLeaderRequest(@NonNull InteractionRequest request) {
        Character character = request.getContext().getCharacter();
        return character != null && character.getCharacterType().getCharacterCard().isLeader();
    }

    private static boolean isBetterTargetPosition(@NonNull InteractionTarget target,
                                                  @Nullable InteractionTarget refTarget,
                                                  @NonNull Position enemyLeaderPos,
                                                  boolean isLeaderRequest) {
        if (refTarget == null) {
            return true;
        }

        int targetDist = getTargetPositionDistance(target, enemyLeaderPos);
        int selectedTargetDist = getTargetPositionDistance(refTarget, enemyLeaderPos);
        return isLeaderRequest ? targetDist > selectedTargetDist : targetDist < selectedTargetDist;
    }

    @NonNull
    private static Position getEnemyLeaderPos(@NonNull GameController controller) {
        GameContext gameContext = controller.getCurrentContext();

        Cell enemyLeaderCell = BoardQuery.findLeaderCell(gameContext.getBoard(), TutorialGameUtils.getPlayerTeamColor());
        if (enemyLeaderCell == null) {
            throw new IllegalStateException("Enemy leader not found");
        }

        return enemyLeaderCell.getPosition();
    }

    private static List<InteractionTarget> getShuffledTargets(@NonNull InteractionRequest request) {
        List<InteractionTarget> targets = new ArrayList<>(request.getLegalTargets());
        Collections.shuffle(targets);
        return targets;
    }

    private static int getTargetPositionDistance(@NonNull InteractionTarget target,
                                                 @NonNull Position enemyLeaderPos) {
        Position position = Objects.requireNonNull(
                target.getChosenPosition(),
                "Invalid target: not a position"
        );
        return position.distanceTo(enemyLeaderPos);
    }

    private static int getTargetCharacterDistance(@NonNull InteractionTarget target,
                                                  @NonNull Position enemyLeaderPos) {
        PlayableCharacter playableCharacter = Objects.requireNonNull(
                target.getChosenPlayableCharacter(),
                "Invalid target: not a playable character"
        );
        return playableCharacter.getPosition().distanceTo(enemyLeaderPos);
    }

    private static int getTargetCardScore(@NonNull InteractionTarget target) {
        SelectableCharacterCard selectableCard = Objects.requireNonNull(
                target.getChosenSelectableCharacterCard(),
                "Invalid target: not a selectable card"
        );
        return getCardScore(selectableCard.getCharacterCard());
    }

    private static int getCardScore(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return 7;
            case Archer: return 3;
            case Assassin: return 8;
            case Brewmaster: return 9;
            case Bruiser: return 2;
            case ClawLauncher: return 10;
            case Illusionist: return 12;
            case Jailer: return 0;
            case Manipulator: return 6;
            case Protector: return 1;
            case Rider: return 13;
            case RoyalGuard: return 5;
            case Vizier: return 4;
            case Wanderer: return 11;
            default: return Integer.MIN_VALUE;
        }
    }
}
