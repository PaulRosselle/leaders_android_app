package com.leaders.app.utilities;

import androidx.annotation.NonNull;

import com.leaders.app.controllers.GameController;
import com.leaders.gamelogic.interactions.InteractionRequest;

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
        // TODO - select based on a card score system
    }

    private static void handlePlayableCharacterRequest(@NonNull GameController controller,
                                                       @NonNull InteractionRequest request) {
        // TODO - select based on the proximity with the enemy leader
    }

    private static void handlePositionRequest(@NonNull GameController controller,
                                              @NonNull InteractionRequest request) {
        // TODO - select always the closest position to the enemy leader
    }

    private static void handleNoTargetRequest(@NonNull GameController controller) {
        if (controller.canEndPhase()) {
            controller.endPhase();
        } else {
            throw new IllegalArgumentException("Request without target not handled");
        }
    }
}
