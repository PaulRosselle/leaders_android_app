package com.leaders.app.views.duel;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.enums.LeaderType;
import com.leaders.gamelogic.entities.Player;

public final class PlayerTopView extends PlayerView {
    public PlayerTopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_player_top;
    }

    @Override
    protected int getImvLeaderResId() {
        return R.id.imvLeader_vwPlayerTop;
    }

    @Override
    protected int getTxvPlayerNameResId() {
        return R.id.txvPlayerName_vwPlayerTop;
    }

    @Override
    protected int getLeaderResId(@NonNull LeaderType leaderType) {
        switch (leaderType) {
            case King: return R.drawable.player_top_king;
            case Queen: return R.drawable.player_top_queen;
            default: throw new IllegalStateException("No leader res id found for leader: " + leaderType);
        }
    }

    @Override
    protected int getBackgroundResId(@NonNull Player player) {
        switch (player.getTeamColor()) {
            case Black: return R.drawable.player_top_bg_b;
            case White: return R.drawable.player_top_bg_w;
            default: throw new IllegalStateException("No background res id found for team: " + player.getTeamColor());
        }
    }
}
