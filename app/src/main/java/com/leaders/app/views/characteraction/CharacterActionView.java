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

import java.util.Objects;

public class CharacterActionView extends LinearLayoutCompat {
    @NonNull
    private final TextView txvIndex;
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

        txvIndex = findViewById(R.id.txvIndex_vwCharacterAction);
        chvSourceCharacter = findViewById(R.id.chvSourceCharacter_vwCharacterAction);
        imvActiveAbility = findViewById(R.id.imvActiveAbility_vwCharacterAction);
        chvTargetCharacter = findViewById(R.id.chvTargetCharacter_vwCharacterAction);
        txvDestination = findViewById(R.id.txvDestination_vwCharacterAction);
    }

    public CharacterActionView(@NonNull Context context, int index,
                               @NonNull CharacterAction characterAction) {
        this(context, null);

        txvIndex.setText(String.format("%s -", index + 1));

        chvSourceCharacter.setCharacter(characterAction.getSrcCharacter());

        CharacterActionTarget normalMovementTarget = getNormalMovementTarget(characterAction);
        if (normalMovementTarget != null) {
            imvActiveAbility.setVisibility(GONE);
            chvTargetCharacter.setVisibility(GONE);
            Position destPos = Objects.requireNonNull(
                    normalMovementTarget.getDestPos(),
                    "Invalid normal movement target : Destination missing"
            );
            txvDestination.setText(LbeUtils.getPositionExportStr(destPos));
            return;
        }

        CharacterActionTarget otherCharacterAbilityTarget = getOtherCharacterAbilityTarget(characterAction);
        if (otherCharacterAbilityTarget != null) {
            imvActiveAbility.setVisibility(VISIBLE);
            chvTargetCharacter.setVisibility(VISIBLE);
            chvTargetCharacter.setCharacter(otherCharacterAbilityTarget.getCharacter());
            if (motionHasDestination(characterAction)) {
                setDestinationText(otherCharacterAbilityTarget);
            }
            return;
        }

        CharacterActionTarget sourceCharacterAbilityTarget = getSourceCharacterAbilityTarget(characterAction);
        if (sourceCharacterAbilityTarget != null) {
            imvActiveAbility.setVisibility(VISIBLE);
            chvTargetCharacter.setVisibility(GONE);
            if (motionHasDestination(characterAction)) {
                setDestinationText(sourceCharacterAbilityTarget);
            }
        }
    }

    private void setDestinationText(@NonNull CharacterActionTarget target) {
        Position destPos = Objects.requireNonNull(
                target.getDestPos(),
                "Invalid target : Destination missing"
        );
        txvDestination.setText(LbeUtils.getPositionExportStr(destPos));
    }

    private boolean motionHasDestination(@NonNull CharacterAction characterAction) {
        CharacterMotionType motionType = characterAction.getMotions().get(0).getMotionType();
        return motionType != CharacterMotionType.Remove &&
                motionType != CharacterMotionType.Transform &&
                motionType != CharacterMotionType.Swap;
    }

    @Nullable
    private CharacterActionTarget getNormalMovementTarget(@NonNull CharacterAction characterAction) {
        if (characterAction.getMotions().size() == 1) {
            CharacterActionMotion motion = characterAction.getMotions().get(0);
            if (motion.getMotionType() == CharacterMotionType.Move &&
                motion.getTargets().size() == 1) {
                CharacterActionTarget target = motion.getTargets().get(0);
                if (target.getCharacter().getId().equals(characterAction.getSrcCharacter().getId())) {
                    return target;
                }
            }
        }
        return null;
    }

    private CharacterActionTarget getSourceCharacterAbilityTarget(@NonNull CharacterAction characterAction) {
        return getCharacterAbilityTarget(characterAction, true);
    }

    private CharacterActionTarget getOtherCharacterAbilityTarget(@NonNull CharacterAction characterAction) {
        return getCharacterAbilityTarget(characterAction, false);
    }


    private CharacterActionTarget getCharacterAbilityTarget(@NonNull CharacterAction characterAction,
                                                            boolean mustBeSourceCharacter) {
        for (CharacterActionMotion motion : characterAction.getMotions()) {
            for (CharacterActionTarget target : motion.getTargets()) {
                if (target.getCharacter().getId().equals(characterAction.getSrcCharacter().getId()) == mustBeSourceCharacter) {
                    return target;
                }
            }
        }
        return null;
    }

}
