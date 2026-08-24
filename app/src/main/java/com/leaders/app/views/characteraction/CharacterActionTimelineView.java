package com.leaders.app.views.characteraction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.gamelogic.actions.CharacterAction;

import java.util.List;

public class CharacterActionTimelineView extends ConstraintLayout {
    public interface OnTimelineMarkerSelectListener {
        void onMarkerSelected(int markerId);
    }

    private final ImageView imvTimeline;
    private final LinearLayout llyMarkers;
    private final LinearLayout llyActions;

    private int lastDraggedMarker = -1;

    @Nullable
    private OnTimelineMarkerSelectListener onMarkerSelectedListener;

    public CharacterActionTimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_character_action_timeline, this);

        imvTimeline = findViewById(R.id.imvTimeline_vwCharacterActionTimeline);
        llyMarkers = findViewById(R.id.llyMarkers_vwCharacterActionTimeline);
        llyActions = findViewById(R.id.llyActions_vwCharacterActionTimeline);

        setupMarkerTouch();
    }

    public void setActions(@NonNull List<CharacterAction> actions) {
        llyMarkers.removeAllViews();
        llyActions.removeAllViews();

        boolean hasActions = !actions.isEmpty();
        imvTimeline.setVisibility(hasActions ? View.VISIBLE : View.GONE);

        if (!hasActions) {
            return;
        }

        int actionMargin = getResources().getDimensionPixelSize(R.dimen.character_action_inner_margin);
        int actionWidth = getResources().getDimensionPixelSize(R.dimen.character_action_width);
        int actionHeight = getResources().getDimensionPixelSize(R.dimen.character_action_height);

        for (CharacterAction action : actions) {
            llyMarkers.addView(getMarkerView(), getMarkerLayoutParams(actionWidth, actionMargin));
            llyActions.addView(getActionView(action), getActionLayoutParams(actionWidth, actionHeight, actionMargin));
        }
        // Each action is bounded by a marker before and after it, so there is always one more marker than action
        llyMarkers.addView(getMarkerView(), getMarkerLayoutParams(actionWidth, actionMargin));

        selectMarker(0);
    }

    private ImageView getMarkerView() {
        ImageView imvMarker = new ImageView(getContext());
        imvMarker.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imvMarker;
    }

    private CharacterActionView getActionView(@NonNull CharacterAction action) {
        CharacterActionView cavAction = new CharacterActionView(getContext(), action);
        cavAction.setOnClickListener(this::onActionClick);
        return cavAction;
    }

    private LinearLayout.LayoutParams getMarkerLayoutParams(int size, int margin) {
        return getLayoutParams(size, size, margin);
    }

    private LinearLayout.LayoutParams getLayoutParams(int width, int height, int margin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        return layoutParams;
    }

    private LinearLayout.LayoutParams getActionLayoutParams(int width, int height, int margin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        return layoutParams;
    }

    private void selectMarker(int lastMarkerIndex) {
        for (int i = 0; i < llyMarkers.getChildCount(); i++) {
            ((ImageView) llyMarkers.getChildAt(i)).setImageResource(i == lastMarkerIndex ?
                    R.drawable.timeline_marker : R.drawable.timeline_marker_empty);
        }

        for (int i = 0; i < llyActions.getChildCount(); i++) {
            CharacterActionView cavAction = (CharacterActionView) llyActions.getChildAt(i);
            cavAction.setForeground(i < lastMarkerIndex ?
                    ContextCompat.getDrawable(getContext(), R.drawable.round_rect_stroke_golden) : null);
        }
    }

    public void setOnMarkerSelectedListener(@Nullable OnTimelineMarkerSelectListener onMarkerSelectedListener) {
        this.onMarkerSelectedListener = onMarkerSelectedListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMarkerTouch() {
        llyMarkers.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: {
                    // Called to prevent scrollview interfiering with the "seekbar" behavior
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    selectMarkerAtX(event.getX());
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    selectMarkerAtX(event.getX());
                    v.performClick();
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    return true;
            }

            return false;
        });
    }

    private void selectMarkerAtX(float x) {
        if (llyMarkers.getChildCount() == 0) {
            return;
        }

        int markerIndex = getMarkerIndexAtX(x);

        if (markerIndex == lastDraggedMarker) {
            return;
        }

        lastDraggedMarker = markerIndex;

        selectMarker(markerIndex);

        if (onMarkerSelectedListener != null) {
            onMarkerSelectedListener.onMarkerSelected(markerIndex);
        }
    }

    private int getMarkerIndexAtX(float x) {
        int closestIndex = 0;
        float closestDistance = Float.MAX_VALUE;

        for (int i = 0; i < llyMarkers.getChildCount(); i++) {
            View marker = llyMarkers.getChildAt(i);

            float center = marker.getLeft() + marker.getWidth() / 2f;
            float distance = Math.abs(x - center);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    private void onActionClick(View v) {
        int markerIndex = llyActions.indexOfChild(v) + 1;
        selectMarker(markerIndex);

        if (onMarkerSelectedListener != null) {
            onMarkerSelectedListener.onMarkerSelected(markerIndex);
        }
    }
}
