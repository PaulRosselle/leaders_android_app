package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.enums.EndGameType;
import com.leaders.app.enums.LeaderType;

public class EndGameView extends ConstraintLayout {
    private static final int SHOW_ANIMATION_DURATION = 400;
    private static final int HIDE_ANIMATION_DURATION = 200;

    private final ImageView imvLeader;
    private final ImageView imvTitleBg;
    private final TextView txvTitle;
    private final TextView txvSubTitle;
    private final ImageView imvSideLeft, imvSideRight;

    public EndGameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Initializing views
        imvLeader = findViewById(R.id.imvLeader_vwEndGame);
        imvTitleBg = findViewById(R.id.imvTitleBg_vwEndGame);
        txvTitle = findViewById(R.id.txvTitle_vwEndGame);
        txvSubTitle = findViewById(R.id.txvSubTitle_vwEndGame);
        imvSideLeft = findViewById(R.id.imvDecoLeft_vwEndGame);
        imvSideRight = findViewById(R.id.imvDecoRight_vwEndGame);
    }

    public void show() {
        setClickable(false);
        setAlpha(0f);
        setVisibility(View.VISIBLE);

        int endScaleDuration = SHOW_ANIMATION_DURATION * 3 / 4;

        animate().alpha(1f).scaleY(1.05f).scaleX(1.05f)
                .setDuration(SHOW_ANIMATION_DURATION)
                .withEndAction(() -> animate()
                        .scaleY(1f).scaleX(1f)
                        .setDuration(endScaleDuration)
                        .withEndAction(() -> setClickable(true))
                        .start()
                )
                .start();
    }

    public void hide() {
        setClickable(false);

        animate().alpha(0f)
                .setDuration(HIDE_ANIMATION_DURATION)
                .withEndAction(() -> {
                    setVisibility(View.GONE);
                    setAlpha(1f);
                    setClickable(true);
                })
                .start();
    }

    public void update(@NonNull EndGameType endType, @NonNull LeaderType leaderType,
                       @NonNull String title, @NonNull String subTitle) {
        updateLeader(endType, leaderType);
        updateDisplay(endType);
        txvTitle.setText(title);
        txvSubTitle.setText(subTitle);
    }

    private void updateLeader(@NonNull EndGameType endType, @NonNull LeaderType leaderType) {
        int leaderDrawableId;

        switch (leaderType) {
            case King: {
                switch (endType) {
                    case Victory: leaderDrawableId = R.drawable.end_game_victory_king; break;
                    case Defeat: leaderDrawableId = R.drawable.end_game_defeat_king; break;
                    default: throw new RuntimeException("Updating end game leader : Invalid end game type");
                }
            } break;
            case Queen: {
                switch (endType) {
                    case Victory: leaderDrawableId = R.drawable.end_game_victory_queen; break;
                    case Defeat: leaderDrawableId = R.drawable.end_game_defeat_queen; break;
                    default: throw new RuntimeException("Updating end game leader : Invalid end game type");
                }
            } break;
            default: throw new RuntimeException("Updating end game leader : Invalid leader type");
        }

        imvLeader.setImageResource(leaderDrawableId);
    }

    private void updateDisplay(@NonNull EndGameType endType) {
        int endColorId;
        switch (endType) {
            case Victory: {
                endColorId = R.color.app_golden;
                txvSubTitle.setBackgroundResource(R.drawable.end_game_victory_subtitle_bg);
                imvSideLeft.setImageResource(R.drawable.end_game_victory_side);
                imvSideRight.setImageResource(R.drawable.end_game_victory_side);
            } break;
            case Defeat: {
                endColorId = R.color.app_red;
                txvSubTitle.setBackgroundResource(R.drawable.end_game_defeat_subtitle_bg);
                imvSideLeft.setImageResource(R.drawable.end_game_defeat_side);
                imvSideRight.setImageResource(R.drawable.end_game_defeat_side);
            } break;
            default: throw new RuntimeException("Updating end game kind : Invalid end game kind");
        }

        txvTitle.setTextColor(getContext().getColor(endColorId));
        imvTitleBg.setForegroundTintList(ContextCompat.getColorStateList(getContext(), endColorId));
    }
}
