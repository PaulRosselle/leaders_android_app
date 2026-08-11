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
        for (RecruitmentActionMotion motion : recruitmentAction.getMotions()) {
            game.getRecruitedCharacters().remove(motion.getCharacter());
            // We only add back a card once into the recruitable cards pool since it shouldn't host any duplicate
            CharacterCard card = motion.getCharacter().getCharacterType().getCharacterCard();
            if (!game.getRecruitableCards().contains(card))
            {
                game.getRecruitableCards().add(card);
            }
            game.getBoard().getCell(motion.getPosition()).setCharacter(null);
        }
    }
}
