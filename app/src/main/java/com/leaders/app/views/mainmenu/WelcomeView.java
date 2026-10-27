package com.leaders.app.views.mainmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class WelcomeView extends ConstraintLayout {
    public enum Action {
        START_TUTORIAL,
        CONTINUE
    }

    public interface Listener {
        void onActionClick(@NonNull Action action);
    }

    private Listener listener;

    public WelcomeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_welcome, this);

        findViewById(R.id.clyMain_vwWelcome).setOnClickListener(this::clyMainClick);
        findViewById(R.id.btnStartTutorial_vwWelcome).setOnClickListener(this::onStartTutorialClick);
        findViewById(R.id.btnContinue_vwWelcome).setOnClickListener(this::onContinueClick);
    }

    private void clyMainClick(View v) {
        // Only called to stop the click listener propagation
    }

    private void onStartTutorialClick(View v) {
        close(Action.START_TUTORIAL);
    }

    private void onContinueClick(View v) {
        close(Action.CONTINUE);
    }

    private void close(@NonNull Action action) {
        setVisibility(GONE);

        if (listener != null) {
            listener.onActionClick(action);
        }
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }
}
