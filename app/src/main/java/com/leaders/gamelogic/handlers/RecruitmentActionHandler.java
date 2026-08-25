package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.enums.CharacterCard;

public final class RecruitmentActionHandler extends GameActionHandler {
    @NonNull
    private final RecruitmentAction recruitmentAction;

    public RecruitmentActionHandler(@NonNull Game game, @NonNull RecruitmentAction recruitmentAction) {
        super(game);
        this.recruitmentAction = recruitmentAction;
    }

    @Override
    public void doAction() {
        for (RecruitmentActionMotion motion : recruitmentAction.getMotions()) {
            game.getBoard().getCell(motion.getPosition()).setCharacter(motion.getCharacter());
            // We always remove the card matching the recruited character from the recruitable cards pool.
            // The only cases where the removal can fail (without generating any exception) are
            // cards like the Hermit & Cub matching multiple characters
            game.getRecruitableCards().remove(motion.getCharacter().getCharacterType().getCharacterCard());
            game.getRecruitedCharacters().add(motion.getCharacter());
        }
    }

    @Override
    public void undoAction() {
        for (int i = recruitmentAction.getMotions().size() - 1; i >= 0; i--) {
            RecruitmentActionMotion motion = recruitmentAction.getMotions().get(i);

            game.getRecruitedCharacters().remove(motion.getCharacter());

            // Restore the recruited character's card only once, since the
            // recruitable cards pool should not contain duplicates.
            CharacterCard card = motion.getCharacter().getCharacterType().getCharacterCard();

            if (!game.getRecruitableCards().contains(card)) {
                game.getRecruitableCards().add(card);
            }

            // Remove the character from the board.
            game.getBoard().getCell(motion.getPosition()).setCharacter(null);
        }
    }


}
