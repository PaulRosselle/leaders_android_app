package com.leaders.app.entities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.leaders.R;

public final class CorruptedDataDialog {
    private CorruptedDataDialog() {}

    public static void show(@NonNull Context context,
                            @NonNull Runnable onRetry,
                            @NonNull Runnable onReset,
                            @NonNull Runnable onCancel) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.corrupted_data_title)
                .setMessage(R.string.corrupted_data_message)
                .setCancelable(false)
                .setPositiveButton(R.string.retry, (d, w) -> onRetry.run())
                .setNeutralButton(R.string.reset_data, (d, w) -> confirmReset(context, onReset, onCancel))
                .setNegativeButton(R.string.back, (d, w) -> onCancel.run())
                .show();
    }

    private static void confirmReset(Context context, Runnable onReset, Runnable onCancel) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.data_will_be_forever_lost)
                .setCancelable(false)
                .setPositiveButton(R.string.retry, (d, w) -> onReset.run())
                .setNegativeButton(R.string.cancel, (d, w) -> onCancel.run())
                .show();
    }
}
