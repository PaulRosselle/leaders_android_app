package com.leaders.app.views.selector;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectorGroupView<T, V extends SelectorView<T>> extends LinearLayout {
    private final List<V> selectorViews = new ArrayList<>();

    public interface OnSelectionChangeListener {
        void onSelectionChange();
    }

    private boolean singleSelection = true;

    @Nullable
    private OnSelectionChangeListener selectionChangeListener;

    protected SelectorGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
    }

    /**
     * Creates the view representing an item.
     */
    @NonNull
    protected abstract V createSelectorView(@NonNull T item);

    /**
     * Replaces all items displayed by this group.
     */
    protected final void setItems(@NonNull List<? extends T> items) {
        removeAllViews();
        selectorViews.clear();

        singleSelection = true;

        for (T item : items) {
            V selectorView = createSelectorView(item);

            selectorView.setOnSelectorClickListener(sender -> onSelectorClick(selectorView));
            selectorView.setOnSelectorLongClickListener(sender -> onSelectorLongClick(selectorView));

            selectorViews.add(selectorView);
            addView(selectorView, getSelectorLayoutParams());
        }

        notifySelectionChanged();
    }

    @NonNull
    protected final List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<>();

        for (V selectorView : selectorViews) {
            if (selectorView.isChecked()) {
                selectedItems.add(selectorView.getItem());
            }
        }

        return selectedItems;
    }

    protected final void selectAll() {
        setSingleSelection(false);
        setAllChecked(true);
        notifySelectionChanged();
    }

    protected final void clearSelection() {
        setAllChecked(false);
        setSingleSelection(true);
        notifySelectionChanged();
    }

    public final void setSelectionChangeListener(@Nullable OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    private void setAllChecked(boolean checked) {
        for (V selectorView : selectorViews) {
            selectorView.setChecked(checked);
        }
    }

    private void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;

        for (V selectorView : selectorViews) {
            selectorView.setCheckboxVisible(!singleSelection);
        }
    }

    private boolean hasSelectedItems() {
        for (V selectorView : selectorViews) {
            if (selectorView.isChecked()) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private Integer getClosestSelectedIndex(int refIndex) {
        int closestIndex = Integer.MAX_VALUE;

        for (int selectorIdx = 0; selectorIdx < selectorViews.size(); selectorIdx++) {
            if (selectorIdx != refIndex && selectorViews.get(selectorIdx).isChecked() &&
                    (closestIndex == Integer.MAX_VALUE ||
                            Math.abs(selectorIdx - refIndex) < Math.abs(closestIndex - refIndex))) {
                closestIndex = selectorIdx;
            }
        }

        return closestIndex != Integer.MAX_VALUE ? closestIndex : null;
    }

    private void onSelectorClick(@NonNull V sender) {
        if (!singleSelection) {
            sender.setChecked(!sender.isChecked());
            if (!hasSelectedItems()) {
                setSingleSelection(true);
            }

        } else if (!sender.isChecked()) {
            setAllChecked(false);
            sender.setChecked(true);
        }

        notifySelectionChanged();
    }

    private boolean onSelectorLongClick(@NonNull V sender) {
        if (singleSelection) {
            setAllChecked(false);
            setSingleSelection(false);
            sender.setChecked(true);

        } else {
            int selectorIndex = indexOfChild(sender);
            Integer closestSelectedIndex = getClosestSelectedIndex(selectorIndex);

            if (closestSelectedIndex != null) {

                int startIndex;
                int endIndex;
                if (selectorIndex < closestSelectedIndex) {
                    startIndex = selectorIndex;
                    endIndex = closestSelectedIndex;
                } else {
                    startIndex = closestSelectedIndex + 1;
                    endIndex = selectorIndex + 1;
                }

                for (int i = startIndex; i <= endIndex; i++) {
                    selectorViews.get(i).setChecked(true);
                }
            }
        }

        notifySelectionChanged();

        return true;
    }

    @NonNull
    private LinearLayout.LayoutParams getSelectorLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        );

        layoutParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);

        return layoutParams;
    }

    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChange();
        }
    }
}