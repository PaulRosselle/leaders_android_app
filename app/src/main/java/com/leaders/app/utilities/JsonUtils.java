package com.leaders.app.utilities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.app.entities.crash.CrashLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class JsonUtils {

    private static final String CRASH_LOG_FILENAME = "crash_log.json";

    private JsonUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    //region GENERIC FILE METHODS

    @NonNull
    private static File getFile(@NonNull Context context, @NonNull String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

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
}