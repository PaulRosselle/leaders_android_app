package com.leaders.app.views.replay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.utilities.GameModeUtils;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@SuppressLint("ViewConstructor") // Cannot be added through the XML editor
public class ReplaySelectorView extends ConstraintLayout {
    public interface OnReplayClickListener {
        void onReplayClick(@NonNull ReplaySelectorView rsvSender);
    }

    public interface OnReplayLongClickListener {
        boolean onReplayLongClick(@NonNull ReplaySelectorView rsvSender);
    }

    private MaterialButton btnMain;
    private ImageView imvChecked;
    private TextView txvName;
    private TextView txvDate;
    private TextView txvGameMode;
    private TextView txvWhitePlayer;
    private TextView txvBlackPlayer;

    private boolean checkboxVisible;
    private boolean isChecked;

    private final ReplaySave replaySave;

    @Nullable
    private OnReplayClickListener onReplayClickListener;
    @Nullable
    private OnReplayLongClickListener onReplayLongClickListener;

    public ReplaySelectorView(@NonNull Context context, @NonNull ReplaySave replaySave) {
        super(context);

        this.replaySave = replaySave;

        inflate(context, R.layout.view_replay_selector, this);

        initViews();
        initListeners();
        loadReplaySave();

        // We apply the default selection behavior
        setCheckboxVisible(false);
        setChecked(false);
    }

    private void initViews() {
        imvChecked = findViewById(R.id.imvChecked_vwReplaySelector);
        btnMain = findViewById(R.id.btnMain_vwReplaySelector);
        txvName = findViewById(R.id.txvName_vwReplaySelector);
        txvDate = findViewById(R.id.txvDate_vwReplaySave);
        txvGameMode = findViewById(R.id.txvGameMode_vwReplaySelector);
        txvWhitePlayer = findViewById(R.id.txvWhitePlayer_vwReplaySelector);
        txvBlackPlayer = findViewById(R.id.txvBlackPlayer_vwReplaySelector);
    }

    private void initListeners() {
        btnMain.setOnClickListener(v -> {
            if (onReplayClickListener != null) {
                onReplayClickListener.onReplayClick(this);
            }
        });
        btnMain.setOnLongClickListener(v -> {
            if (onReplayLongClickListener != null) {
                return onReplayLongClickListener.onReplayLongClick(this);
            }
            return false;
        });
    }

    private String getPlayerName(@NonNull TeamColor teamColor) {
        for (Player player : replaySave.getPlayers()) {
            if (player.getTeamColor() == teamColor) {
                return player.getName();
            }
        }
        throw new IllegalStateException("No player found for team: " + teamColor);
    }

    private void loadReplaySave() {
        // NAME
        txvName.setText(replaySave.getName());
        // DATE
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault());
        txvDate.setText(formatter.format(replaySave.getDate()));
        // GAME MODE
        txvGameMode.setText(GameModeUtils.getName(getContext(), replaySave.getGameMode()));
        // WHITE PLAYER
        txvWhitePlayer.setText(getPlayerName(TeamColor.White));
        // BLACK PLAYER
        txvBlackPlayer.setText(getPlayerName(TeamColor.Black));
    }

    public void setCheckboxVisible(boolean checkboxVisible) {
        this.checkboxVisible = checkboxVisible;
        updateCheckboxVisibleState();
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        updateCheckedState();
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void updateCheckboxVisibleState() {
        imvChecked.setVisibility(checkboxVisible ? VISIBLE : INVISIBLE);
    }

    private void updateCheckedState() {
        imvChecked.setImageResource(isChecked ? R.drawable.checked_box : R.drawable.unchecked_box);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int bgColorId = R.color.darker_background;
        int strokeColorId = R.color.font;
        int mainTextColorId = R.color.font;
        int secondaryTextColorId = R.color.darker_font;
        if (isChecked) {
            strokeWidth *= 2;
            bgColorId = R.color.ultra_dark_background;
            strokeColorId = R.color.white;
            mainTextColorId = R.color.white;
            secondaryTextColorId = R.color.light_gray;
        }

        int mainTextColor = getResources().getColor(mainTextColorId, getContext().getTheme());
        int secondaryTextColor = getResources().getColor(secondaryTextColorId, getContext().getTheme());

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), bgColorId));
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));

        txvName.setTextColor(mainTextColor);
        txvDate.setTextColor(mainTextColor);

        txvGameMode.setTextColor(secondaryTextColor);
        txvWhitePlayer.setTextColor(secondaryTextColor);
        txvBlackPlayer.setTextColor(secondaryTextColor);
    }

    public void setOnReplayClickListener(@Nullable OnReplayClickListener onReplayClickListener) {
        this.onReplayClickListener = onReplayClickListener;
    }

    public void setOnReplayLongClickListener(@Nullable OnReplayLongClickListener onReplayLongClickListener) {
        this.onReplayLongClickListener = onReplayLongClickListener;
    }

    public ReplaySave getReplaySave() {
        return replaySave;
    }
}
