package com.leaders.app.views.puzzle;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;

public class SearchToolbarView extends LinearLayout {
    public interface SearchToolbarListener {
        void onStartSearch(@NonNull String searchKey);

        void onFilter(); // TODO - handle filters

        void onSort(); // TODO - handle sorting
    }

    private static final long SEARCH_DELAY_MS = 300L;

    private final Handler searchHandler;

    private final EditText edtSearch;
    private final MaterialButton btnFilter;
    private final MaterialButton btnSort;

    private final Runnable searchRunnable;

    private SearchToolbarListener listener;

    public SearchToolbarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_search_toolbar, this);

        edtSearch = findViewById(R.id.edtSearch_vwReplaySave);
        btnFilter = findViewById(R.id.btnFilter_vwSearchToolbar);
        btnSort = findViewById(R.id.btnSort_vwSearchToolbar);

        searchHandler = new Handler(Looper.getMainLooper());
        searchRunnable = this::startSearch;

        initListeners();
    }

    @NonNull
    private String getSearchKey() {
        return edtSearch.getText().toString().trim();
    }

    private void startSearch() {
        if (listener != null) {
            listener.onStartSearch(getSearchKey());
        }
    }

    private void initListeners() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        searchHandler.removeCallbacks(searchRunnable);
        super.onDetachedFromWindow();
    }
}