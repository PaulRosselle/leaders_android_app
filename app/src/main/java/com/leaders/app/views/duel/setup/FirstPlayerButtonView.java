package com.leaders.app.views.duel.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.enums.FirstPlayerTeam;

public class FirstPlayerButtonView extends ConstraintLayout {
    private final ImageView imvTeam;
    private final TextView txvTeam;

    private boolean isSelectedTeam;

    @NonNull
    private FirstPlayerTeam team;

    public FirstPlayerButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        imvTeam = findViewById(R.id.imvTeam_vwFirstPlayerButton);
        txvTeam = findViewById(R.id.txvTeam_vwFirstPlayerButton);

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
                txvTeam.setText("alÉatoire"); // TODO - extract
            } break;
            case Black: {
                imvTeam.setImageResource(R.drawable.character_piece_empty_b);
                txvTeam.setText("noir"); // TODO - extract
            } break;
            case White: {
                imvTeam.setImageResource(R.drawable.character_piece_empty_w);
                txvTeam.setText("blanc"); // TODO - extract
            } break;
            default: throw new IllegalStateException("Unsupported first team: " + team);
        }
    }

    public void setTeamSelected(boolean isSelectedTeam) {
        this.isSelectedTeam = isSelectedTeam;
        AppCompatResources.getColorStateList(getContext(),
                isSelectedTeam ? R.color.selected_golden : R.color.darker_background
        );
    }

    public boolean isSelectedTeam() {
        return isSelectedTeam;
    }
}
