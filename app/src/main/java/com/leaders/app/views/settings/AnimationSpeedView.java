package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.AnimationSpeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimationSpeedView extends ConstraintLayout {
    public interface onSpeedChangeListener {
        void onSpeedChange(@NonNull AnimationSpeed speed);
    }

    @NonNull
    private final SeekBar skbSpeed;
    @NonNull
    private final TextView txvSpeed;

    private List<AnimationSpeed> availableSpeeds;
    private AnimationSpeed speed;

    private onSpeedChangeListener changeListener;

    public AnimationSpeedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        changeListener = null;

        inflate(context, R.layout.view_animation_speed, this);

        skbSpeed = findViewById(R.id.skbSpeed_vwAnimationSpeed);
        txvSpeed = findViewById(R.id.txvSpeed_vwAnimationSpeed);
        setBackgroundResource(R.drawable.round_rect);

        initListeners();

        setAvailableSpeeds(new ArrayList<>(Arrays.asList(AnimationSpeed.values())));
    }

    public void setAvailableSpeeds(@NonNull List<AnimationSpeed> availableSpeeds) {
        if (availableSpeeds.isEmpty()) {
            throw new IllegalArgumentException("At least one animation speed must be available");
        }

        this.availableSpeeds = availableSpeeds;

        skbSpeed.setMax(availableSpeeds.size() - 1);
        // Normal speed is selected by default, the first element is selected otherwise
        setSpeed(availableSpeeds.contains(AnimationSpeed.Normal) ? AnimationSpeed.Normal : availableSpeeds.get(0));
    }

    private void setSpeed(@NonNull AnimationSpeed speed) {
        this.speed = speed;
        skbSpeed.setProgress(availableSpeeds.indexOf(speed));
        txvSpeed.setText(getContext().getString(speed.getNameResId()));

        if (changeListener != null) {
            changeListener.onSpeedChange(speed);
        }
    }

    private void initListeners() {
        skbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setSpeed(availableSpeeds.get(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No treatment here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No treatment here
            }
        });
    }

    public AnimationSpeed getSpeed() {
        return speed;
    }

    public void setChangeListener(onSpeedChangeListener changeListener) {
        this.changeListener = changeListener;
    }
}
