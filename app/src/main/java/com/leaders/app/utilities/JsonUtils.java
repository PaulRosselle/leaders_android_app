package com.leaders.app.utilities;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.ReplaySave;
import com.leaders.app.entities.crash.CrashLog;
import com.leaders.puzzlelogic.entities.CustomPuzzleSave;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
        return getFile(context, fileName).exists();
    }

    private static void deleteFile(@NonNull Context context, @NonNull String fileName) {
        File file = getFile(context, fileName);

        // A missing file is considered successfully deleted.
        if (!file.delete() && file.exists()) {
            throw new IllegalStateException(String.format("Unable to delete file \"%s\"", fileName));
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

        try (FileInputStream inputStream = context.openFileInput(fileName)) {
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

    @SuppressWarnings({"IOStreamConstructor", "ResultOfMethodCallIgnored"})
    private static void saveJsonFile(@NonNull Context context,
                                     @NonNull String fileName,
                                     @NonNull JSONObject jsonObject) throws IOException {
        File directory = context.getFilesDir();

        File file = new File(directory, fileName);
        File temporaryFile = new File(directory, fileName + ".tmp");
        File backupFile = new File(directory, fileName + ".bak");

        // 1. Write the new content into the temporary file.
        try (OutputStream outputStream = new FileOutputStream(temporaryFile)) {
            updateJsonFile(jsonObject, outputStream);
        }

        // 2. Validate the temporary file before touching the existing file.
        try (InputStream inputStream = new FileInputStream(temporaryFile)) {
            openJsonFile(inputStream);
        } catch (JSONException e) {
            // The temporary file is invalid. Keep the existing file untouched.
            if (!temporaryFile.delete() && temporaryFile.exists()) {
                throw new IOException(String.format("Unable to delete temporary file \"%s\"", temporaryFile.getName()), e);
            }

            throw new IOException(String.format("Generated JSON for file \"%s\" is invalid", fileName), e);
        }

        // 3. Keep the current version as a backup.
        if (file.exists()) {
            if (backupFile.exists() && !backupFile.delete()) {
                throw new IOException(String.format("Unable to delete backup file \"%s\"", backupFile.getName()));
            }

            if (!file.renameTo(backupFile)) {
                throw new IOException(String.format("Unable to create backup for file \"%s\"", fileName));
            }
        }

        // 4. Replace the current version with the new version.
        if (!temporaryFile.renameTo(file)) {
            // Try to restore the previous version.
            if (backupFile.exists() && !file.exists()) {
                backupFile.renameTo(file);
            }

            throw new IOException(String.format("Unable to replace file \"%s\"", fileName));
        }
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

    public static List<OfficialPuzzleSave> loadOfficialPuzzles(@NonNull Context context) {
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

    private static Set<Integer> loadSolvedOfficialPuzzles(@NonNull Context context) {
        if (!fileExists(context, SOLVED_OFFICIAL_PUZZLES_FILENAME)) {
            return Collections.emptySet();
        }

        try {
            JSONObject joSolvedPuzzles = openJsonFile(context, SOLVED_OFFICIAL_PUZZLES_FILENAME);
            JSONArray jaSolvedPuzzles = joSolvedPuzzles.getJSONArray("solved_puzzles");

            Set<Integer> solvedPuzzles = new HashSet<>();
            for (int i = 0; i < jaSolvedPuzzles.length(); i++) {
                solvedPuzzles.add(jaSolvedPuzzles.getInt(i));
            }
            return solvedPuzzles;
        } catch (JSONException e) {
            // If the official puzzle solved tracking file is corrupted, it is better to remove it
            deleteFile(context, SOLVED_OFFICIAL_PUZZLES_FILENAME);
            throw new RuntimeException(e);
        }
    }

    public static List<CustomPuzzleSave> loadCustomPuzzles(@NonNull Context context) {
        if (!fileExists(context, CUSTOM_PUZZLES_FILENAME)) {
            return new ArrayList<>();
        }

        try {
            return getCustomPuzzlesFromJson(openJsonFile(context, CUSTOM_PUZZLES_FILENAME));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public static List<CustomPuzzleSave> loadCustomPuzzlesFromFile(@NonNull Context context,
                                                                   @NonNull Uri uri) throws IllegalArgumentException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot get valid input stream from Uri");
            }

            return getCustomPuzzlesFromJson(openJsonFile(inputStream));
        } catch (IOException | JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<CustomPuzzleSave> getCustomPuzzlesFromJson(@NonNull JSONObject joCustomPuzzles) throws JSONException {
        List<CustomPuzzleSave> customPuzzles = new ArrayList<>();

        JSONArray jaCustomPuzzles = joCustomPuzzles.getJSONArray("puzzles");
        for (int i = 0; i < jaCustomPuzzles.length(); i++) {
            customPuzzles.add(new CustomPuzzleSave(jaCustomPuzzles.getJSONObject(i)));
        }

        return customPuzzles;
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

    public static void saveCustomPuzzles(@NonNull Context context,
                                         @NonNull List<CustomPuzzleSave> customPuzzles) {
        try {
            JSONArray jaCustomPuzzles = new JSONArray();
            for (CustomPuzzleSave customPuzzle : customPuzzles) {
                jaCustomPuzzles.put(customPuzzle.getAsJsonObject());
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
                // We only save puzzle in files as custom puzzles
                CustomPuzzleSave customPuzzleSave;
                if (puzzleSave instanceof CustomPuzzleSave) {
                    customPuzzleSave = (CustomPuzzleSave) puzzleSave;
                } else {
                    customPuzzleSave = new CustomPuzzleSave(puzzleSave.getName(), "",
                            puzzleSave.getLifetime(), puzzleSave.getDatas(), puzzleSave.isSolved());
                }
                jaPuzzles.put(customPuzzleSave.getAsJsonObject());
            }

            JSONObject joCustomPuzzles = new JSONObject();
            joCustomPuzzles.put("puzzles", jaPuzzles);

            updateJsonFile(joCustomPuzzles, outputStream);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion



    //region PUZZLE SAVE METHODS

    public static List<ReplaySave> loadReplays(@NonNull Context context) {
        if (!fileExists(context, REPLAYS_FILENAME)) {
            return new ArrayList<>();
        }

        try {
            JSONObject joReplays = openJsonFile(context, REPLAYS_FILENAME);

            JSONArray jaReplays = joReplays.getJSONArray("replays");

            List<ReplaySave> replays = new ArrayList<>();
            for (int i = 0; i < jaReplays.length(); i++) {
                replays.add(ReplaySave.getFromJson(jaReplays.getJSONObject(i)));
            }

            return replays;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
}