package com.leaders.app.views.replay;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.entities.ReplaySave;
import com.leaders.app.views.selector.SelectorGroupView;

import java.util.List;

public final class ReplaySelectorGroupView extends SelectorGroupView<ReplaySave, ReplaySelectorView> {
    public ReplaySelectorGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    protected ReplaySelectorView createSelectorView(@NonNull ReplaySave replaySave) {
        return new ReplaySelectorView(getContext(), replaySave);
    }

    public void setReplays(@NonNull List<ReplaySave> replaySaves) {
        setItems(replaySaves);
    }

    @NonNull
    public List<ReplaySave> getSelectedReplays() {
        return getSelectedItems();
    }

    public void selectAllReplays() {
        selectAll();
    }

    public void clearReplaySelection() {
        clearSelection();
    }
}