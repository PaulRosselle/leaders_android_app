package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Board;
import com.leaders.gamelogic.entities.Game;

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
        }
    }
}
