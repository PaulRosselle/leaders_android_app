package com.leaders.puzzlelogic.utilities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;

import java.util.ArrayList;
import java.util.List;

public final class CharactersPermutationUtils {
    private CharactersPermutationUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Returns the total number of permutations of {@code characters}.
     *
     * @param characters the elements to permute
     * @return the number of permutations, {@code characters.size()!}
     * @throws IllegalArgumentException if the number of permutations exceeds
     *                                  {@link Long#MAX_VALUE}
     */
    public static long getPermutationCount(@NonNull List<Character> characters) {
        return factorial(characters.size());
    }

    /**
     * Returns the permutation of {@code characters} identified by {@code index}
     * in lexicographic order.
     *
     * @param characters the elements to permute
     * @param index the zero-based permutation index, in {@code [0, n!)}
     * @return the permutation corresponding to {@code index}
     * @throws IllegalArgumentException if {@code index} is outside the valid range
     */
    @NonNull
    public static List<Character> getPermutation(@NonNull List<Character> characters, long index) {
        int size = characters.size();

        if (index < 0 || index >= factorial(size)) {
            throw new IllegalArgumentException("Invalid permutation index: " + index);
        }

        List<Character> remaining = new ArrayList<>(characters);
        List<Character> permutation = new ArrayList<>(size);

        for (int position = size - 1; position >= 0; position--) {
            long blockSize = factorial(position);

            int selectedIndex = (int) (index / blockSize);
            index %= blockSize;

            permutation.add(remaining.remove(selectedIndex));
        }

        return permutation;
    }

    /**
     * Returns the factorial of {@code value}.
     *
     * @param value a non-negative integer
     * @return {@code value!}
     * @throws IllegalArgumentException if the result exceeds {@link Long#MAX_VALUE}
     */
    private static long factorial(int value) {
        long result = 1;

        for (int i = 2; i <= value; i++) {
            if (Long.MAX_VALUE / i < result) {
                throw new IllegalArgumentException("Factorial exceeds long range: " + value);
            }

            result *= i;
        }

        return result;
    }
}
