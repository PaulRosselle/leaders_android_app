package com.leaders.app.views.characteraction;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.leaders.R;
import com.leaders.app.utilities.LbeUtils;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterMotionType;

import java.util.List;
import java.util.Objects;

public class CharacterActionView extends LinearLayoutCompat {
    @NonNull
    private final CharacterView chvSourceCharacter;
    @NonNull
    private final ImageView imvActiveAbility;
    @NonNull
    private final CharacterView chvTargetCharacter;
    @NonNull
    private final TextView txvDestination;

    public CharacterActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_character_action, this);

        chvSourceCharacter = findViewById(R.id.chvSourceCharacter_vwCharacterAction);
        imvActiveAbility = findViewById(R.id.imvActiveAbility_vwCharacterAction);
        chvTargetCharacter = findViewById(R.id.chvTargetCharacter_vwCharacterAction);
        txvDestination = findViewById(R.id.txvDestination_vwCharacterAction);
    }

    public CharacterActionView(@NonNull Context context,
                               @NonNull CharacterAction characterAction) {
        this(context, (AttributeSet) null);

        chvSourceCharacter.setCharacter(characterAction.getSrcCharacter());

        if (characterAction.getMotions().isEmpty()) {
            throw new IllegalArgumentException("No display can be loaded for an empty character action");
        }

        if (isNormalMovement(characterAction)) {
            handleNormalMovement(characterAction);
        } else {
            handleCharacterAbility(characterAction);
        }
    }

    private void setDestinationText(@NonNull CharacterActionTarget target) {
        Position destPos = Objects.requireNonNull(
                target.getDestPos(),
                "Invalid target : Destination missing"
        );
        txvDestination.setText(LbeUtils.getPositionExportStr(destPos));
    }

    private boolean isNormalMovement(@NonNull CharacterAction characterAction) {
        for (CharacterActionMotion motion : characterAction.getMotions()) {
            List<CharacterActionTarget> targets = motion.getTargets();
            if (motion.getMotionType() != CharacterMotionType.Move ||
                    targets.size() != 1 ||
                    targets.get(0).getCharacter() != characterAction.getSrcCharacter()) {
                return false;
            }
        }

        return true;
    }

    private void handleNormalMovement(@NonNull CharacterAction characterAction) {
        imvActiveAbility.setVisibility(GONE);
        chvTargetCharacter.setVisibility(GONE);

        List<CharacterActionMotion> motions = characterAction.getMotions();
        setDestinationText(motions.get(motions.size() - 1).getTargets().get(0));
    }

    private void handleCharacterAbility(@NonNull CharacterAction characterAction) {
        List<CharacterActionMotion> motions = characterAction.getMotions();

        CharacterActionMotion motion = motions.get(0);
        if (!isSingleInteractionMotion(motion)) {
            motion = motions.get(motions.size() - 1);
        }

        List<CharacterActionTarget> targets = motion.getTargets();
        CharacterActionTarget abilityTarget = targets.get(targets.size() - 1);

        imvActiveAbility.setVisibility(VISIBLE);

        if (abilityTarget.getCharacter() != characterAction.getSrcCharacter()) {
            chvTargetCharacter.setCharacter(abilityTarget.getCharacter());
            chvTargetCharacter.setVisibility(VISIBLE);
        } else {
            chvTargetCharacter.setVisibility(GONE);
        }

        if (motionHasDestination(motion)) {
            setDestinationText(abilityTarget);
        }
    }

    private boolean isSingleInteractionMotion(@NonNull CharacterActionMotion motion) {
        return List.of(
                CharacterMotionType.Transform,
                CharacterMotionType.Add,
                CharacterMotionType.Remove,
                CharacterMotionType.Push,
                CharacterMotionType.Swap,
                CharacterMotionType.Fly
        ).contains(motion.getMotionType());
    }

    private boolean motionHasDestination(@NonNull CharacterActionMotion motion) {
        return !List.of(
                CharacterMotionType.Remove,
                CharacterMotionType.Transform,
                CharacterMotionType.Swap
        ).contains(motion.getMotionType());
    }

}
