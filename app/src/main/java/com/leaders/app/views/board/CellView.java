package com.leaders.app.views.board;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.gamelogic.interactions.InteractionTarget;

import java.util.Objects;

public final class CellView extends AppCompatImageView {
    @Nullable
    private InteractionTarget target;

    public CellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        target = null;

        // Cell views are instanciated without a target
        clearTarget();
    }

    private Point getCenter() {
        return new Point((int) (getX() + getWidth() / 2f), (int) (getY() + getHeight() / 2f));
    }

    public void clearTarget() {
        target = null;
        setImageResource(R.drawable.empty_cell);
    }

    public void setAsRecruitmentDestinationTarget(@NonNull InteractionTarget target,
                                                  @NonNull BoardOrientation boardOrientation) {
        this.target = target;
        int columnAxisPos = Objects.requireNonNull(target.getChosenPosition(),
                "Recruitment interaction target invalid: no position").getQ();
        setImageResource(columnAxisPos == 0 ?
                R.drawable.target_recruitment_center : R.drawable.target_recruitment_side);
        boolean rotateImg = boardOrientation == BoardOrientation.Rotated ? columnAxisPos < 0 : columnAxisPos > 0;

        setRotation(0f);
        setRotationY(rotateImg ? 180f : 0f);
        setRotationX(0f);
    }

    public void setAsMovementDestinationTarget(@NonNull InteractionTarget target,
                                               @NonNull CellView clvCharacter) {
        this.target = target;
        setImageResource(R.drawable.target_movement);
        rotateAwayFromCell(clvCharacter);
    }

    public void setAsActiveAbilityDestinationTarget(@NonNull InteractionTarget target) {
        this.target = target;
        setImageResource(R.drawable.target_ability_movement);
        setRotationY(0f);
        setRotation(0f);
        setRotationX(0f);
    }

    public void setAsTarget(@NonNull InteractionTarget target) {
        this.target = target;
        // This method is made to target a cellView without visual feedacks
    }

    private void rotateAwayFromCell(@NonNull CellView clvCharacter) {
        Point characterCenter = clvCharacter.getCenter();
        Point destCenter = getCenter();

        // We calculate the arcTan with the sender as the origin point and with the Y axis inverted.
        // This gives us the angle between the two points as a radiant that we must convert to degrees
        double angleRad = Math.atan2(destCenter.x - characterCenter.x, -(destCenter.y - characterCenter.y));
        double angle = Math.toDegrees(angleRad);

        setRotationX(0f);
        setRotationY(0f);
        // Finally we normalize then angle to use it as a rotation
        setRotation((int) ((angle % 360 + 360) % 360));
    }

    @Nullable
    public InteractionTarget getTarget() {
        return target;
    }

    public void playHighlight() {
        animate().scaleX(0.85f).scaleY(0.85f).alpha(0.7f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> animate()
                        .scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start()
                ).start();
    }
}
