package com.leaders.app.views.mainmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.entities.crash.CrashLog;
import com.leaders.app.utilities.ContactUtils;
import com.leaders.app.utilities.JsonUtils;

public final class CrashLogView extends ConstraintLayout {
    private final static String SHARE_LOG_SUBJECT = "CRASH LOG";
    private final static String SHARE_LOG_TITLE = "SHARE CRASH LOG";
    private CrashLog FCrashLog;

    public CrashLogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_crash_log, this);
        findViewById(R.id.clyMain_vwCrashLog).setOnClickListener(this::clyMainClick);
        findViewById(R.id.btnExit_vwCrashLog).setOnClickListener(this::btnExitClick);
        findViewById(R.id.btnMail_vwCrashLog).setOnClickListener(this::btnMailClick);
    }

    public void show(@NonNull CrashLog crashLog) {
        FCrashLog = crashLog;
        setVisibility(VISIBLE);
    }

    private void clyMainClick(View v) {
        // Only called to stop the click listener propagation
    }

    private void btnExitClick(View v) {
        deleteLogAndClose();
    }

    private void btnMailClick(View v) {
        ContactUtils.sendMessage(getContext(), SHARE_LOG_TITLE, SHARE_LOG_SUBJECT, FCrashLog.getFullMessage());

        deleteLogAndClose();
    }

    private void deleteLogAndClose() {
        JsonUtils.deleteCrashLog(getContext());
        FCrashLog = null;
        setVisibility(GONE);
    }
}

