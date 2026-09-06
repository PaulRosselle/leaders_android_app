package com.leaders.app.views.duel;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.LeaderType;
import com.leaders.gamelogic.entities.Player;

public abstract class PlayerView extends ConstraintLayout {
    private static final int WARNING_ANIMATION_DURATION = 3000;

    private final ImageView imvLeader;
    private final ImageView imvWarning;
    private final TextView txvName;
    private final ObjectAnimator animator;

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, getLayoutResId(), this);

        imvLeader = findViewById(getImvLeaderResId());
        imvWarning = findViewById(getImvWarningResId());
        txvName = findViewById(getTxvPlayerNameResId());

        animator = ObjectAnimator.ofFloat(imvWarning, ALPHA, 1f, 0.5f, 1f);
        animator.setDuration(WARNING_ANIMATION_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);

        imvWarning.setOnLongClickListener(this::onWarningLongClick);
    }

    protected abstract int getLayoutResId();

    protected abstract int getImvLeaderResId();
    protected abstract int getImvWarningResId();

    protected abstract int getTxvPlayerNameResId();

    protected abstract int getLeaderResId(@NonNull LeaderType leaderType);

    protected abstract int getBackgroundResId(@NonNull Player player);

    public void setPlayer(@NonNull Player player, @NonNull LeaderType leaderType) {
        txvName.setText(player.getName());
        imvLeader.setImageResource(getLeaderResId(leaderType));
        imvLeader.setBackgroundResource(getBackgroundResId(player));
    }

    public boolean onWarningLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.alert_dialog_theme);

        builder.setTitle(R.string.barrage_warning_title);
        builder.setMessage(R.string.barrage_warning_message);
        builder.setPositiveButton(R.string.ok, null);

        builder.show();

        return true;
    }

    public void setWarningVisible(boolean visible) {
        imvWarning.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            startAnimaton();
        } else {
            stopAnimation();
        }
    }

    private void startAnimaton() {
        if (animator.isStarted()) {
            stopAnimation();
        }
        animator.start();
    }

    public void stopAnimation() {
        animator.cancel();
        imvWarning.setAlpha(1f);
    }
}
