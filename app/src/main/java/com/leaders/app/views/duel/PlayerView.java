package com.leaders.app.views.duel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.gamelogic.entities.Player;

public abstract class PlayerView extends ConstraintLayout {
    protected ImageView imvLeader;
    protected TextView txvName;

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, getLayoutResId(), this);

        imvLeader = findViewById(getImvLeaderResId());
        txvName = findViewById(getTxvPlayerNameResId());
    }

    protected abstract int getLayoutResId();

    protected abstract int getImvLeaderResId();

    protected abstract int getTxvPlayerNameResId();

    protected abstract int getLeaderResId();

    protected abstract int getLeaderBackgroundResId();

    public void setPlayer(@NonNull Player player) {
        txvName.setText(player.getName());
        imvLeader.setImageResource(getLeaderResId());
        imvLeader.setBackgroundResource(getLeaderBackgroundResId());
    }
}
