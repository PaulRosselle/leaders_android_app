package com.leaders.app.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.controllers.GameController;
import com.leaders.app.utilities.TutorialGameUtils;
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
import com.leaders.puzzlelogic.utilities.BotUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TutorialBot {

    /** Number of moves made since the last reset (game start / phase change). */
    private int moveCount = 0;

    /**
     * Characters that had no legal destination, keyed by their position,
     * with the moveCount at the time they were found blocked.
     */
    private final Map<Position, Integer> blockedAt = new HashMap<>();

    /**
     * Characters that chose not to move (their best destination was not an improvement),
     * keyed by their position, with the moveCount at the time of the decision.
     */
    private final Map<Position, Integer> declinedAt = new HashMap<>();

    /** Distance (to the player's non-leader characters) under which a position is considered dangerous. */
    private static final int SAFE_RADIUS = 3;
    /** Distance the bot's leader tries to keep from the player's leader. */
    private static final int PREFERRED_LEADER_DISTANCE = 2;
    /** Radius used to measure how much room a position leaves around it. */
    private static final int MOBILITY_RADIUS = 2;

    private static final double LEADER_APPROACH_WEIGHT = 1.0;
    private static final double DANGER_WEIGHT = 1.0;
    private static final double MOBILITY_WEIGHT = 0.3;

    /**
     * If true, the bot must move a character even if the move is not an improvement
     * (used when the phase cannot be ended otherwise).
     */
    private boolean forceMove = false;

    /** Playable character selected most recently, null if no character is being moved. */
    @Nullable
    private PlayableCharacter lastSelectedCharacter;

    /** Must be called when a game starts/restarts and when the phase changes. */
    public void reset() {
        moveCount = 0;
        blockedAt.clear();
        declinedAt.clear();
        forceMove = false;
        lastSelectedCharacter = null;
    }

    public void handleRequest(@NonNull GameController controller,
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

    //region REQUEST HANDLERS

    private void handleSelectCardRequest(@NonNull GameController controller,
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

    private void handlePlayableCharacterRequest(@NonNull GameController controller,
                                                @NonNull InteractionRequest request) {
        if (request.getLegalTargets().isEmpty()) {
            handleNoTargetRequest(controller);
            return;
        }

        List<InteractionTarget> fresh = new ArrayList<>();      // never skipped
        List<InteractionTarget> retryable = new ArrayList<>();  // skipped, but a move happened since

        for (InteractionTarget target : getShuffledTargets(request)) {
            Integer skippedAt = getSkippedAt(getTargetPosition(target));

            if (skippedAt == null) {
                fresh.add(target);
            } else if (skippedAt < moveCount) {
                retryable.add(target);
            }
        }

        // Skipped characters are only replayed once every other character has been played
        List<InteractionTarget> candidates = !fresh.isEmpty() ? fresh : retryable;

        if (candidates.isEmpty()) {
            if (controller.canEndPhase()) {
                controller.endPhase();
            } else if (!forceMove) {
                // The phase cannot be ended: a character must move, even without improvement
                forceMove = true;
                handlePlayableCharacterRequest(controller, request);
            } else {
                throw new IllegalStateException(
                        "Bot is blocked: no movable character and phase cannot be ended");
            }
            return;
        }

        Position playerLeaderPos = getPlayerLeaderPos(controller);

        // We select the closest character to the player leader
        InteractionTarget selectedTarget = null;
        for (InteractionTarget target : candidates) {
            if (selectedTarget == null ||
                    getTargetCharacterDistance(target, playerLeaderPos) <
                            getTargetCharacterDistance(selectedTarget, playerLeaderPos)) {
                selectedTarget = target;
            }
        }

        lastSelectedCharacter = getTargetPlayableCharacter(Objects.requireNonNull(selectedTarget));
        controller.selectTarget(selectedTarget);
    }

    private void handlePositionRequest(@NonNull GameController controller,
                                       @NonNull InteractionRequest request) {
        List<InteractionTarget> targets = getShuffledTargets(request);

        if (targets.isEmpty()) {
            // The selected character is immovable (for now): remember it and go back
            // to the character selection so another one can be chosen
            if (lastSelectedCharacter == null) {
                throw new IllegalStateException(
                        "Position request without target and no selected character");
            }
            blockedAt.put(lastSelectedCharacter.getPosition(), moveCount);
            lastSelectedCharacter = null;
            controller.cancelAction();
            return;
        }

        // A character may decide not to move if its best destination is not an improvement
        // (never possible for a mandatory character, nor in forced mode, nor when the
        // move was not initiated by a character selection). origin == null means "cannot decline".
        boolean canDecline = lastSelectedCharacter != null &&
                !lastSelectedCharacter.isMandatory() &&
                !forceMove;
        Position origin = canDecline ? lastSelectedCharacter.getPosition() : null;

        InteractionTarget selectedTarget = isBotLeaderRequest(request) ?
                chooseLeaderMove(controller, targets, origin) :
                chooseCharacterMove(controller, targets, origin);

        if (selectedTarget == null) {
            // The character prefers to stay where it is
            declinedAt.put(Objects.requireNonNull(origin), moveCount);
            lastSelectedCharacter = null;
            controller.cancelAction();
            return;
        }

        moveCount++;
        forceMove = false;
        lastSelectedCharacter = null;
        controller.selectTarget(selectedTarget);
    }

    private void handleNoTargetRequest(@NonNull GameController controller) {
        if (controller.canEndPhase()) {
            controller.endPhase();
        } else {
            throw new IllegalArgumentException("Request without target not handled");
        }
    }

    //endregion

    //region MOVE BEHAVIORS

    /**
     * Behavior of the bot's leader: stay mobile and prudent. It hovers at a preferred distance
     * from the player's leader, avoids the immediate surroundings of the player's other
     * characters, and prefers positions that leave room to maneuver (no corners).
     *
     * @param origin current position of the leader, or {@code null} if it cannot decline the move.
     * @return the destination to play, or {@code null} if the leader should stay where it is.
     */
    @Nullable
    private InteractionTarget chooseLeaderMove(@NonNull GameController controller,
                                               @NonNull List<InteractionTarget> targets,
                                               @Nullable Position origin) {
        Position playerLeaderPos = getPlayerLeaderPos(controller);
        List<Position> threats = getPlayerNonLeaderCharacterPositions(controller);
        List<Position> boardPositions = getBoardPositions(controller);

        InteractionTarget best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (InteractionTarget target : targets) {
            double score = getLeaderPositionScore(
                    getTargetDestination(target), playerLeaderPos, threats, boardPositions);
            if (best == null || score > bestScore) {
                best = target;
                bestScore = score;
            }
        }

        if (origin != null &&
                bestScore <= getLeaderPositionScore(origin, playerLeaderPos, threats, boardPositions)) {
            return null;
        }
        return best;
    }

    /** Score of a position for the bot's leader: the higher, the better. */
    private double getLeaderPositionScore(@NonNull Position position,
                                          @NonNull Position playerLeaderPos,
                                          @NonNull List<Position> threats,
                                          @NonNull List<Position> boardPositions) {
        // 1. Stay around a preferred distance from the player's leader (neither charge nor flee)
        double approachPenalty = Math.abs(
                position.distanceTo(playerLeaderPos) - PREFERRED_LEADER_DISTANCE);

        // 2. Danger only exists close to the player's characters and grows quickly as they get closer
        double danger = 0;
        for (Position threat : threats) {
            int margin = SAFE_RADIUS - position.distanceTo(threat);
            if (margin > 0) {
                danger += margin * margin;
            }
        }

        // 3. Prefer positions with room around them (center rather than edges and corners)
        int mobility = 0;
        for (Position boardPosition : boardPositions) {
            if (position.distanceTo(boardPosition) <= MOBILITY_RADIUS) {
                mobility++;
            }
        }

        return -LEADER_APPROACH_WEIGHT * approachPenalty
                - DANGER_WEIGHT * danger
                + MOBILITY_WEIGHT * mobility;
    }

    /** Positions of all the board's cells. */
    @NonNull
    private List<Position> getBoardPositions(@NonNull GameController controller) {
        List<Position> positions = new ArrayList<>();
        for (Cell cell : controller.getCurrentContext().getBoard().getCells().values()) {
            positions.add(cell.getPosition());
        }
        return positions;
    }

    /** Positions of the player's characters, leader excluded. */
    @NonNull
    private List<Position> getPlayerNonLeaderCharacterPositions(@NonNull GameController controller) {
        List<Cell> cells = BoardQuery.findCharacterCells(
                controller.getCurrentContext().getBoard(),
                TutorialGameUtils.getPlayerTeamColor(),
                null
        );

        List<Position> positions = new ArrayList<>(cells.size());
        for (Cell cell : cells) {
            Character character = Objects.requireNonNull(cell.getCharacter());
            if (!character.getCharacterType().getCharacterCard().isLeader()) {
                positions.add(cell.getPosition());
            }
        }
        return positions;
    }

    /**
     * Behavior of all the other bot's characters: get as close as possible to the player's leader.
     *
     * @param origin current position of the character, or {@code null} if it cannot decline the move.
     * @return the destination to play, or {@code null} if the character should stay where it is.
     */
    @Nullable
    private InteractionTarget chooseCharacterMove(@NonNull GameController controller,
                                                  @NonNull List<InteractionTarget> targets,
                                                  @Nullable Position origin) {
        Position playerLeaderPos = getPlayerLeaderPos(controller);

        InteractionTarget best = BotUtils.getClosestTarget(targets, playerLeaderPos);

        if (origin != null &&
                getTargetDestination(best).distanceTo(playerLeaderPos) >=
                        origin.distanceTo(playerLeaderPos)) {
            return null;
        }
        return best;
    }

    //endregion

    //region DECISION HELPERS

    /**
     * Last moveCount at which this character was skipped (blocked or declined),
     * null if it has never been skipped. Declined characters are ignored in forced mode.
     */
    @Nullable
    private Integer getSkippedAt(@NonNull Position pos) {
        Integer blocked = blockedAt.get(pos);
        Integer declined = forceMove ? null : declinedAt.get(pos);

        if (blocked == null) return declined;
        if (declined == null) return blocked;
        return Math.max(blocked, declined);
    }

    private boolean isBotLeaderRequest(@NonNull InteractionRequest request) {
        Character character = request.getContext().getTargetCharacter();
        return character != null &&
                character.getCharacterType().getCharacterCard().isLeader() &&
                character.getTeamColor() == TutorialGameUtils.getPlayerTeamColor().getOpposite();
    }

    @NonNull
    private Position getPlayerLeaderPos(@NonNull GameController controller) {
        GameContext gameContext = controller.getCurrentContext();

        Cell playerLeaderCell = BoardQuery.findLeaderCell(
                gameContext.getBoard(),
                TutorialGameUtils.getPlayerTeamColor()
        );
        if (playerLeaderCell == null) {
            throw new IllegalStateException("Player leader not found");
        }

        return playerLeaderCell.getPosition();
    }

    //endregion

    //region TARGET HELPERS

    private List<InteractionTarget> getShuffledTargets(@NonNull InteractionRequest request) {
        List<InteractionTarget> targets = new ArrayList<>(request.getLegalTargets());
        Collections.shuffle(targets);
        return targets;
    }

    @NonNull
    private PlayableCharacter getTargetPlayableCharacter(@NonNull InteractionTarget target) {
        return Objects.requireNonNull(
                target.getChosenPlayableCharacter(),
                "Invalid target: not a playable character"
        );
    }

    @NonNull
    private Position getTargetPosition(@NonNull InteractionTarget target) {
        return getTargetPlayableCharacter(target).getPosition();
    }

    @NonNull
    private Position getTargetDestination(@NonNull InteractionTarget target) {
        return Objects.requireNonNull(
                target.getChosenPosition(),
                "Invalid target: not a position"
        );
    }

    private int getTargetCharacterDistance(@NonNull InteractionTarget target,
                                           @NonNull Position referencePos) {
        return getTargetPosition(target).distanceTo(referencePos);
    }

    private int getTargetCardScore(@NonNull InteractionTarget target) {
        SelectableCharacterCard selectableCard = Objects.requireNonNull(
                target.getChosenSelectableCharacterCard(),
                "Invalid target: not a selectable card"
        );
        return getCardScore(selectableCard.getCharacterCard());
    }

    private int getCardScore(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Brewmaster: return 10;
            case Rider: return 9;
            case Acrobat: return 8;
            case Wanderer: return 7;
            case ClawLauncher: return 6;
            case Bruiser: return 5;
            case Assassin: return 4;
            case Illusionist: return 3;
            case RoyalGuard: return 2;
            case Manipulator: return 1;
            case Vizier: return 0;
            default: return Integer.MIN_VALUE;
        }
    }

    //endregion
}