package com.leaders.gamelogic.handlers;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionTarget;
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
        for (RecruitmentActionTarget target : recruitmentAction.getTargets()) {
            game.getBoard().getCell(target.getPosition()).setCharacter(target.getCharacter());
            // We always remove the card matching the recruited character from the recruitable cards pool.
            // The only cases where the removal can fail (without generating any exception) are
            // cards like the Hermit & Cub matching multiple characters
            game.getRecruitableCards().remove(target.getCharacter().getCharacterType().getCharacterCard());
            game.getRecruitedCharacters().add(target.getCharacter());
        }
    }

    @Override
    public void undoAction() {
        for (RecruitmentActionTarget target : recruitmentAction.getTargets()) {
            game.getRecruitedCharacters().remove(target.getCharacter());
            // We only add back a card once into the recruitable cards pool since it shouldn't host any duplicate
            CharacterCard card = target.getCharacter().getCharacterType().getCharacterCard();
            if (!game.getRecruitableCards().contains(card))
            {
                game.getRecruitableCards().add(card);
            }
            game.getBoard().getCell(target.getPosition()).setCharacter(null);
        }
    }
}
