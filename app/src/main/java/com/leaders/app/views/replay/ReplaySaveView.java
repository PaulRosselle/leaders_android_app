package com.leaders.app.views.replay;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ReplaySaveView extends ConstraintLayout {
    private String defaultName;

    private final EditText edtName;

    private final EditText edtDate;
    private final ImageView imvDate;

    private final TextView txvCancel;
    private final TextView txvSave;

    private LocalDate date;

    public ReplaySaveView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_replay_save, this);

        edtName = findViewById(R.id.edtName_vwReplaySave);
        edtDate = findViewById(R.id.edtDate_vwReplaySave);
        imvDate = findViewById(R.id.imvDate_vwReplaySave);

        txvCancel = findViewById(R.id.txvCancel_vwReplaySave);
        txvSave = findViewById(R.id.txvSave_vwReplaySave);

        defaultName = "";
        setDate(LocalDate.now());

        initListeners();
    }

    private void initListeners() {
        edtDate.setOnClickListener(this::onDateClick);
        imvDate.setOnClickListener(this::onDateClick);
    }

    private void onDateClick(View v) {
        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.alert_dialog_theme);
        dialog.setOnDateSetListener(this::onDateSet);
        dialog.updateDate(date.getYear(), date.getMonthValue() + 1, date.getDayOfMonth());
        dialog.show();
    }

    private void onDateSet(DatePicker datePicker, int year, int monthValue, int dayOfMonth) {
        setDate(LocalDate.of(year, monthValue + 1, dayOfMonth));
    }

    public void setDefaultName(@NonNull String defaultName) {
        this.defaultName = defaultName;
        edtName.setHint(this.defaultName);
    }

    public void setName(@NonNull String name) {
        edtName.setText(name);
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault());
        edtDate.setText(formatter.format(date));
    }

    public LocalDate getDate() {
        return date;
    }

    private String getFormattedText(EditText editText) {
        return getFormattedText(editText.getText().toString());
    }

    private String getFormattedText(@NonNull String text) {
        return text.trim();
    }

    public String getName() {
        String name = getFormattedText(edtName);
        return name.isEmpty() ? getFormattedText(defaultName) : name;
    }

    public void setOnCancelClick(@NonNull OnClickListener onCancelClick) {
        txvCancel.setOnClickListener(onCancelClick);
    }

    public void setOnSaveClick(@NonNull OnClickListener onSaveClick) {
        txvSave.setOnClickListener(onSaveClick);
    }
}
