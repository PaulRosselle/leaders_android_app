package com.leaders.app.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.leaders.R;
import com.leaders.app.enums.BoardOrientation;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Position;

public class CellView extends AppCompatImageView {
    @NonNull
    private Cell cell;

    public CellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Initialized with a default cell at (0,0)
        cell = new Cell(new Position(0, 0));
    }

    private Point getCenter() {
        return new Point((int) (getX() + getWidth() / 2f), (int) (getY() + getHeight() / 2f));
    }

    public void clearTarget() {
        setVisibility(GONE);
    }

    public void setAsRecruitmentTarget(@NonNull BoardOrientation boardOrientation) {
        int columnAxisPos = cell.getPosition().getQ();
        setImageResource(columnAxisPos == 0 ? R.drawable.target_recruitment_center : R.drawable.target_recruitment_side);
        setRotationY(columnAxisPos > 0 || (boardOrientation == BoardOrientation.Rotated && columnAxisPos < 0) ? 180f : 0f);
        setRotation(0f);
        setRotationX(0f);
        setVisibility(VISIBLE);
    }

    public void setAsMovementTarget(@NonNull CellView clvCharacter) {
        setImageResource(R.drawable.target_movement);
        setAsCharacterActionTarget(clvCharacter);
        setVisibility(VISIBLE);
    }

    public void setAsAbilityMovementTarget(@NonNull CellView clvCharacter) {
        setImageResource(R.drawable.target_ability_movement);
        setAsCharacterActionTarget(clvCharacter);
        setVisibility(VISIBLE);
    }

    private void setAsCharacterActionTarget(@NonNull CellView clvCharacter) {
        if (clvCharacter.cell.getCharacter() == null) {
            throw new IllegalArgumentException("A character action cell cannot be target without a valid character cell");
        }

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

    @NonNull
    public Cell getCell() {
        return cell;
    }

    public void setCell(@NonNull Cell cell) {
        this.cell = cell;
    }
}
