package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.enums.FirstPlayerTeam;

public class FirstPlayerButtonView extends ConstraintLayout {
    public interface OnTeamSelectedListener {
        void onTeamSelectClick(@NonNull FirstPlayerTeam selectedTeam);
    }

    private final MaterialButton btnBackground;
    private final ImageView imvTeam;
    private final TextView txvTeam;

    private boolean isSelectedTeam;

    @NonNull
    private FirstPlayerTeam team;

    private OnTeamSelectedListener onTeamSelectedListener;

    public FirstPlayerButtonView(@NonNull Context context, @NonNull FirstPlayerTeam team) {
        this(context, (AttributeSet) null);
        setTeam(team);
    }

    public FirstPlayerButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_first_player_button, this);

        btnBackground = findViewById(R.id.btnBackground_vwFirstPlayerButton);
        imvTeam = findViewById(R.id.imvTeam_vwFirstPlayerButton);
        txvTeam = findViewById(R.id.txvTeam_vwFirstPlayerButton);

        btnBackground.setOnClickListener(view -> {
            if (onTeamSelectedListener != null) {
                onTeamSelectedListener.onTeamSelectClick(getTeam());
            }
        });

        setTeam(FirstPlayerTeam.Random);
    }

    public void setTeam(@NonNull FirstPlayerTeam team) {
        this.team = team;
        update();
    }

    @NonNull
    public FirstPlayerTeam getTeam() {
        return team;
    }

    private void update() {
        switch (team) {
            case Random: {
                imvTeam.setImageResource(R.drawable.icon_question);
                txvTeam.setText(R.string.random);
            } break;
            case Black: {
                imvTeam.setImageResource(R.drawable.character_piece_empty_b);
                txvTeam.setText(R.string.black_name);
            } break;
            case White: {
                imvTeam.setImageResource(R.drawable.character_piece_empty_w);
                txvTeam.setText(R.string.white_name);
            } break;
            default: throw new IllegalStateException("Unsupported first team: " + team);
        }
        btnBackground.setBackgroundTintList(AppCompatResources.getColorStateList(getContext(),
                isSelectedTeam ? R.color.selected_golden : R.color.darker_background
        ));

    }

    public void setTeamSelected(boolean isSelectedTeam) {
        this.isSelectedTeam = isSelectedTeam;
        update();
    }

    public boolean isSelectedTeam() {
        return isSelectedTeam;
    }

    public void setOnTeamSelectedListener(OnTeamSelectedListener onTeamSelectedListener) {
        this.onTeamSelectedListener = onTeamSelectedListener;
    }
}
