package com.leaders.app.views.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.utilities.ContactUtils;

public class ContactFormView extends ConstraintLayout {
    private final static String CONTACT_TITLE = "REPORT BUG | SUGGESTION";
    private final EditText edtSubject;
    private final EditText edtMessage;

    public ContactFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_contact_form, this);

        edtSubject = findViewById(R.id.edtSubject_vwContactForm);
        edtMessage = findViewById(R.id.edtMessage_vwContactForm);
        findViewById(R.id.txvSend_vwContactForm).setOnClickListener(this::onSendClick);
        findViewById(R.id.txvCancel_vwContactForm).setOnClickListener(v -> hide());
        setOnClickListener(view -> {
            // Dummy listener to intercept click events on the form background
        });
    }

    public void show() {
        edtSubject.setText("");
        edtMessage.setText("");
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    private String getFormattedText(@NonNull EditText editText) {
        return editText.getText().toString().trim();
    }

    private void onSendClick(View v) {
        ContactUtils.sendMessage(
                getContext(),
                CONTACT_TITLE,
                getFormattedText(edtSubject),
                getFormattedText(edtMessage)
        );

        hide();
    }
}
