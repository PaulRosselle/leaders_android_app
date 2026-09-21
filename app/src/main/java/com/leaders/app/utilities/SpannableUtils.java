package com.leaders.app.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.NoSuchElementException;

public class SpannableUtils {
    private final static String IMAGE_SPAN_TAG = "{IMAGE_%s}";

    private SpannableUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static Spannable getImageSpannable(@NonNull Context context, int stringResId,
                                              @NonNull List<ImageSpan> imageSpans) {
        Spannable spannable = new SpannableString(context.getString(stringResId));

        for (int i = 0; i < imageSpans.size(); i++) {
            final String imageTag = String.format(IMAGE_SPAN_TAG, i + 1);

            final int start = spannable.toString().indexOf(imageTag);
            if (start == -1) {
                throw new NoSuchElementException("No tag found for " + imageTag + " in " + stringResId);
            }
            final int end = start + imageTag.length();

            spannable.setSpan(
                    imageSpans.get(i),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return spannable;
    }

    public static ImageSpan getImageSpan(@NonNull Context context, int imageResId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(context, imageResId);
        if (drawable == null) {
            throw new NoSuchElementException("No resource found for id=" + imageResId);
        }

        drawable.setBounds(0, 0, width, height);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ImageSpan(drawable, ImageSpan.ALIGN_CENTER);
        } else {
            return new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        }
    }
}
