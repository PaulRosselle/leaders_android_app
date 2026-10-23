package com.leaders.puzzlelogic.search;

import androidx.annotation.NonNull;

import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PuzzleSaveSearchEngine {
    // Identifies diacritical marks (accents, diaereses, etc.) for removal
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private PuzzleSaveSearchEngine() {
    }

    // Filter then sort, doesn't modify the source list
    @NonNull
    public static List<PuzzleSave> apply(@NonNull Collection<PuzzleSave> source,
                                         @NonNull PuzzleSaveCriteria criteria) {
        String key = normalize(criteria.getSearchKey());
        List<PuzzleSave> result = new ArrayList<>();
        for (PuzzleSave save : source) {
            if (criteria.getFilters().matches(save) && matchesSearchKey(save, key)) {
                result.add(save);
            }
        }
        result.sort(criteria.getSortMode().getComparator());
        return result;
    }

    // Search by name and author.
    private static boolean matchesSearchKey(@NonNull PuzzleSave save, @NonNull String normalizedKey) {
        if (normalizedKey.isEmpty()) {
            return true;
        }
        return normalize(save.getName()).contains(normalizedKey) ||
                normalize(save.getAuthor()).contains(normalizedKey);
    }

    @NonNull
    private static String normalize(@NonNull String value) {
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }
}