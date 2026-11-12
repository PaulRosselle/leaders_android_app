package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.queries.CharacterTransformUtils;

/**
 * Four target cases are handled:
 * 1. movement (OriginPos → DestPos)
 * 2. addition (null → DestPos)
 * 3. removal (OriginPos → null)
 * 4. targeting only (null → null) - identifies a target without modifying the board
 */
public final class CharacterActionHandler extends GameActionHandler {
    @NonNull
    private final CharacterAction characterAction;
    public CharacterActionHandler(@NonNull Game game, @NonNull CharacterAction characterAction) {
        super(game);
        this.characterAction = characterAction;
    }

    @Override
    public void doAction() {
        Board board = game.getBoard();

        for (CharacterActionMotion motion : characterAction.getMotions()) {
            // Characters are removed from their original position before being added to their destination.
            // This prevents a character swap from removing a character that has already been placed.
            for (CharacterActionTarget target : motion.getTargets()) {
                if (target.getOriginPos() != null) {
                    board.getCell(target.getOriginPos()).setCharacter(null);
                }
            }
            for (CharacterActionTarget target : motion.getTargets()) {
                if (target.getDestPos() != null) {
                    board.getCell(target.getDestPos()).setCharacter(target.getCharacter());
                }
            }

            updateTransformedState(motion, false);
        }
    }

    @Override
    public void undoAction() {
        Board board = game.getBoard();

        // Motions must be undone in reverse order.
        // doAction() applies them sequentially, so undoing them in the
        // same order can leave intermediate positions occupied.
        for (int i = characterAction.getMotions().size() - 1; i >= 0; i--) {
            CharacterActionMotion motion = characterAction.getMotions().get(i);

            // Characters are removed from their destinations before being
            // restored to their original positions.
            for (CharacterActionTarget target : motion.getTargets()) {
                if (target.getDestPos() != null) {
                    board.getCell(target.getDestPos()).setCharacter(null);
                }
            }

            for (CharacterActionTarget target : motion.getTargets()) {
                if (target.getOriginPos() != null) {
                    board.getCell(target.getOriginPos()).setCharacter(target.getCharacter());
                }
            }

            updateTransformedState(motion, true);
        }
    }

    /**
     * Updates the transformed state associated with a character action motion.
     *
     * @param motion the motion whose transformed state should be updated
     * @param undoAction whether the action is being undone
     */
    private void updateTransformedState(@NonNull CharacterActionMotion motion, boolean undoAction) {
        if (!CharacterTransformUtils.isTransformMotion(motion)) {
            return;
        }

        TransformedState newState = CharacterTransformUtils.getStateFromTranformMotion(motion);

        if (motion.getMotionType() == CharacterMotionType.Transform) {
            applyTransformState(newState, undoAction);
        } else if (motion.getMotionType() == CharacterMotionType.Untransform) {
            applyUntransformState(newState, undoAction);
        }
    }

    /**
     * Applies or reverses the state change caused by a transform action.
     *
     * @param newState the transformed state describing the character before and after transformation
     * @param undoAction whether the action is being undone
     */
    private void applyTransformState(@NonNull TransformedState newState, boolean undoAction) {
        if (undoAction) {
            game.removeTransformedState(newState.getCharacterId());
        } else {
            // We add the transformed state (before -> after)
            game.addTransformedState(newState.getCharacterBefore(), newState.getCharacterAfter());
        }
    }

    /**
     * Applies or reverses the state change caused by an untransform action.
     *
     * @param newState the transformed state describing the character before and after untransformation
     * @param undoAction whether the action is being undone
     */
    private void applyUntransformState(@NonNull TransformedState newState, boolean undoAction) {
        // Undoing an "Untransform" action means retransforming the character (after -> before)
        if (undoAction) {
            game.addTransformedState(newState.getCharacterAfter(), newState.getCharacterBefore());
        } else {
            game.removeTransformedState(newState.getCharacterId());
        }
    }
}
