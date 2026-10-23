package com.leaders.puzzlelogic.search;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.leaders.R;
import com.leaders.app.views.puzzle.SearchToolbarFilterGroup;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.enums.PuzzleDifficulty;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates between the generic vocabulary of SearchToolbarView (menu IDs,
 * string IDs) and the business vocabulary of PuzzleSave.
 */
public final class PuzzleSaveToolbarMapper {
    private static final String GROUP_DIFFICULTY = "difficulty";
    private static final String GROUP_AUTHOR = "author";
    private static final String GROUP_SOLVED = "solved";

    private static final String OPTION_SOLVED = "solved";
    private static final String OPTION_UNSOLVED = "unsolved";

    private PuzzleSaveToolbarMapper() {
    }

    @NonNull
    public static PuzzleSortMode toSortMode(@IdRes int menuItemId) {
        if (menuItemId == R.id.sort_name_desc) {
            return PuzzleSortMode.NAME_DESC;
        }
        if (menuItemId == R.id.sort_author_name) {
            return PuzzleSortMode.AUTHOR_ASC;
        }
        if (menuItemId == R.id.sort_author_desc) {
            return PuzzleSortMode.AUTHOR_DESC;
        }
        if (menuItemId == R.id.sort_difficulty) {
            return PuzzleSortMode.DIFFICULTY_ASC;
        }
        if (menuItemId == R.id.sort_difficulty_desc) {
            return PuzzleSortMode.DIFFICULTY_DESC;
        }

        return PuzzleSortMode.NAME_ASC;
    }

    @IdRes
    public static int toMenuItemId(@NonNull PuzzleSortMode sortMode) {
        switch (sortMode) {
            case NAME_DESC: return R.id.sort_name_desc;
            case AUTHOR_ASC: return R.id.sort_author_name;
            case AUTHOR_DESC: return R.id.sort_author_desc;
            case DIFFICULTY_ASC: return R.id.sort_difficulty;
            case DIFFICULTY_DESC: return R.id.sort_difficulty_desc;
            case NAME_ASC: return R.id.sort_name;
            default: throw new IllegalArgumentException("Sort mode not handled: " + sortMode);
        }
    }

    @NonNull
    public static List<SearchToolbarFilterGroup> buildFilterGroups(@NonNull Context context,
                                                                   @NonNull List<PuzzleSave> saves) {
        List<SearchToolbarFilterGroup> groups = new ArrayList<>();

        // Difficulty (multiple choice allowed)
        List<SearchToolbarFilterGroup.Option> difficultyOptions = new ArrayList<>();
        for (PuzzleDifficulty difficulty : PuzzleDifficulty.values()) {
            difficultyOptions.add(new SearchToolbarFilterGroup.Option(
                    difficulty.name(),
                    context.getString(getDifficultyLabelRes(difficulty))
            ));
        }
        groups.add(new SearchToolbarFilterGroup(
                GROUP_DIFFICULTY,
                context.getString(R.string.filter_group_difficulty),
                true, difficultyOptions
        ));

        // Author (multiple choice, options populated from the data)
        Set<String> authorSet = new HashSet<>();
        for (PuzzleSave save : saves) {
            if (!save.getAuthor().trim().isEmpty()) {
                authorSet.add(save.getAuthor());
            }
        }
        if (!authorSet.isEmpty()) {
            List<String> authors = new ArrayList<>(authorSet);
            Collator collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
            authors.sort(collator);

            List<SearchToolbarFilterGroup.Option> authorOptions = new ArrayList<>();
            for (String author : authors) {
                authorOptions.add(new SearchToolbarFilterGroup.Option(author, author));
            }
            groups.add(new SearchToolbarFilterGroup(
                    GROUP_AUTHOR,
                    context.getString(R.string.filter_group_author),
                    true, authorOptions
            ));
        }

        // Completion status (single choice, no selection = all)
        List<SearchToolbarFilterGroup.Option> solvedOptions = new ArrayList<>();
        solvedOptions.add(new SearchToolbarFilterGroup.Option(
                OPTION_SOLVED, context.getString(R.string.filter_solved)));
        solvedOptions.add(new SearchToolbarFilterGroup.Option(
                OPTION_UNSOLVED, context.getString(R.string.filter_unsolved)));
        groups.add(new SearchToolbarFilterGroup(
                GROUP_SOLVED, context.getString(R.string.filter_group_solved),
                false, solvedOptions
        ));

        return groups;
    }

    @NonNull
    public static PuzzleSaveFilters toFilters(@NonNull Map<String, Set<String>> selection) {
        Set<PuzzleDifficulty> difficulties = EnumSet.noneOf(PuzzleDifficulty.class);
        for (String id : getOrEmpty(selection, GROUP_DIFFICULTY)) {
            difficulties.add(PuzzleDifficulty.valueOf(id));
        }

        Set<String> authors = new HashSet<>(getOrEmpty(selection, GROUP_AUTHOR));

        Set<String> solvedIds = getOrEmpty(selection, GROUP_SOLVED);
        Boolean solved = null;
        if (solvedIds.contains(OPTION_SOLVED)) {
            solved = Boolean.TRUE;
        } else if (solvedIds.contains(OPTION_UNSOLVED)) {
            solved = Boolean.FALSE;
        }

        return new PuzzleSaveFilters(difficulties, authors, solved);
    }

    @NonNull
    public static Map<String, Set<String>> toSelection(@NonNull PuzzleSaveFilters filters) {
        Map<String, Set<String>> selection = new HashMap<>();

        if (!filters.getDifficulties().isEmpty()) {
            Set<String> ids = new LinkedHashSet<>();
            for (PuzzleDifficulty difficulty : filters.getDifficulties()) {
                ids.add(difficulty.name());
            }
            selection.put(GROUP_DIFFICULTY, ids);
        }
        if (!filters.getAuthors().isEmpty()) {
            selection.put(GROUP_AUTHOR, new LinkedHashSet<>(filters.getAuthors()));
        }
        if (filters.getSolved() != null) {
            selection.put(GROUP_SOLVED, Collections.singleton(
                    filters.getSolved() ? OPTION_SOLVED : OPTION_UNSOLVED
            ));
        }
        return selection;
    }

    private static int getDifficultyLabelRes(@NonNull PuzzleDifficulty difficulty) {
        switch (difficulty) {
            case Easy: return R.string.puzzle_difficulty_easy;
            case Medium: return R.string.puzzle_difficulty_medium;
            case Hard: return R.string.puzzle_difficulty_hard;
            case Extreme: return R.string.puzzle_difficulty_extreme;
            default: throw new IllegalArgumentException("Puzzle difficulty not handled: " + difficulty);
        }
    }

    @NonNull
    private static Set<String> getOrEmpty(@NonNull Map<String, Set<String>> selection, @NonNull String key) {
        Set<String> value = selection.get(key);
        return value != null ? value : Collections.emptySet();
    }
}