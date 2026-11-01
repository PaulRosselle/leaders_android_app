package com.leaders.app.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.AtomicFile;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.Contributor;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.entities.Settings;
import com.leaders.app.entities.crash.CrashLog;
import com.leaders.app.enums.TutorialChapter;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;
import com.leaders.puzzlelogic.entities.TransferablePuzzleSave;
import com.leaders.puzzlelogic.search.PuzzleSortMode;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.entities.GameHistorySerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JsonUtils {

    private static final String CRASH_LOG_FILENAME = "crash_log.json";
    private static final String CUSTOM_PUZZLES_FILENAME = "custom_puzzles.json";
    private static final String SOLVED_OFFICIAL_PUZZLES_FILENAME = "solved_official_puzzles.json";
    private static final String REPLAYS_FILENAME = "replays.json";
    private static final String SETTINGS_FILENAME = "settings.json";
    private static final String LOG_TAG = "JsonUtils";
    private static final String ATOMIC_FILE_BACKUP_SUFFIX = ".bak";
    private static final String APP_RECOVER_SUFFIX = ".recover";
    private static final String GAME_SNAPSHOT_FILENAME_PREFIX = "game_snapshot_";

    private interface JsonParser<T> {
        T parse(@NonNull JSONObject json) throws JSONException;
    }

    /** Save file unreadable, even after trying the app recover copy. The file is left untouched. */
    public static final class CorruptedFileException extends Exception {

        private final String fileName;

        public CorruptedFileException(@NonNull String fileName, @NonNull Throwable cause) {
            super(String.format("File \"%s\" is corrupted and could not be recovered", fileName), cause);
            this.fileName = fileName;
        }

        @NonNull
        public String getFileName() {
            return fileName;
        }
    }

    private JsonUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    //region GENERIC FILE METHODS

    @NonNull
    private static File getFile(@NonNull Context context, @NonNull String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean fileExists(@NonNull Context context, @NonNull String fileName) {
        // After a crash we can have a backup file instead of "filename"
        return getFile(context, fileName).exists() ||
                getFile(context, fileName + ATOMIC_FILE_BACKUP_SUFFIX).exists() ||
                getFile(context, fileName + APP_RECOVER_SUFFIX).exists();
    }

    private static void deleteFile(@NonNull Context context, @NonNull String fileName) {
        // The backups must be deleted too, otherwise deleted data would be restored
        for (String name : new String[] {
                fileName,
                fileName + ATOMIC_FILE_BACKUP_SUFFIX,
                fileName + APP_RECOVER_SUFFIX}) {
            File file = getFile(context, name);
            if (!file.delete() && file.exists()) {
                throw new IllegalStateException(String.format("Unable to delete file \"%s\"", name));
            }
        }
    }

    //endregion

    //region JSON FILE METHODS

    @NonNull
    private static JSONObject openJsonFile(@NonNull InputStream inputStream) throws IOException, JSONException {
        StringBuilder content = new StringBuilder();

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
        }

        return new JSONObject(content.toString());
    }

    @NonNull
    private static JSONObject openJsonFile(@NonNull Context context,
                                           @NonNull String fileName) throws IllegalArgumentException {
        AtomicFile atomicFile = new AtomicFile(getFile(context, fileName));

        // "openRead" restores the backup if necessary
        try (InputStream inputStream = atomicFile.openRead()) {
            return openJsonFile(inputStream);

        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("File \"%s\" cannot be opened", fileName), e);

        } catch (JSONException e) {
            throw new IllegalArgumentException(String.format("File \"%s\" content is not valid JSON", fileName), e);
        }
    }

    private static void updateJsonFile(@NonNull JSONObject jsonObject,
                                       @NonNull OutputStream outputStream) throws IOException {
        byte[] content = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        outputStream.write(content);
        outputStream.flush();
    }

    private static void writeAtomically(@NonNull File file, @NonNull byte[] content) throws IOException {
        AtomicFile atomicFile = new AtomicFile(file);
        FileOutputStream outputStream = atomicFile.startWrite();
        try {
            outputStream.write(content);
            // File sync + closing + backup removal
            atomicFile.finishWrite(outputStream);
        } catch (IOException e) {
            // Restores the original file
            atomicFile.failWrite(outputStream);
            throw e;
        }
    }

    private static void saveJsonFile(@NonNull Context context,
                                     @NonNull String fileName,
                                     @NonNull JSONObject jsonObject) throws IOException {
        byte[] content = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        // First we validate the json object in memory before updating the file
        try {
            new JSONObject(new String(content, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            throw new IOException(String.format("Generated JSON for file \"%s\" is invalid", fileName), e);
        }

        writeAtomically(getFile(context, fileName), content);

        // Best-effort copy of the app recover state. Its failure must not fail the save.
        try {
            writeAtomically(getFile(context, fileName + APP_RECOVER_SUFFIX), content);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Unable to update app recover copy of " + fileName, e);
        }
    }

    @NonNull
    private static <T> T loadWithFallback(@NonNull Context context,
                                          @NonNull String fileName,
                                          @NonNull JsonParser<T> parser) throws CorruptedFileException {
        try {
            return parser.parse(openJsonFile(context, fileName));
        } catch (JSONException | RuntimeException primaryError) {
            // Covers unreadable file, invalid JSON and invalid structure/content
            Log.w(LOG_TAG, "Unable to load " + fileName + ", trying app recover copy", primaryError);

            String lastGoodName = fileName + APP_RECOVER_SUFFIX;
            if (getFile(context, lastGoodName).exists()) {
                try {
                    T result = parser.parse(openJsonFile(context, lastGoodName));
                    Log.w(LOG_TAG, fileName + " restored from app recover copy");
                    return result;
                } catch (JSONException | RuntimeException fallbackError) {
                    primaryError.addSuppressed(fallbackError);
                }
            }
            // Nothing is deleted: the user decides what to do
            throw new CorruptedFileException(fileName, primaryError);
        }
    }

    /**
     * Deletes the corrupted file (and its backups) so the app can start from a clean state.
     * To be called only after an explicit user confirmation.
     */
    public static void discardCorruptedFile(@NonNull Context context,
                                            @NonNull CorruptedFileException exception) {
        deleteFile(context, exception.getFileName());
    }

    /**
     * Sets aside (renames, never deletes) the corrupted files so the app can start from a clean state.
     * To be called only after an explicit user choice.
     */
    public static boolean quarantineCorruptedFile(@NonNull Context context,
                                                  @NonNull CorruptedFileException exception) {
        String suffix = ".corrupt-" + System.currentTimeMillis();
        boolean success = true;
        for (String name : new String[]{exception.getFileName(),
                exception.getFileName() + APP_RECOVER_SUFFIX}) {
            File file = getFile(context, name);
            if (file.exists() && !file.renameTo(getFile(context, name + suffix))) {
                success = false;
            }
        }
        return success;
    }

    //endregion

    //region CRASH LOGGING METHODS

    @Nullable
    public static CrashLog loadCrashLog(@NonNull Context context) {
        if (!fileExists(context, CRASH_LOG_FILENAME)) {
            return null;
        }

        try {
            return new CrashLog(openJsonFile(context, CRASH_LOG_FILENAME));
        } catch (IllegalArgumentException | JSONException e) {
            // Crash logging must never affect application execution.
            return null;
        }
    }

    public static void saveCrashLog(@NonNull Context context, @NonNull CrashLog crashLog) {
        try (OutputStream outputStream = context.openFileOutput(CRASH_LOG_FILENAME, Context.MODE_PRIVATE)) {
            updateJsonFile(crashLog.getAsJsonObj(), outputStream);
        } catch (IOException | JSONException e) {
            // Crash logging must never affect application execution.
        }
    }

    public static void deleteCrashLog(@NonNull Context context) {
        deleteFile(context, CRASH_LOG_FILENAME);
    }

    //endregion

    //region PUZZLE SAVE METHODS

    public static List<OfficialPuzzleSave> loadOfficialPuzzles(@NonNull Context context) throws CorruptedFileException {
        // Official puzzles are stored within a raw resource file
        try (InputStream fisPuzzles = context.getResources().openRawResource(R.raw.official_puzzles)) {
            JSONArray jaPuzzles = openJsonFile(fisPuzzles).getJSONArray("puzzles");
            List<OfficialPuzzleSave> officialPuzzles = new ArrayList<>();

            // Official puzzle solved state is saved in a different
            Set<Integer> solvedPuzzles = loadSolvedOfficialPuzzles(context);

            for (int i = 0; i < jaPuzzles.length(); i++) {
                OfficialPuzzleSave officialPuzzle = new OfficialPuzzleSave(context, jaPuzzles.getJSONObject(i));
                officialPuzzle.setSolved(solvedPuzzles.contains(officialPuzzle.getId()));
                officialPuzzles.add(officialPuzzle);
            }

            return officialPuzzles;

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Integer> loadSolvedOfficialPuzzles(@NonNull Context context) throws CorruptedFileException {
        if (!fileExists(context, SOLVED_OFFICIAL_PUZZLES_FILENAME)) {
            return Collections.emptySet();
        }

        return loadWithFallback(context, SOLVED_OFFICIAL_PUZZLES_FILENAME, json -> {
            JSONArray jaSolvedPuzzles = json.getJSONArray("solved_puzzles");
            Set<Integer> solvedPuzzles = new HashSet<>();
            for (int i = 0; i < jaSolvedPuzzles.length(); i++) {
                solvedPuzzles.add(jaSolvedPuzzles.getInt(i));
            }
            return solvedPuzzles;
        });
    }

    public static List<CustomPuzzleSave> loadCustomPuzzles(@NonNull Context context) throws CorruptedFileException {
        if (!fileExists(context, CUSTOM_PUZZLES_FILENAME)) {
            return new ArrayList<>();
        }
        return loadWithFallback(context, CUSTOM_PUZZLES_FILENAME, JsonUtils::getCustomPuzzlesFromJson);
    }

    public static List<CustomPuzzleSave> loadDefaultCustomPuzzles(@NonNull Context context) {
        try (InputStream fisPuzzles = context.getResources().openRawResource(R.raw.default_custom_puzzles)) {
            return getCustomPuzzlesFromJson(openJsonFile(fisPuzzles));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<CustomPuzzleSave> getCustomPuzzlesFromJson(@NonNull JSONObject jsonObject) throws JSONException {
        List<CustomPuzzleSave> customPuzzles = new ArrayList<>();

        JSONArray jaCustomPuzzles = jsonObject.getJSONArray("puzzles");
        for (int i = 0; i < jaCustomPuzzles.length(); i++) {
            customPuzzles.add(new CustomPuzzleSave(jaCustomPuzzles.getJSONObject(i)));
        }
        customPuzzles.sort(PuzzleSortMode.DIFFICULTY_ASC.getComparator());

        return customPuzzles;
    }

    public static List<CustomPuzzleSave> loadCustomPuzzlesFromFile(@NonNull Context context,
                                                                   @NonNull Uri uri) throws IllegalArgumentException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot get valid input stream from Uri");
            }
            List<CustomPuzzleSave> customPuzzles = new ArrayList<>();

            JSONArray jaCustomPuzzles = openJsonFile(inputStream).getJSONArray("puzzles");
            for (int i = 0; i < jaCustomPuzzles.length(); i++) {
                TransferablePuzzleSave puzzleSave = TransferablePuzzleSave.fromJson(
                        jaCustomPuzzles.getJSONObject(i)
                );
                customPuzzles.add(puzzleSave.toCustomSave());
            }

            return customPuzzles;
        } catch (IOException | JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void saveOfficialPuzzles(@NonNull Context context,
                                           @NonNull List<OfficialPuzzleSave> officialPuzzles) {
        saveSolvedOfficialPuzzles(context, officialPuzzles);
    }

    private static void saveSolvedOfficialPuzzles(@NonNull Context context,
                                                  @NonNull List<OfficialPuzzleSave> officialPuzzles) {
        JSONArray jaSolvedPuzzles = new JSONArray();
        for (OfficialPuzzleSave officialPuzzle : officialPuzzles) {
            if (officialPuzzle.isSolved()) {
                jaSolvedPuzzles.put(officialPuzzle.getId());
            }
        }

        JSONObject joSolvedPuzzles = new JSONObject();
        try {
            joSolvedPuzzles.put("solved_puzzles", jaSolvedPuzzles);
            saveJsonFile(context, SOLVED_OFFICIAL_PUZZLES_FILENAME, joSolvedPuzzles);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearOfficialPuzzlesSolved(@NonNull Context context) {
        if (fileExists(context, SOLVED_OFFICIAL_PUZZLES_FILENAME)) {
            deleteFile(context, SOLVED_OFFICIAL_PUZZLES_FILENAME);
        }
    }

    public static void saveCustomPuzzles(@NonNull Context context,
                                         @NonNull List<CustomPuzzleSave> customPuzzles) {
        try {
            JSONArray jaCustomPuzzles = new JSONArray();
            for (CustomPuzzleSave customPuzzle : customPuzzles) {
                jaCustomPuzzles.put(customPuzzle.getAsJson());
            }

            JSONObject joCustomPuzzles = new JSONObject();
            joCustomPuzzles.put("puzzles", jaCustomPuzzles);

            saveJsonFile(context, CUSTOM_PUZZLES_FILENAME, joCustomPuzzles);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void savePuzzlesToFile(@NonNull Context context,
                                         @NonNull List<PuzzleSave> puzzleSaves,
                                         @NonNull Uri uri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IllegalArgumentException("Cannot get valid outputStream stream from Uri");
            }

            JSONArray jaPuzzles = new JSONArray();
            for (PuzzleSave puzzleSave : puzzleSaves) {
                jaPuzzles.put(TransferablePuzzleSave.fromSave(puzzleSave).getAsJson());
            }

            JSONObject joCustomPuzzles = new JSONObject();
            joCustomPuzzles.put("puzzles", jaPuzzles);

            updateJsonFile(joCustomPuzzles, outputStream);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject loadNemesisMoves(@NonNull Context context) {
        try (InputStream fisNemesisMoves = context.getResources().openRawResource(R.raw.nemesis_moves)) {
            return openJsonFile(fisNemesisMoves);

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region GAME SNAPSHOT METHODS

    @NonNull
    private static String getGameSnapshotFileName(@NonNull String snapshotName) {
        return GAME_SNAPSHOT_FILENAME_PREFIX + snapshotName + ".json";
    }

    /**
     * Saves the history of a game in progress so it can be resumed if Android destroys the
     * activity while the app is in background. The file write is atomic: a process killed during
     * the save leaves the previous snapshot intact.
     */
    public static void saveGameSnapshot(@NonNull Context context,
                                        @NonNull String snapshotName,
                                        @NonNull GameHistory history) throws IOException {
        try {
            JSONObject joHistory = new GameHistorySerializer().getAsJson(history);
            writeAtomically(
                    getFile(context, getGameSnapshotFileName(snapshotName)),
                    joHistory.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (JSONException e) {
            throw new IOException("Unable to serialize the game snapshot " + snapshotName, e);
        }
    }

    /**
     * Loads a game snapshot.
     *
     * @return the snapshot, or {@code null} if there is none or if it is unusable. A snapshot is
     * only a convenience (unlike user saves): an unusable one is silently deleted, the caller
     * falls back to its usual initial history.
     */
    @Nullable
    public static GameHistory loadGameSnapshot(@NonNull Context context,
                                               @NonNull String snapshotName) {
        String fileName = getGameSnapshotFileName(snapshotName);
        if (!fileExists(context, fileName)) {
            return null;
        }

        try {
            return new GameHistorySerializer().getFromJson(
                    openJsonFile(context, fileName), new SerializationContext());
        } catch (JSONException | RuntimeException e) {
            Log.w(LOG_TAG, "Unusable game snapshot " + snapshotName + ", it is discarded", e);
            deleteGameSnapshot(context, snapshotName);
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") // "delete()" results are ignored here
    public static void deleteGameSnapshot(@NonNull Context context, @NonNull String snapshotName) {
        String fileName = getGameSnapshotFileName(snapshotName);
        // Best effort: the "File.delete" result is ignored on purpose, a leftover snapshot is
        // harmless (it is only loaded when the system gives back its matching saved state)
        getFile(context, fileName).delete();
        getFile(context, fileName + ATOMIC_FILE_BACKUP_SUFFIX).delete();
    }

    //endregion

    //region REPLAY SAVE METHODS

    public static List<ReplaySave> loadReplays(@NonNull Context context) throws CorruptedFileException {
        if (!fileExists(context, REPLAYS_FILENAME)) {
            return new ArrayList<>();
        }
        return loadWithFallback(context, REPLAYS_FILENAME, JsonUtils::getReplaysFromJson);
    }

    public static List<ReplaySave> loadDefaultReplays(@NonNull Context context) {
        try (InputStream fisReplays = context.getResources().openRawResource(R.raw.default_replays)) {
            return getReplaysFromJson(openJsonFile(fisReplays));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ReplaySave> loadReplaysFromFile(@NonNull Context context,
                                                       @NonNull Uri uri) throws IllegalArgumentException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot get valid input stream from Uri");
            }

            return getReplaysFromJson(openJsonFile(inputStream));
        } catch (IOException | JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<ReplaySave> getReplaysFromJson(@NonNull JSONObject joReplays) throws JSONException {
        JSONArray jaReplays = joReplays.getJSONArray("replays");

        List<ReplaySave> replays = new ArrayList<>();
        for (int i = 0; i < jaReplays.length(); i++) {
            replays.add(ReplaySave.getFromJson(jaReplays.getJSONObject(i)));
        }

        return replays;
    }

    public static void saveReplays(@NonNull Context context, @NonNull List<ReplaySave> replays) {
        try {
            saveJsonFile(context, REPLAYS_FILENAME, getReplaysAsJson(replays));
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveReplaysToFile(@NonNull Context context,
                                         @NonNull List<ReplaySave> replays,
                                         @NonNull Uri uri) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IllegalArgumentException("Cannot get valid outputStream stream from Uri");
            }

            updateJsonFile(getReplaysAsJson(replays), outputStream);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JSONObject getReplaysAsJson(@NonNull List<ReplaySave> replays) throws JSONException {
        JSONObject joReplays = new JSONObject();

        JSONArray jaReplays = new JSONArray();
        for (ReplaySave replay : replays) {
            jaReplays.put(replay.getAsJsonObject());
        }

        joReplays.put("replays", jaReplays);

        return joReplays;
    }

    //endregion

    //region CHARACTER DEMO METHODS

    public static GameHistory loadCharacterDemo(@NonNull Context context,
                                                @NonNull CharacterCard characterCard) {
        // Character demos are stored within raw resource files
        try (InputStream fisCharacterDemo = context.getResources().openRawResource(getCharacterDemoResId(characterCard))) {
            JSONObject joCharacterDemo = openJsonFile(fisCharacterDemo);

            GameHistorySerializer serializer = new GameHistorySerializer();

            return serializer.getFromJson(joCharacterDemo, new SerializationContext());

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static int getCharacterDemoResId(@NonNull CharacterCard characterCard) {
        switch (characterCard) {
            case Acrobat: return R.raw.character_demo_acrobat;
            case Archer: return R.raw.character_demo_archer;
            case Assassin: return R.raw.character_demo_assassin;
            case Brewmaster: return R.raw.character_demo_brewmaster;
            case Bruiser: return R.raw.character_demo_bruiser;
            case ClawLauncher: return R.raw.character_demo_claw_launcher;
            case HermitAndCub: return R.raw.character_demo_hermit_and_cub;
            case Illusionist: return R.raw.character_demo_illusionist;
            case Jailer: return R.raw.character_demo_jailer;
            case LeaderKing: return R.raw.character_demo_leader_king;
            case LeaderQueen: return R.raw.character_demo_leader_queen;
            case Manipulator: return R.raw.character_demo_manipulator;
            case Nemesis: return R.raw.character_demo_nemesis;
            case Protector: return R.raw.character_demo_protector;
            case Rider: return R.raw.character_demo_rider;
            case RoyalGuard: return R.raw.character_demo_royal_guard;
            case Vizier: return R.raw.character_demo_vizier;
            case Wanderer: return R.raw.character_demo_wanderer;
            default: throw new IllegalStateException("No character demo file found for character card: " + characterCard);
        }
    }

    //endregion

    //region TUTORIAL CHAPTER METHODS

    public static GameHistory loadTutorialChapter(@NonNull Context context,
                                                  @NonNull TutorialChapter tutorialChapter) {
        // Tutorial chapters are stored within raw resource files
        try (InputStream fisCharacterDemo = context.getResources().openRawResource(getTutorialChapterResId(tutorialChapter))) {
            JSONObject joCharacterDemo = openJsonFile(fisCharacterDemo);

            GameHistorySerializer serializer = new GameHistorySerializer();

            return serializer.getFromJson(joCharacterDemo, new SerializationContext());

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getTutorialChapterResId(@NonNull TutorialChapter tutorialChapter) {
        switch (tutorialChapter) {
            case CaptureLeader: return R.raw.tutorial_capture;
            case ActiveAbilities: return R.raw.tutorial_active_abilities;
            case PassiveAbilities: return R.raw.tutorial_passive_abilities;
            case Recruitment: return R.raw.tutorial_recruitment;
            case SurroundLeader: return R.raw.tutorial_surround;
            default: throw new IllegalStateException("No tutorial chapter file found for: " + tutorialChapter);
        }
    }

    //endregion

    //region CHARACTER DEMO METHODS

    public static Settings loadSettings(@NonNull Context context) {
        if (fileExists(context, SETTINGS_FILENAME)) {
            try {
                return Settings.getFromJson(openJsonFile(context, SETTINGS_FILENAME));
            } catch (IllegalArgumentException e) {
                Log.w(LOG_TAG, "Unable to load settings", e);
            }
        }
        // Without a valid settings save file, we return the default value
        return new Settings();
    }

    public static void saveSettings(@NonNull Context context, @NonNull Settings settings) {
        try {
            saveJsonFile(context, SETTINGS_FILENAME, settings.getAsJson());
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region CONTRIBUTORS METHODS

    public static List<Contributor> loadContributors(@NonNull Context context) {
        try (InputStream fisContributors = context.getResources().openRawResource(R.raw.contributors)) {
            JSONObject joContributors = openJsonFile(fisContributors);
            JSONArray jaContributors = joContributors.getJSONArray("contributors");

            List<Contributor> contributors = new ArrayList<>();

            for (int i = 0; i < jaContributors.length(); i++) {
                contributors.add(Contributor.getFromJson(jaContributors.getJSONObject(i)));
            }

            return contributors;

        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion
}