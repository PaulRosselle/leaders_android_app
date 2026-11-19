package com.leaders.app.views.duel;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.leaders.R;
import com.leaders.app.entities.WarningDimension;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.enums.PlayerWarning;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.GamePhaseType;

public final class PlayerBottomView extends PlayerView {
    private final TextView txvInfo;
    private final ImageView imvWarning;
    private final ImageView imvInfoBg;

    @Nullable
    private PlayerWarning playerWarning;

    public PlayerBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        txvInfo = findViewById(R.id.txvInfo_vwPlayerBottom);
        imvWarning = findViewById(R.id.imvWarning_actPlayerBottom);
        imvInfoBg = findViewById(R.id.imvInfo_vwPlayerBottom);

        playerWarning = null;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_player_bottom;
    }

    @Override
    protected int getImvTeamResId() {
        return R.id.imvTeam_vwPlayerBottom;
    }

    @Override
    protected int getImvLeaderResId() {
        return R.id.imvLeader_vwPlayerBottom;
    }

    @Override
    protected int getImvWarningResId() {
        return R.id.imvWarning_actPlayerBottom;
    }

    @Override
    protected int getTxvPlayerNameResId() {
        return R.id.txvPlayerName_vwPlayerBottom;
    }

    @Override
    protected int getLeaderResId(@NonNull LeaderType leaderType) {
        switch (leaderType) {
            case King: return R.drawable.player_bottom_king;
            case Queen: return R.drawable.player_bottom_queen;
            case Vermilion: return R.drawable.player_bottom_vermilion;
            default: throw new IllegalStateException("No leader res id found for leader: " + leaderType);
        }
    }

    @Override
    protected int getBackgroundResId(@NonNull Player player) {
        switch (player.getTeamColor()) {
            case Black: return R.drawable.player_bottom_bg_b;
            case White: return R.drawable.player_bottom_bg_w;
            default: throw new IllegalStateException("No background res id found for team: " + player.getTeamColor());
        }
    }

    @Override
    protected WarningDimension getWarningDimensions() {
        return new WarningDimension(277f, 175f, 463f, 286f, 218f, 287f);
    }

    @Override
    protected int getWarningTitle() {
        if (playerWarning == null) {
            throw new IllegalStateException("Warning title should not be requested without a valid warning");
        }
        return playerWarning.getTitleResId();
    }

    @Override
    protected int getWarningMessage() {
        if (playerWarning == null) {
            throw new IllegalStateException("Warning message should not be requested without a valid warning");
        }
        return playerWarning.getMessageResId();
    }

    public void setInfo(@NonNull GamePhaseType gamePhaseType) {
        setInfo(getInfoResId(gamePhaseType),
                gamePhaseType == GamePhaseType.Banishment ?
                R.drawable.player_bottom_bg_info_ban : R.drawable.player_bottom_bg_info
        );
    }

    public void setInfo(@StringRes int infoResId, @DrawableRes int infoBgResId) {
        if (infoResId != -1) {
            txvInfo.setText(infoResId);
        }
        imvInfoBg.setImageResource(infoBgResId);
    }

    public void setInfoVisible(boolean infoVisible) {
        txvInfo.setVisibility(infoVisible ? VISIBLE : INVISIBLE);
        imvInfoBg.setVisibility(infoVisible ? VISIBLE : INVISIBLE);
    }

    public void applyWarning(@Nullable PlayerWarning playerWarning) {
        this.playerWarning = playerWarning;

        boolean warningVisible = playerWarning != null;
        if (warningVisible) {
            imvWarning.setImageResource(playerWarning.getBottomResId());
        }
        setWarningVisible(warningVisible);
    }

    private int getInfoResId(@NonNull GamePhaseType gamePhaseType) {
        switch (gamePhaseType) {
            case Banishment: return R.string.ban_character;
            case Actions: return R.string.play_characters;
            case Recruitment: return R.string.recruit_character;
            default: return -1;
        }
    }
}
