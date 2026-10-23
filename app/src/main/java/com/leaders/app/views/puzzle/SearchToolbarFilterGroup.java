package com.leaders.app.views.puzzle;

import androidx.annotation.NonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Generic description of a filter group that can be displayed by SearchToolbarView.
 */
public final class SearchToolbarFilterGroup {
    public static final class Option {
        @NonNull
        private final String id;
        @NonNull
        private final String label;

        public Option(@NonNull String id, @NonNull String label) {
            this.id = id;
            this.label = label;
        }

        @NonNull
        public String getId() {
            return id;
        }

        @NonNull
        public String getLabel() {
            return label;
        }
    }

    @NonNull
    private final String id;
    @NonNull
    private final String title;
    private final boolean multipleChoice;
    @NonNull
    private final List<Option> options;

    public SearchToolbarFilterGroup(@NonNull String id,
                                    @NonNull String title,
                                    boolean multipleChoice,
                                    @NonNull List<Option> options) {
        this.id = id;
        this.title = title;
        this.multipleChoice = multipleChoice;
        this.options = List.copyOf(options);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    @NonNull
    public List<Option> getOptions() {
        return options;
    }

    @NonNull
    public Set<String> getOptionIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (Option option : options) {
            ids.add(option.getId());
        }
        return ids;
    }
}