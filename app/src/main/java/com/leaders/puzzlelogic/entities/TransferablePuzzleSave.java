package com.leaders.puzzlelogic.entities;

import androidx.annotation.NonNull;

import com.leaders.puzzlelogic.enums.PuzzleLifetime;

import org.json.JSONException;
import org.json.JSONObject;

/**

 * Represents the transferable version of a puzzle used for import and export.
 *
 * <p>This class intentionally does not extend {@link PuzzleSave}, because a transferable
 * puzzle represents the puzzle data that can be shared between applications or users,
 * whereas {@code PuzzleSave} represents a puzzle's local state within the application.
 * In particular, the {@code solved} state is local to the application and must not be
 * part of the transferable representation.</p>
 *
 * <p>Conversion between the two representations is handled by this class through its
 * factory and conversion methods, keeping import/export logic separate from the
 * application-specific puzzle state.</p>
 */
public final class TransferablePuzzleSave {
    @NonNull
    private final String name;
    @NonNull
    private final String author;
    @NonNull
    private final PuzzleLifetime lifetime;
    @NonNull
    private final JSONObject datas;

    private TransferablePuzzleSave(@NonNull String name,
                                   @NonNull String author,
                                   @NonNull PuzzleLifetime lifetime,
                                   @NonNull JSONObject datas) {
        this.name = name;
        this.author = author;
        this.lifetime = lifetime;
        try {
            this.datas = new JSONObject(datas.toString());
        } catch (JSONException e) {
            throw new IllegalStateException("Unable to copy puzzle datas", e);
        }
    }

    @NonNull
    public static TransferablePuzzleSave fromSave(@NonNull PuzzleSave puzzleSave) {
        if (puzzleSave instanceof OfficialPuzzleSave) {
            return fromOfficialSave((OfficialPuzzleSave) puzzleSave);
        }

        if (puzzleSave instanceof CustomPuzzleSave) {
            return fromCustomSave((CustomPuzzleSave) puzzleSave);
        }

        throw new IllegalArgumentException("Unsupported PuzzleSave type: " + puzzleSave.getClass());
    }

    @NonNull
    public static TransferablePuzzleSave fromOfficialSave(@NonNull OfficialPuzzleSave officialPuzzleSave) {
        return new TransferablePuzzleSave(
                officialPuzzleSave.getName(),
                "",
                officialPuzzleSave.getLifetime(),
                officialPuzzleSave.getDatas()
        );
    }

    @NonNull
    public static TransferablePuzzleSave fromCustomSave(@NonNull CustomPuzzleSave customPuzzleSave) {
        return new TransferablePuzzleSave(
                customPuzzleSave.getName(),
                customPuzzleSave.getAuthor(),
                customPuzzleSave.getLifetime(),
                customPuzzleSave.getDatas()
        );
    }

    @NonNull
    public CustomPuzzleSave toCustomSave() {
        return new CustomPuzzleSave(name, author, lifetime, datas, false);
    }

    @NonNull
    public static TransferablePuzzleSave fromJson(@NonNull JSONObject joPuzzleSave) throws JSONException {
        return new TransferablePuzzleSave(
                joPuzzleSave.getString("name"),
                joPuzzleSave.optString("author", ""),
                PuzzleLifetime.valueOf(joPuzzleSave.getString("lifetime")),
                joPuzzleSave.getJSONObject("datas")
        );
    }

    @NonNull
    public JSONObject getAsJson() throws JSONException {
        JSONObject joPuzzleSave = new JSONObject();

        joPuzzleSave.put("name", name);
        joPuzzleSave.put("author", author);
        joPuzzleSave.put("lifetime", lifetime.name());
        joPuzzleSave.put("datas", new JSONObject(datas.toString()));

        return joPuzzleSave;
    }
}
