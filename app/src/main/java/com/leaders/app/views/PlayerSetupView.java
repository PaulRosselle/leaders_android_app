package com.leaders.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.LeaderType;
import com.leaders.app.views.character.CharacterCardPortraitView;
import com.leaders.app.views.character.CharacterView;
import com.leaders.gamelogic.enums.TeamColor;

public class PlayerSetupView extends ConstraintLayout {
    public interface PlayerSetupWatcher {
        void onLeaderTypeChanged(@NonNull LeaderType leaderType);
        void onTeamColorChanged(@NonNull TeamColor teamColor);
    }

    private LeaderType leaderType;
    private TeamColor teamColor;

    private PlayerSetupWatcher watcher;

    private final CharacterCardPortraitView ccpvLeader;
    private final EditText edtName;
    private final CharacterView chvCharacterColor;


    public PlayerSetupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_player_setup, this);

        // Views initialization
        ccpvLeader = findViewById(R.id.ccpvLeader_vwPlayerSetup);
        edtName = findViewById(R.id.edtName_vwPlayerSetup);
        chvCharacterColor = findViewById(R.id.chvCharacterColor_vwPlayerSetup);

        initListeners();
    }

    private void initListeners() {
        ccpvLeader.setOnClickListener(this::onLeaderClick);
        chvCharacterColor.setOnClickListener(this::onCharacterColorClick);
    }

    private void onLeaderClick(View v) {
        LeaderType newLeaderType = leaderType.getNext();
        setLeaderType(newLeaderType);
        if (watcher != null) {
            watcher.onLeaderTypeChanged(newLeaderType);
        }
    }

    private void onCharacterColorClick(View v) {
        TeamColor newTeamColor = teamColor.getOpposite();
        setTeamColor(newTeamColor);
        if (watcher != null) {
            watcher.onTeamColorChanged(newTeamColor);
        }
    }

    public LeaderType getLeaderType() {
        return leaderType;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public String getName() {
        return edtName.getText().toString().trim();
    }

    public void setLeaderType(LeaderType leaderType) {
        this.leaderType = leaderType;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public void setName(@NonNull String name) {
        edtName.setText(name);
    }

    public void setPlayerSetupWatcher(PlayerSetupWatcher watcher) {
        this.watcher = watcher;
    }
}
