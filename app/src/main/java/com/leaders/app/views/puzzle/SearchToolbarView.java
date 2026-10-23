package com.leaders.app.views.puzzle;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.leaders.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic search / sort / filter toolbar: it knows neither the data type
 * nor the search logic. It displays the UI, stores the selection, and notifies
 * listeners of changes.
 */
public final class SearchToolbarView extends LinearLayout {
    public interface SearchToolbarListener {
        /** Called after debouncing when the user changes the text. */
        void onSearchKeyChanged(@NonNull String searchKey);

        /** @param sortItemId ID of the item selected from the menu resource passed to setSortMenu. */
        void onSortChanged(@IdRes int sortItemId);

        /** @param selection groupId -> IDs of the selected options. Groups with no selection are omitted. */
        void onFiltersChanged(@NonNull Map<String, Set<String>> selection);
    }

    private static final long SEARCH_DELAY_MS = 300L;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::notifySearchKeyChanged;

    private final EditText edtSearch;
    private final MaterialButton btnFilter;
    private final MaterialButton btnSort;

    // Invariant: no empty sets in this map.
    private final Map<String, Set<String>> selectedFilters = new HashMap<>();
    private List<SearchToolbarFilterGroup> filterGroups = Collections.emptyList();

    @MenuRes
    private int sortMenuRes = 0;
    @IdRes
    private int selectedSortItemId = View.NO_ID;

    private boolean silent;

    @Nullable
    private SearchToolbarListener listener;
    @Nullable
    private PopupWindow filterPopup;

    public SearchToolbarView(@NonNull Context context) {
        this(context, null);
    }

