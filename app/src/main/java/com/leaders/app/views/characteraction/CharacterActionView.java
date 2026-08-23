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
    private TextView txvIndex;
    @NonNull
    private CharacterView chvSourceCharacter;
    @NonNull
    private ImageView imvActiveAbility;
    @NonNull
    private CharacterView chvTargetCharacter;
    @NonNull
    private TextView txvDestination;

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
        this(context, (AttributeSet) null);

        txvIndex.setText(String.format("%s -", index + 1));

        chvSourceCharacter.setCharacter(characterAction.getSrcCharacter());

        Position destPos = null;
        if (isNormalMovement(characterAction)) {
            imvActiveAbility.setVisibility(GONE);
            chvTargetCharacter.setVisibility(GONE);
            destPos = Objects.requireNonNull(
                    characterAction.getMotions().get(0).getTargets().get(0).getDestPos(),
                    "Invalid normal movement target : Destination missing"
            );
        } else {
            imvActiveAbility.setVisibility(VISIBLE);
            boolean hasTarget = false;
            for (CharacterActionMotion motion : characterAction.getMotions()) {
                for (CharacterActionTarget target : motion.getTargets()) {
                    if (target.getCharacter() != characterAction.getSrcCharacter()) {
                        hasTarget = true;
                        chvTargetCharacter.setCharacter(target.getCharacter());
                        if (motionHasDestination(motion)) {
                            destPos = target.getDestPos();
                        }
                        break;
                    }
                }
            }
            chvTargetCharacter.setVisibility(hasTarget ? VISIBLE : GONE);
        }

        if (destPos != null) {
            txvDestination.setText(LbeUtils.getPositionExportStr(destPos));
        }
    }

    private boolean motionHasDestination(@NonNull CharacterActionMotion motion) {
        CharacterMotionType motionType = motion.getMotionType();
        return motionType != CharacterMotionType.Remove &&
                motionType != CharacterMotionType.Transform &&
                motionType != CharacterMotionType.Swap;
    }

    private boolean isNormalMovement(@NonNull CharacterAction characterAction) {
        return characterAction.getMotions().size() == 1 &&
                characterAction.getMotions().get(0).getMotionType() == CharacterMotionType.Move &&
                characterAction.getMotions().get(0).getTargets().size() == 1 &&
                characterAction.getMotions().get(0).getTargets().get(0).getCharacter() == characterAction.getSrcCharacter();
    }
}
