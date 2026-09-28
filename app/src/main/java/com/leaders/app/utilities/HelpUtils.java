package com.leaders.app.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.leaders.R;

import java.util.List;

public final class HelpUtils {
    private HelpUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static AlertDialog.Builder getHelpDialog(@NonNull Context context) {
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.image_span_default_size);

        ImageSpan cancelIcon = SpannableUtils.getImageSpan(context, R.drawable.icon_cancel, iconSize, iconSize);
        cancelIcon.getDrawable().setTint(context.getColor(R.color.font));

        Spannable helpMessage = SpannableUtils.getImageSpannable(context,
                R.string.help_message,
                List.of(cancelIcon)
        );

        Drawable helpIcon = AppCompatResources.getDrawable(context, R.drawable.icon_question);
        if (helpIcon == null) {
            throw new IllegalStateException("Help icon not found");
        }
        helpIcon.setTint(context.getColor(R.color.app_golden));

        return new AlertDialog.Builder(context, R.style.alert_dialog_theme)
                .setIcon(helpIcon)
                .setTitle(R.string.help)
                .setMessage(helpMessage);
    }
}
