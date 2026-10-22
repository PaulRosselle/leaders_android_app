package com.leaders.app.views.selector;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectorGroupView<T, V extends SelectorView<T>> extends GridLayout {
    private final List<V> selectorViews = new ArrayList<>();

    public interface OnSelectionChangeListener {
        void onSelectionChange();
    }

    private boolean singleSelection = true;

    @Nullable
    private OnSelectionChangeListener selectionChangeListener;

    protected SelectorGroupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // HORIZONTAL (default): items fill rows first. Do NOT use VERTICAL here.
        setOrientation(HORIZONTAL);
        setColumnCount(1); // list mode by default
    }

    /**
     * Creates the view representing an item.
     */
    @NonNull
    protected abstract V createSelectorView(@NonNull T item);

    // ---------------------------------------------------------------------------------------------
    // Display mode
    // ---------------------------------------------------------------------------------------------

    /**
     * Number of columns: 1 = list, 2 or more = grid.
     */
    public final void setSpanCount(int spanCount) {
        int safeSpanCount = Math.max(1, spanCount);

        if (safeSpanCount == getColumnCount()) {
            return;
        }

        setColumnCount(safeSpanCount);

        // Margins and column specs depend on the column index, so refresh them.
        for (int i = 0; i < selectorViews.size(); i++) {
            selectorViews.get(i).setLayoutParams(createLayoutParams(i));
        }
    }

    public final int getSpanCount() {
        return getColumnCount();
    }

    public final boolean isGrid() {
        return getColumnCount() > 1;
    }

    // ---------------------------------------------------------------------------------------------
    // Items
    // ---------------------------------------------------------------------------------------------

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

            addView(selectorView, createLayoutParams(selectorViews.size()));
            selectorViews.add(selectorView);
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

    public boolean isSingleSelection() {
        return singleSelection;
    }

    // ---------------------------------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------------------------------

    @NonNull
    private GridLayout.LayoutParams createLayoutParams(int index) {
        int spacing = (int) (4 * getResources().getDisplayMetrics().density);
        int column = index % getColumnCount();

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                // Row: let the grid place it, stretch to the tallest cell of the row
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL),
                // Column: equal weight, so all columns share the width evenly
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
        );

        params.width = 0; // required for the column weight to apply
        params.height = LayoutParams.WRAP_CONTENT;
        params.topMargin = spacing;

        if (column > 0) {
            params.setMarginStart(spacing);
        }

        return params;
    }

    // ---------------------------------------------------------------------------------------------
    // Selection logic (unchanged)
    // ---------------------------------------------------------------------------------------------

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
            // Children are still direct children, added in order: indexOfChild() stays valid
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
                    endIndex = selectorIndex;
                }

                for (int i = startIndex; i <= endIndex; i++) {
                    selectorViews.get(i).setChecked(true);
                }
            }
        }

        notifySelectionChanged();

        return true;
    }

    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChange();
        }
    }
}