package com.leaders.app.utilities;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class ContactUtils {
    private final static String DEV_APP_EMAIL = "leaders.unofficial.app@gmail.com";
    private final static String EMAIL_MIME_TYPE = "message/rfc822";

    private ContactUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void sendMessage(@NonNull Context context, String title, String subject, String text) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { DEV_APP_EMAIL } );
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType(EMAIL_MIME_TYPE);
        context.startActivity(Intent.createChooser(sendIntent, title));
    }
}
