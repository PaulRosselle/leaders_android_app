package com.leaders.puzzlelogic.search;

import androidx.annotation.NonNull;

import com.leaders.puzzlelogic.entities.PuzzleSave;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;

public enum PuzzleSortMode {
    NAME_ASC,
    NAME_DESC,
    AUTHOR_ASC,
    AUTHOR_DESC,
    DIFFICULTY_ASC,
    DIFFICULTY_DESC;

    @NonNull
    public Comparator<PuzzleSave> getComparator() {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY); // ignore case and accents

        Comparator<PuzzleSave> byName = (a, b) -> compareNatural(collator, a.getName(), b.getName());
        Comparator<PuzzleSave> byAuthor = (a, b) -> compareNatural(collator, a.getAuthor(), b.getAuthor());
        Comparator<PuzzleSave> byDifficulty = Comparator.comparing(PuzzleSave::getDifficulty);

        // Les critères secondaires restent toujours en ordre croissant.
        switch (this) {
            case NAME_DESC:
                return then(Collections.reverseOrder(byName), byAuthor);
            case AUTHOR_ASC:
                return then(byAuthor, byName);
            case AUTHOR_DESC:
                return then(Collections.reverseOrder(byAuthor), byName);
            case DIFFICULTY_ASC:
                return then(then(byDifficulty, byName), byAuthor);
            case DIFFICULTY_DESC:
                return then(then(Collections.reverseOrder(byDifficulty), byName), byAuthor);
            case NAME_ASC:
            default:
                return then(byName, byAuthor);
        }
    }

    @NonNull
    private static Comparator<PuzzleSave> then(@NonNull Comparator<PuzzleSave> first,
                                               @NonNull Comparator<PuzzleSave> second) {
        return (a, b) -> {
            int result = first.compare(a, b);
            return result != 0 ? result : second.compare(a, b);
        };
    }

    /**
     * Natural sorting: "#2" < "#11", "Puzzle #9" < "Puzzle #10".
     * Digit sequences are compared numerically, while other characters are compared using the Collator.
     */
    private static int compareNatural(@NonNull Collator collator, @NonNull String a, @NonNull String b) {
        int i = 0;
        int j = 0;

        while (i < a.length() && j < b.length()) {
            boolean digitsA = isAsciiDigit(a.charAt(i));
            boolean digitsB = isAsciiDigit(b.charAt(j));

            int endA = findChunkEnd(a, i, digitsA);
            int endB = findChunkEnd(b, j, digitsB);

            String chunkA = a.substring(i, endA);
            String chunkB = b.substring(j, endB);

            int result = (digitsA && digitsB)
                    ? compareDigitChunks(chunkA, chunkB)
                    : collator.compare(chunkA, chunkB);
            if (result != 0) {
                return result;
            }

            i = endA;
            j = endB;
        }

        // Common prefix: the shorter string comes first.
        int remainingA = a.length() - i;
        int remainingB = b.length() - j;
        if (remainingA != remainingB) {
            return Integer.compare(remainingA, remainingB);
        }

        // "Numeric" equality (e.g. "01" and "1"): break ties deterministically.
        return collator.compare(a, b);
    }

    private static int findChunkEnd(@NonNull String s, int start, boolean digits) {
        int end = start;
        while (end < s.length() && isAsciiDigit(s.charAt(end)) == digits) {
            end++;
        }
        return end;
    }

    // Numeric comparison without parsing: no overflow is possible, even with very long sequences.
    private static int compareDigitChunks(@NonNull String a, @NonNull String b) {
        String trimmedA = stripLeadingZeros(a);
        String trimmedB = stripLeadingZeros(b);
        if (trimmedA.length() != trimmedB.length()) {
            return Integer.compare(trimmedA.length(), trimmedB.length());
        }
        return trimmedA.compareTo(trimmedB);
    }

    @NonNull
    private static String stripLeadingZeros(@NonNull String digits) {
        int start = 0;
        while (start < digits.length() - 1 && digits.charAt(start) == '0') {
            start++;
        }
        return digits.substring(start);
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}