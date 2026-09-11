package com.leaders.app.views.rules;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.app.activities.BaseActivity;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.TutorialChapter;

public final class TutorialNavigationView extends ConstraintLayout {
    public interface IRulesNavigation {
        TutorialChapter getChapter();
        void goToChapter(@NonNull TutorialChapter chapter);
    }

    @NonNull
    private final TextView txvChapterNumber;
    @NonNull
    private final TextView txvChapterName;
    @NonNull
    private final MaterialButton btnChangeChapter;


    private IRulesNavigation navigator;
    private final ArrayAdapter<String> chapterNamesAdapter;


    public TutorialNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_tutorial_navigation, this);

        txvChapterNumber = findViewById(R.id.txvChapterNumber_vwTutorialNavigation);
        txvChapterName = findViewById(R.id.txvChapterName_vwTutorialNavigation);
        btnChangeChapter = findViewById(R.id.btnChangeChapter_vwTutorialNavigation);

        chapterNamesAdapter = new ArrayAdapter<>(
                context,
                R.layout.res_navigation_chapter_item,
                getResources().getStringArray(R.array.tutorial_chapter_names)
        );

        txvChapterNumber.setOnClickListener(this::txvChapterClick);
        txvChapterName.setOnClickListener(this::txvChapterClick);
        btnChangeChapter.setOnClickListener(this::btnChangeChapterClick);
    }

    private void btnChangeChapterClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.alert_dialog_theme);
        builder.setSingleChoiceItems(chapterNamesAdapter, 0,
                (dialog, which) -> {
                    goToChapter(TutorialChapter.values()[which]);
                    dialog.dismiss();
                });
        builder.show();
    }

    private void txvChapterClick(View v) {
        // A click on the txvChapter simulate a click on the change chapter button
        float btnCenter = btnChangeChapter.getWidth() / 2f;
        long now = SystemClock.uptimeMillis();

        // We create the motion events. The up event occurs 150ms after the down event so
        // the on click ripple animation can play
        MotionEvent downEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, btnCenter, btnCenter, 0);
        MotionEvent upEvent = MotionEvent.obtain(now, now + 150, MotionEvent.ACTION_UP, btnCenter, btnCenter, 0);
        try {
            // We can dispatch the events immediately since their eventTime will be used to start the behaviors related to them
            btnChangeChapter.dispatchTouchEvent(downEvent);
            btnChangeChapter.dispatchTouchEvent(upEvent);
        } finally {
            // Once the events have been dispatched they must be recycled (to avoid memory leaks)
            downEvent.recycle();
            upEvent.recycle();
        }
    }


    private void goToChapter(@NonNull TutorialChapter chapter) {
        if (navigator == null) {
            throw new IllegalStateException("Rules navigation impossible: navigator missing");
        }

        navigator.goToChapter(chapter);
    }

    public void setNavigator(@NonNull IRulesNavigation navigator) {
        this.navigator = navigator;

        TutorialChapter chapter = navigator.getChapter();
        txvChapterName.setText(chapter.getNameResId());
        txvChapterNumber.setText(chapter.getNumberResId());
    }
}