    public SearchToolbarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchToolbarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.view_search_toolbar, this);

        edtSearch = findViewById(R.id.edtSearch_vwSearchToolbar);
        btnFilter = findViewById(R.id.btnFilter_vwSearchToolbar);
        btnSort = findViewById(R.id.btnSort_vwSearchToolbar);

        initListeners();
    }

    public void setListener(@Nullable SearchToolbarListener listener) {
        this.listener = listener;
    }

    /** Sets the sort menu resource and the initially selected item. Does not notify listeners. */
    public void setSortMenu(@MenuRes int menuRes, @IdRes int selectedItemId) {
        this.sortMenuRes = menuRes;
        this.selectedSortItemId = selectedItemId;
    }

    /**
     * Sets the filter groups. Selected options that no longer exist are removed;
     * if the selection changes as a result, the listener is notified.
     */
    public void setFilterGroups(@NonNull List<SearchToolbarFilterGroup> groups) {
        dismissFilterPopup();
        filterGroups = new ArrayList<>(groups);
        if (pruneSelection()) {
            notifyFiltersChanged();
        }
    }

    /** Replaces the selection (state restoration). Does not notify listeners. */
    public void setSelectedFilters(@NonNull Map<String, Set<String>> selection) {
        selectedFilters.clear();
        for (Map.Entry<String, Set<String>> entry : selection.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                selectedFilters.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
            }
        }
    }

    /** Replaces the entered text (state restoration). Does not notify listeners. */
    public void setSearchKey(@NonNull String searchKey) {
        if (edtSearch.getText().toString().equals(searchKey)) {
            return;
        }
        searchHandler.removeCallbacks(searchRunnable);
        silent = true;
        try {
            edtSearch.setText(searchKey);
        } finally {
            silent = false;
        }
    }

    private void initListeners() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (silent) {
                    return;
                }
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSort.setOnClickListener(v -> showSortMenu());
        btnFilter.setOnClickListener(v -> showFilterPopup());
    }

    private void notifySearchKeyChanged() {
        if (listener != null) {
            listener.onSearchKeyChanged(edtSearch.getText().toString().trim());
        }
    }

    private void showSortMenu() {
        if (sortMenuRes == 0) {
            return;
        }
        PopupMenu popup = new PopupMenu(new ContextThemeWrapper(getContext(), R.style.popup_menu), btnSort);
        popup.inflate(sortMenuRes);

        Menu menu = popup.getMenu();
        menu.setGroupCheckable(Menu.NONE, true, true);
        MenuItem selected = menu.findItem(selectedSortItemId);
        if (selected != null) {
            selected.setChecked(true);
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != selectedSortItemId) {
                selectedSortItemId = item.getItemId();
                if (listener != null) {
                    listener.onSortChanged(selectedSortItemId);
                }
            }
            return true;
        });
        popup.show();
    }

    private void showFilterPopup() {
        if (filterGroups.isEmpty()) {
            return;
        }
        dismissFilterPopup();

        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.popup_search_toolbar_filters, this, false);
        LinearLayout llGroups = content.findViewById(R.id.llGroups_popSearchToolbarFilters);
        View btnReset = content.findViewById(R.id.btnReset_popSearchToolbarFilters);

        populateFilterGroups(llGroups);
        btnReset.setOnClickListener(v -> {
            if (selectedFilters.isEmpty()) {
                return;
            }
            selectedFilters.clear();
            populateFilterGroups(llGroups);
            notifyFiltersChanged();
        });

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int width = Math.min(dp(320), screenWidth - dp(32));
        int maxHeight = (int) (screenHeight * 0.6f);

        content.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
        int height = Math.min(content.getMeasuredHeight(), maxHeight);

        PopupWindow popup = new PopupWindow(content, width, height, true);
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8));
        popup.setBackgroundDrawable(createPopupBackground());
        popup.setOnDismissListener(() -> filterPopup = null);

        int[] location = new int[2];
        btnFilter.getLocationOnScreen(location);
        boolean anchorOnRightHalf = location[0] + btnFilter.getWidth() / 2 > screenWidth / 2;
        popup.showAsDropDown(btnFilter, 0, dp(4), anchorOnRightHalf ? Gravity.END : Gravity.START);
        filterPopup = popup;
    }

    private void populateFilterGroups(@NonNull LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        for (SearchToolbarFilterGroup group : filterGroups) {
            View groupView = inflater.inflate(R.layout.item_search_toolbar_filter_group, container, false);
            TextView tvTitle = groupView.findViewById(R.id.tvTitle_itmSearchToolbarFilterGroup);
            ChipGroup chipGroup = groupView.findViewById(R.id.cgOptions_itmSearchToolbarFilterGroup);

            tvTitle.setText(group.getTitle());
            chipGroup.setSingleSelection(!group.isMultipleChoice());

            Set<String> selected = selectedFilters.get(group.getId());
            for (SearchToolbarFilterGroup.Option option : group.getOptions()) {
                Chip chip = (Chip) inflater.inflate(R.layout.item_search_toolbar_filter_chip, chipGroup, false);
                chip.setId(View.generateViewId()); // Required for chipgroup
                chip.setTag(option.getId());
                chip.setText(option.getLabel());
                chipGroup.addView(chip);
                chip.setChecked(selected != null && selected.contains(option.getId()));
            }
            // Attached last to avoid notifications during initialization.
            chipGroup.setOnCheckedStateChangeListener((g, checkedIds) ->
                    onChipGroupChanged(group, chipGroup)
            );
            container.addView(groupView);
        }
    }

    private void onChipGroupChanged(@NonNull SearchToolbarFilterGroup group, @NonNull ChipGroup chipGroup) {
        Set<String> checked = new LinkedHashSet<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked()) {
                checked.add((String) child.getTag());
            }
        }
        if (checked.isEmpty()) {
            selectedFilters.remove(group.getId());
        } else {
            selectedFilters.put(group.getId(), checked);
        }
        notifyFiltersChanged();
    }

    /** @return true if the selection has changed. */
    private boolean pruneSelection() {
        boolean changed = false;
        Iterator<Map.Entry<String, Set<String>>> it = selectedFilters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Set<String>> entry = it.next();
            SearchToolbarFilterGroup group = findGroup(entry.getKey());
            if (group == null) {
                it.remove();
                changed = true;
                continue;
            }
            if (entry.getValue().retainAll(group.getOptionIds())) {
                changed = true;
            }
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
        return changed;
    }

    @Nullable
    private SearchToolbarFilterGroup findGroup(@NonNull String groupId) {
        for (SearchToolbarFilterGroup group : filterGroups) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    private void notifyFiltersChanged() {
        if (listener == null) {
            return;
        }
        Map<String, Set<String>> snapshot = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : selectedFilters.entrySet()) {
            snapshot.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<>(entry.getValue())));
        }
        listener.onFiltersChanged(Collections.unmodifiableMap(snapshot));
    }

    private void dismissFilterPopup() {
        if (filterPopup != null) {
            filterPopup.dismiss();
            filterPopup = null;
        }
    }

    @NonNull
    private MaterialShapeDrawable createPopupBackground() {
        MaterialShapeDrawable background = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder().setAllCornerSizes(dp(12)).build());
        background.setFillColor(ColorStateList.valueOf(
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface)));
        return background;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDetachedFromWindow() {
        searchHandler.removeCallbacks(searchRunnable);
        dismissFilterPopup();
        super.onDetachedFromWindow();
    }
}