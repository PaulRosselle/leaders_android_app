package com.leaders.app.views.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.leaders.R;

import java.util.Locale;
import java.util.Objects;

public class LanguageSelectionView extends ConstraintLayout {
    private enum AppLanguage {
        ENGLISH(Locale.ENGLISH.getLanguage()),
        FRENCH(Locale.FRENCH.getLanguage());

        private final String languageTag;

        AppLanguage(@NonNull String languageTag) {
            this.languageTag = languageTag;
        }

        @NonNull
        static AppLanguage fromCode(@NonNull String code) {
            for (AppLanguage language : values()) {
                if (language.languageTag.equalsIgnoreCase(code)) {
                    return language;
                }
            }

            // Language not handled: english is used by default.
            return ENGLISH;
        }

        @NonNull
        LocaleListCompat getLocale() {
            return LocaleListCompat.forLanguageTags(languageTag);
        }
    }

    private MaterialButtonToggleGroup toggleGroup;

    private boolean updatingSelection;

    public LanguageSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_language_selection, this);

        setBackgroundResource(R.drawable.round_rect);
        initViews();
    }

    private void initViews() {
        toggleGroup = findViewById(R.id.mbtgPuzzlesCategory_vwLanguageSelection);

        toggleGroup.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {
                    if (updatingSelection || !isChecked) {
                        return;
                    }

                    AppLanguage language = getLanguageForButton(checkedId);

                    if (language != null) {
                        setAppLanguage(language);
                    }
                }
        );

        selectLanguage(getEffectiveAppLanguage());
    }

    @Nullable
    private AppLanguage getLanguageForButton(int buttonId) {
        if (buttonId == R.id.btnOfficial_vwLanguageSelection) {
            return AppLanguage.ENGLISH;
        }
        if (buttonId == R.id.btnCustom_vwLanguageSelection) {
            return AppLanguage.FRENCH;
        }

        return null;
    }

    private int getButtonForLanguage(@NonNull AppLanguage language) {
        switch (language) {
            case ENGLISH: return R.id.btnOfficial_vwLanguageSelection;
            case FRENCH: return R.id.btnCustom_vwLanguageSelection;
            default: throw new IllegalStateException("Unsupported application language: " + language);
        }
    }

    @NonNull
    private AppLanguage getEffectiveAppLanguage() {
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();

        // A language has been explicitely selected for the app
        if (!appLocales.isEmpty()) {
            Locale locale = Objects.requireNonNull(appLocales.get(0));
            return AppLanguage.fromCode(locale.getLanguage());
        }

        // No preference: we use the config language
        Configuration configuration = getResources().getConfiguration();
        Locale locale = configuration.getLocales().get(0);

        return AppLanguage.fromCode(locale.getLanguage());
    }

    private void selectLanguage(@NonNull AppLanguage language) {
        int buttonId = getButtonForLanguage(language);

        updatingSelection = true;
        try {
            toggleGroup.check(buttonId);
        } finally {
            updatingSelection = false;
        }
    }

    private void setAppLanguage(@NonNull AppLanguage language) {
        AppCompatDelegate.setApplicationLocales(language.getLocale());
    }
}
