package com.leaders.app.views.settings;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class UserNameView extends ConstraintLayout {
    public interface onNameChangeListener {
        void onNameChange(@NonNull String name);
    }

    private final EditText edtName;

    private onNameChangeListener changeListener;


    public UserNameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_user_name, this);

        edtName = findViewById(R.id.edtName_vwUserName);

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (changeListener != null) {
                    changeListener.onNameChange(getFormattedString(editable));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No treatment here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // No treatment here
            }
        });
    }

    @NonNull
    private String getFormattedString(@NonNull Editable editable) {
        return editable.toString().trim();
    }

    @NonNull
    public String getName() {
        return getFormattedString(edtName.getText());
    }

    public void setName(@NonNull String name) {
        edtName.setText(name);
    }

    public void setChangeListener(onNameChangeListener changeListener) {
        this.changeListener = changeListener;
    }
}
