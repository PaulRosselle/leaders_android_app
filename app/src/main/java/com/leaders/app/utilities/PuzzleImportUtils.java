package com.leaders.app.utilities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.RecruitmentActionMotion;
import com.leaders.gamelogic.entities.Cell;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.RecruitmentMotionType;
import com.leaders.gamelogic.enums.TeamColor;
import com.leaders.puzzlelogic.utilities.PuzzleEditionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PuzzleImportUtils {
    public static final class ImportedPuzzle {
        @NonNull public final String name;
        @NonNull public final String author;
        @NonNull public final GameHistory history;

        ImportedPuzzle(@NonNull String name, @NonNull String author, @NonNull GameHistory history) {
            this.name = name;
            this.author = author;
            this.history = history;
        }
    }

    public static final class ImportResult {
        @NonNull public final List<ImportedPuzzle> puzzles = new ArrayList<>();
        @NonNull public final List<String> errors = new ArrayList<>();
    }

    private static final class LbeEntry {
        final String title;
        final String url;

        LbeEntry(String title, String url) {
            this.title = title;
            this.url = url;
        }
    }

    /** Inverse of R.string.puzzle_export_identity: the regex plus the group index of each argument. */
    private static final class TitlePattern {
        final Pattern pattern;
        final int nameGroup;
        final int authorGroup;

        TitlePattern(Pattern pattern, int nameGroup, int authorGroup) {
            this.pattern = pattern;
            this.nameGroup = nameGroup;
            this.authorGroup = authorGroup;
        }
    }

    private PuzzleImportUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Imports puzzles from LBE URLs found in the clipboard.
     *
     * @param context the context used to access the clipboard and display messages
     * @return the result containing the successfully imported puzzles and any errors
     */
    @NonNull
    public static ImportResult importPuzzlesFromClipboard(@NonNull Context context) {
        ImportResult result = new ImportResult();

        String text = readClipboardText(context);
        List<LbeEntry> entries = text == null ? Collections.emptyList() : parseEntries(text);
        if (entries.isEmpty()) {
            Toast.makeText(context, R.string.invalid_lbe_url_not_found, Toast.LENGTH_LONG).show();
            return result;
        }

        TitlePattern titlePattern = buildTitlePattern(context);

        for (int i = 0; i < entries.size(); i++) {
            LbeEntry entry = entries.get(i);
            String label = entry.title.isEmpty() ? "#" + (i + 1) : entry.title;

            try {
                GameHistory history = getFromLbeUrl(context, entry.url);

                String validityErrors = PuzzleEditionUtils.getPuzzleValidityErrors(context, history);
                if (!validityErrors.isEmpty()) {
                    result.errors.add(label + "\n" + validityErrors);
                    continue;
                }

                String[] nameAndAuthor = splitTitle(titlePattern, entry.title);
                result.puzzles.add(new ImportedPuzzle(nameAndAuthor[0], nameAndAuthor[1], history));
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                // Clipboard content is arbitrary input: malformed data must not crash the import
                result.errors.add(label + "\n" + e.getMessage());
            }
        }

        if (!result.errors.isEmpty()) {
            new AlertDialog.Builder(context, R.style.alert_dialog_theme)
                    .setTitle(R.string.invalid_puzzle)
                    .setMessage(TextUtils.join("\n\n", result.errors))
                    .show();
        }
        return result;
    }

    /**
     * Creates a game history from an LBE URL.
     *
     * @param context the context used to access resources, or {@code null}
     * @param lbeUrl the LBE URL to import
     * @return the game history initialized with the characters from the URL
     */
    public static GameHistory getFromLbeUrl(@Nullable Context context, @NonNull String lbeUrl) {
        List<IGameAction> initialActions = new ArrayList<>();
        for (TeamColor teamColor : TeamColor.values()) {
            for (Cell characterCell : getTeamCharacterCells(context, lbeUrl, teamColor)) {
                initialActions.add(new RecruitmentAction(List.of(
                        new RecruitmentActionMotion(RecruitmentMotionType.Add,
                                characterCell.getCharacter(),
                                characterCell.getPosition())
                        ))
                );
            }
        }

        return PuzzleEditionUtils.getDefaultHistory(initialActions);
    }

    /**
     * Extracts the character cells for the given team from an LBE URL.
     *
     * @param context the context used to access resources, or {@code null}
     * @param lbeUrl the LBE URL containing the character data
     * @param teamColor the team color to extract
     * @return the list of character cells for the team, or an empty list if its data is absent
     */
    private static List<Cell> getTeamCharacterCells(@Nullable Context context,
                                                    @NonNull String lbeUrl,
                                                    @NonNull TeamColor teamColor) {
        // First we get the header separator associated with the player color
        String colorHeader = teamColor == TeamColor.Black ? LbeUtils.HEADER_BLACK : LbeUtils.HEADER_WHITE;

        // If the header cannot be found within the URL, we have no data to extract
        int headerIdx = lbeUrl.indexOf(colorHeader);
        if (headerIdx == -1) {
            return Collections.emptyList();
        }

        // The end of the data section can either be marked with
        // the color separator or with the end of the url
        int cellDatasStartIdx = headerIdx + colorHeader.length();
        int colorSeparatorIdx = lbeUrl.indexOf(LbeUtils.COLOR_DATA_SEPARATOR, cellDatasStartIdx);
        String cellDatas;
        if (colorSeparatorIdx != -1) {
            cellDatas = lbeUrl.substring(cellDatasStartIdx, colorSeparatorIdx);
        } else {
            cellDatas = lbeUrl.substring(cellDatasStartIdx);
        }

        List<Cell> characterCells = new ArrayList<>();

        // Once we have isolated the tiles datas we can extract each token and add them immediately to the board
        for (String cellData : cellDatas.split(LbeUtils.CELL_DATA_SEPARATOR)) {
            if (cellData.isEmpty()) {
                continue;
            }
            String[] characterDatas = cellData.split(LbeUtils.CHARACTER_DATA_SEPARATOR);
            Position position = LbeUtils.getPositionFromExportStr(context, characterDatas[0].toUpperCase());
            CharacterType characterType = LbeUtils.getCharacterTypeFromExportStr(context, characterDatas[1].toUpperCase());

            Cell characterCell = new Cell(position);
            characterCell.setCharacter(Character.create(characterType, teamColor));
            characterCells.add(characterCell);
        }

        return characterCells;
    }

    /**
     * Reads the text content of the clipboard.
     *
     * @param context the context used to access the clipboard
     * @return the clipboard text, or {@code null} if the clipboard is empty
     */
    @Nullable
    private static String readClipboardText(@NonNull Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        // "coerceToText" guarantees the item can be manipulated as text
        return clipData.getItemAt(0).coerceToText(context).toString();
    }

    /**
     * Parses text into LBE entries containing titles and URLs.
     *
     * @param text the text to parse
     * @return the list of parsed entries
     */
    @NonNull
    private static List<LbeEntry> parseEntries(@NonNull String text) {
        List<LbeEntry> entries = new ArrayList<>();
        String pendingTitle = "";

        for (String rawLine : text.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.contains(LbeUtils.DEFAULT_URL)) {
                entries.add(new LbeEntry(pendingTitle, line));
                pendingTitle = "";
            } else {
                pendingTitle = line;
            }
        }
        return entries;
    }

    /**
     * Builds a pattern to extract a puzzle name and author from a formatted title.
     *
     * @param context the context used to retrieve the title format
     * @return the title pattern, or {@code null} if the format cannot be reliably parsed
     */
    @Nullable
    private static TitlePattern buildTitlePattern(@NonNull Context context) {
        // Titles are trimmed when parsed, so the format must be too
        String format = context.getString(R.string.puzzle_export_identity).trim();
        Matcher placeholder = Pattern.compile("%%|%(\\d+)\\$s|%s").matcher(format);

        StringBuilder regex = new StringBuilder("^");
        int last = 0;
        int sequentialIndex = 0;
        int groupCount = 0;
        int nameGroup = -1;
        int authorGroup = -1;

        while (placeholder.find()) {
            regex.append(Pattern.quote(format.substring(last, placeholder.start())));
            last = placeholder.end();

            if (placeholder.group().equals("%%")) {
                regex.append(Pattern.quote("%"));
                continue;
            }

            // Same numbering rule as String.format: explicit index, or next ordinary index
            String placeHolderGroup = placeholder.group(1);
            int argIndex = placeHolderGroup != null
                    ? Integer.parseInt(placeHolderGroup) : ++sequentialIndex;

            groupCount++;
            if (argIndex == 1 && nameGroup == -1) {
                nameGroup = groupCount;
            } else if (argIndex == 2 && authorGroup == -1) {
                authorGroup = groupCount;
            } else {
                return null; // Unknown or repeated argument: cannot be inverted reliably
            }

            // First group greedy, the next ones lazy: with "A by B by C", the split happens
            // on the last separator
            regex.append(groupCount == 1 ? "(.+)" : "(.+?)");
        }
        regex.append(Pattern.quote(format.substring(last))).append("$");

        if (groupCount != 2) {
            return null;
        }
        return new TitlePattern(Pattern.compile(regex.toString()), nameGroup, authorGroup);
    }

    /**
     * Splits a title into a puzzle name and an author.
     *
     * @param titlePattern the pattern used to parse the title, or {@code null}
     * @param title the title to split
     * @return an array containing the puzzle name and author, in that order; the author is empty
     *         if the title cannot be split
     */
    @NonNull
    private static String[] splitTitle(@Nullable TitlePattern titlePattern, @NonNull String title) {
        if (titlePattern != null) {
            Matcher matcher = titlePattern.pattern.matcher(title);
            if (matcher.matches()) {
                return new String[]{
                        Objects.requireNonNull(
                                matcher.group(titlePattern.nameGroup),
                                "Matcher name group cannot be null"
                        ).trim(),
                        Objects.requireNonNull(
                                matcher.group(titlePattern.authorGroup),
                                "Matcher name group cannot be null"
                        ).trim()
                };
            }
        }
        return new String[]{title, ""};
    }
}
