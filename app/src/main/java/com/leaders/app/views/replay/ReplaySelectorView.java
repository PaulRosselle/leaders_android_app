package com.leaders.app.views.replay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.utilities.GameModeUtils;
import com.leaders.app.views.selector.SelectorView;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.TeamColor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public final class ReplaySelectorView extends SelectorView<ReplaySave> {
    private TextView txvName;
    private TextView txvDate;
    private TextView txvGameMode;
    private TextView txvWhitePlayer;
    private TextView txvBlackPlayer;

    ReplaySelectorView(@NonNull Context context, @NonNull ReplaySave item) {
        super(context, item);
    }

    protected void initViews() {
        super.initViews();

        txvName = findViewById(R.id.txvName_vwReplaySelector);
        txvDate = findViewById(R.id.txvDate_vwReplaySave);
        txvGameMode = findViewById(R.id.txvGameMode_vwReplaySelector);
        txvWhitePlayer = findViewById(R.id.txvWhitePlayer_vwReplaySelector);
        txvBlackPlayer = findViewById(R.id.txvBlackPlayer_vwReplaySelector);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        ReplaySave replaySave = getItem();
        // NAME
        txvName.setText(replaySave.getName());
        // DATE
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault());
        txvDate.setText(formatter.format(replaySave.getDate()));
        // GAME MODE
        txvGameMode.setText(GameModeUtils.getName(getContext(), replaySave.getGameMode()));
        // PLAYERS
        txvWhitePlayer.setText(getPlayerName(TeamColor.White));
        txvBlackPlayer.setText(getPlayerName(TeamColor.Black));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_replay_selector;
    }

    @Override
    protected int getBtnMainResId() {
        return R.id.btnMain_vwReplaySelector;
    }

    @Override
    protected int getImvCheckedResId() {
        return R.id.imvChecked_vwReplaySelector;
    }

    private String getPlayerName(@NonNull TeamColor teamColor) {
        for (Player player : getItem().getPlayers()) {
            if (player.getTeamColor() == teamColor) {
                return player.getName();
            }
        }

        throw new IllegalStateException("No player found for team: " + teamColor);
    }

    @Override
    protected void updateCheckboxVisibleState() {
        imvChecked.setVisibility(isCheckboxVisible() ? VISIBLE : INVISIBLE);
    }

    @Override
    protected void updateCheckedState() {
        imvChecked.setImageResource(isChecked() ? R.drawable.checked_box : R.drawable.unchecked_box);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int backgroundColorId = R.color.darker_background;
        int strokeColorId = R.color.font;
        int mainTextColorId = R.color.font;
        int secondaryTextColorId = R.color.darker_font;

        if (isChecked()) {
            strokeWidth *= 2;
            backgroundColorId = R.color.ultra_dark_background;
            strokeColorId = R.color.white;
            mainTextColorId = R.color.white;
            secondaryTextColorId = R.color.light_gray;
        }

        int mainTextColor = getResources().getColor(mainTextColorId, getContext().getTheme());
        int secondaryTextColor = getResources().getColor(secondaryTextColorId, getContext().getTheme());

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), backgroundColorId));
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));

        txvName.setTextColor(mainTextColor);
        txvDate.setTextColor(mainTextColor);

        txvGameMode.setTextColor(secondaryTextColor);
        txvWhitePlayer.setTextColor(secondaryTextColor);
        txvBlackPlayer.setTextColor(secondaryTextColor);
    }

    @NonNull
    public ReplaySave getReplaySave() {
        return getItem();
    }
}